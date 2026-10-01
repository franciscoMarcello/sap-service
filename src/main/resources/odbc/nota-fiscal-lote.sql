-- NF-e de saida (OINV, Model 39 = modelo 55) que foram emitidas para a SEFAZ, com o status
-- do SAP e o da SEFAZ. Usada pela listagem paginada e pelo download do lote.
--
-- "Emitida para a SEFAZ" = tem linha na "Process" da TaxPlus COM chave de acesso. Nota sem
-- essa linha nunca foi transmitida e nao tem DANFE nem XML para baixar.
--
-- CANCELED = 'C' e o documento de cancelamento que o SAP gera, nao uma nota: fica de fora. A
-- nota cancelada de verdade (CANCELED = 'Y') entra, com status SAP CANCELADO - o contador
-- tambem precisa do XML dela.
--
-- Filtros condicionais seguem o painel de vendas: flag/sentinela desliga o filtro, porque
-- o sap-odbc exige que todo parametro enviado apareca na consulta e `IN ()` e erro de
-- sintaxe. :filiais e :docEntries nunca vao vazias; :todasFiliais / :todosDocs desligam.
-- Datas sempre vao preenchidas (o servico usa uma faixa larga quando a tela nao manda):
-- comparar DATE com '' e erro de conversao no HANA, entao vencimento usa flag como a filial.
--
-- VENCIMENTO e por PARCELA (INV6), nao pelo DocDueDate: o DocDueDate e sempre o vencimento
-- da ULTIMA parcela (conferido no HOMOLOG2 em 2026-10-01: 9.094 de 9.094 notas de 2026), e
-- filtrar por ele perderia a nota de 3 parcelas cuja 1a vence no periodo. A nota entra se
-- QUALQUER parcela vence no intervalo. Para parcela unica (92% das notas) da no mesmo.
--
-- Faixa de NF compara o "Serial" puro: a numeracao e por serie/filial, entao NF 100 a 200
-- sem filtro de filial traz as de todas as filiais nessa faixa.
--
-- Paginacao por ROW_NUMBER em vez de LIMIT/OFFSET com bind; COUNT(*) OVER () traz o total
-- do filtro na mesma ida ao banco. O filtro de status SAP fica em NUMERADAS porque o status
-- e um CASE calculado em NOTAS - filtrar depois do ROW_NUMBER quebraria a paginacao.
WITH NOTAS AS (
    SELECT
        N."DocEntry" AS "DocEntry",
        N."DocNum" AS "DocNum",
        N."Serial" AS "Numero",
        N."SeriesStr" AS "Serie",
        TO_VARCHAR(N."DocDate", 'YYYY-MM-DD') AS "DocDate",
        N."CardCode" AS "CardCode",
        N."CardName" AS "CardName",
        N."BPLId" AS "BPLId",
        N."BPLName" AS "BPLName",
        N."DocTotal" AS "DocTotal",
        TO_VARCHAR(PARC."PrimeiroVencimento", 'YYYY-MM-DD') AS "VencimentoInicial",
        TO_VARCHAR(N."DocDueDate", 'YYYY-MM-DD') AS "VencimentoFinal",
        IFNULL(PARC."Parcelas", 0) AS "Parcelas",
        N."SlpCode" AS "SlpCode",
        V."SlpName" AS "SlpName",
        P."KeyNfe" AS "ChaveAcesso",
        P."StatusId" AS "StatusSefazId",
        S."Description" AS "StatusSefaz",
        CASE
            WHEN N."CANCELED" = 'Y' THEN 'CANCELADO'
            WHEN N."DocStatus" = 'C' THEN 'FECHADO'
            ELSE 'ABERTO'
        END AS "StatusSap"
    FROM OINV N
    INNER JOIN "Process" P ON P."DocEntry" = N."DocEntry" AND P."DocType" = N."ObjType"
    LEFT JOIN "ProcessStatus" S ON S."ID" = P."StatusId"
    LEFT JOIN OSLP V ON V."SlpCode" = N."SlpCode"
    LEFT JOIN (
        SELECT I."DocEntry", MIN(I."DueDate") AS "PrimeiroVencimento", COUNT(*) AS "Parcelas"
        FROM INV6 I
        GROUP BY I."DocEntry"
    ) PARC ON PARC."DocEntry" = N."DocEntry"
    WHERE N."Model" = 39
      AND N."CANCELED" <> 'C'
      AND IFNULL(P."KeyNfe", '') <> ''
      AND N."DocDate" >= :dataInicial
      AND N."DocDate" <= :dataFinal
      AND (:todasFiliais = 1 OR N."BPLId" IN (:filiais))
      AND (:todosDocs = 1 OR N."DocEntry" IN (:docEntries))
      AND (:cardCode = '' OR N."CardCode" = :cardCode)
      AND (:vendedor = -1 OR N."SlpCode" = :vendedor)
      AND (:numeroDe = -1 OR N."Serial" >= :numeroDe)
      AND (:numeroAte = -1 OR N."Serial" <= :numeroAte)
      AND (:todosVencimentos = 1 OR EXISTS (
            SELECT 1 FROM INV6 VI
            WHERE VI."DocEntry" = N."DocEntry"
              AND VI."DueDate" >= :vencimentoDe
              AND VI."DueDate" <= :vencimentoAte))
      AND (:statusSefaz = -1 OR P."StatusId" = :statusSefaz)
),
NUMERADAS AS (
    SELECT
        NOTAS.*,
        ROW_NUMBER() OVER (ORDER BY "DocDate" DESC, "DocEntry" DESC) AS "Linha",
        COUNT(*) OVER () AS "Total"
    FROM NOTAS
    WHERE (:statusSap = '' OR "StatusSap" = :statusSap)
)
SELECT *
FROM NUMERADAS
WHERE "Linha" > :inicio AND "Linha" <= :fim
ORDER BY "Linha"
