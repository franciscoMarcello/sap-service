package br.andrew.sap.model.fiscal

import java.math.BigDecimal
import java.nio.file.Path

/**
 * NF-e de saida emitida para a SEFAZ, como sai de odbc/nota-fiscal-lote.sql.
 *
 * Os nomes de coluna sao os aliases do SQL. O sap-odbc serializa decimal como string, por
 * isso numero passa sempre por toString/BigDecimal.
 */
class NotaFiscalResumo(
    val docEntry: Int,
    val docNum: Int?,
    val numero: Int?,
    val serie: String?,
    val data: String?,
    val cardCode: String?,
    val cardName: String?,
    val filialId: Int?,
    val filial: String?,
    val total: BigDecimal?,
    // Primeiro e ultimo vencimento das parcelas (INV6). O ultimo e o DocDueDate.
    val vencimentoInicial: String?,
    val vencimentoFinal: String?,
    val parcelas: Int,
    val vendedorCodigo: Int?,
    val vendedor: String?,
    val chaveAcesso: String?,
    val statusSefazId: Int?,
    val statusSefaz: String?,
    val statusSap: StatusSapNota?,
) {
    companion object {
        fun from(linha: Map<String, Any?>) = NotaFiscalResumo(
            docEntry = linha.inteiro("DocEntry") ?: throw IllegalStateException("Linha sem DocEntry"),
            docNum = linha.inteiro("DocNum"),
            numero = linha.inteiro("Numero"),
            serie = linha.texto("Serie"),
            data = linha.texto("DocDate")?.take(10),
            cardCode = linha.texto("CardCode"),
            cardName = linha.texto("CardName"),
            filialId = linha.inteiro("BPLId"),
            filial = linha.texto("BPLName"),
            total = linha.texto("DocTotal")?.toBigDecimalOrNull(),
            vencimentoInicial = linha.texto("VencimentoInicial")?.take(10),
            vencimentoFinal = linha.texto("VencimentoFinal")?.take(10),
            parcelas = linha.inteiro("Parcelas") ?: 0,
            vendedorCodigo = linha.inteiro("SlpCode")?.takeIf { it != -1 },
            vendedor = linha.texto("SlpName"),
            chaveAcesso = linha.texto("ChaveAcesso"),
            statusSefazId = linha.inteiro("StatusSefazId"),
            statusSefaz = linha.texto("StatusSefaz"),
            statusSap = linha.texto("StatusSap")?.let { StatusSapNota.valueOf(it) },
        )

        private fun Map<String, Any?>.texto(campo: String): String? = this[campo]?.toString()?.trim()?.ifEmpty { null }

        private fun Map<String, Any?>.inteiro(campo: String): Int? = texto(campo)?.toBigDecimal()?.toInt()
    }
}

// Mesma regra do "Status SAP" dos relatorios de nota (sql/alertas, "notas com error").
enum class StatusSapNota { ABERTO, FECHADO, CANCELADO }

class StatusSefaz(val id: Int, val descricao: String?)

class NotaFiscalLoteFiltro(
    val dataInicial: String? = null,
    val dataFinal: String? = null,
    val filial: List<Int>? = null,
    val cardCode: String? = null,
    val salesPersonCode: Int? = null,
    // Faixa de numero da NF ("Serial"); um lado so tambem vale (de 500 em diante).
    val numeroDe: Int? = null,
    val numeroAte: Int? = null,
    // Nota entra se QUALQUER parcela vence no intervalo (ver odbc/nota-fiscal-lote.sql).
    val vencimentoDe: String? = null,
    val vencimentoAte: String? = null,
    val statusSap: StatusSapNota? = null,
    val statusSefaz: Int? = null,
)

class NotaFiscalLoteRequest(
    val docEntries: List<Int> = listOf(),
    val pdf: Boolean = true,
    val xml: Boolean = true,
    val boleto: Boolean = false,
)

// O zip fica num arquivo temporario (ver NotaFiscalLoteService.zip); quem envia apaga.
class NotaFiscalLoteArquivo(
    val nome: String,
    val caminho: Path,
    val notasComErro: Int,
    // Notas sem nenhum boleto no zip (sem boleto na BankPlus ou so cancelado). Nao e erro.
    val notasSemBoleto: Int = 0,
)
