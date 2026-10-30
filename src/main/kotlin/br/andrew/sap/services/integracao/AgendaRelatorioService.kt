package br.andrew.sap.services.integracao

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.agenda.AgendaConfig
import br.andrew.sap.model.agenda.AgendaRelatorio
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.security.AuthService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Agenda do e-mail de inadimplencia (UDO AGENDA_RELATORIO). Sem registro no SAP, ou com o UDO fora do ar,
 * vale a agenda padrao: segunda 07:00, com a copia de `report.inadimplencia.cc`. Assim subir esta versao
 * sem a flag `fields.agenda-relatorio` nao muda nada para quem ja recebe o relatorio.
 */
@Service
class AgendaRelatorioService(
    env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService,
    private val relatorio: RelatorioInadimplenciaService,
    @Value("\${report.inadimplencia.cc:}") private val copiaPadrao: List<String> = listOf(),
) : EntitiesService<AgendaRelatorio>(env, restTemplate, authService) {

    private val logger = LoggerFactory.getLogger(AgendaRelatorioService::class.java)

    // Segunda trava, so em memoria: se o SAP estiver fora e o "ultimo envio" nao puder ser gravado, o minuto
    // seguinte nao reenvia o relatorio ate a janela fechar.
    @Volatile private var diaEnviado: LocalDate? = null

    override fun path(): String = "/b1s/v1/AGENDA_RELATORIO"

    fun emAndamento() = relatorio.emAndamento()

    private fun padrao() = AgendaConfig(copia = copiaPadrao.map { it.trim() }.filter { it.isNotEmpty() })

    /** Leitura para a tela: erro do SAP aparece para o usuario. */
    fun obter(): AgendaConfig =
        get(Filter(Predicate("Code", CODIGO, Condicao.EQUAL))).tryGetValues<AgendaRelatorio>()
            .firstOrNull()?.let(AgendaConfig::de) ?: padrao()

    /** Leitura do agendador: nunca falha, cai na agenda padrao. */
    private fun obterTolerante(): AgendaConfig =
        try { obter() } catch (e: Exception) {
            logger.warn("Agenda do relatorio de inadimplencia indisponivel no SAP, usando a padrao: {}", e.message)
            padrao()
        }

    fun salvar(config: AgendaConfig): AgendaConfig {
        val existe = get(Filter(Predicate("Code", CODIGO, Condicao.EQUAL))).tryGetValues<AgendaRelatorio>().isNotEmpty()
        val entidade = config.paraEntidade(CODIGO)
        if (existe) update(entidade, CODIGO) else save(entidade)
        return obter()
    }

    /** Chamado a cada minuto; envia quando a agenda manda e ainda nao enviou hoje. */
    fun verificar(agora: LocalDateTime = LocalDateTime.now()) {
        if (diaEnviado == agora.toLocalDate()) return
        val config = obterTolerante()
        if (!config.deveEnviar(agora)) return
        val iniciou = relatorio.emSegundoPlano {
            diaEnviado = agora.toLocalDate()
            tentarGravar(config.copy(ultimoEnvio = agora, ultimoStatus = "Envio em andamento"))
            val status = executar(config.copia, null, "Agendado")
            tentarGravar(config.copy(ultimoEnvio = agora, ultimoStatus = status))
        }
        if (!iniciou) logger.info("Envio do relatorio de inadimplencia ja em andamento, aguardando.")
    }

    /** Dispara o envio na hora, para todos ou para um vendedor. false = ja tem um envio rodando. */
    fun enviarAgora(slpCode: Int?): Boolean = relatorio.emSegundoPlano {
        val config = obterTolerante()
        val status = executar(config.copia, slpCode, "Manual")
        // Envio manual nao mexe em ultimoEnvio: nao pode impedir o envio agendado do dia.
        tentarGravar(config.copy(ultimoStatus = status))
    }

    private fun executar(copia: List<String>, slpCode: Int?, origem: String): String {
        val quando = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
        return try {
            val r = relatorio.enviar(copia, slpCode)
            "$origem $quando: ${r.enviados} enviado(s), ${r.erros} com erro"
        } catch (e: Exception) {
            logger.error("Falha no envio do relatorio de inadimplencia", e)
            "$origem $quando: falhou - ${e.message}".take(254)
        }
    }

    private fun tentarGravar(config: AgendaConfig) {
        try { salvar(config) } catch (e: Exception) {
            logger.warn("Nao foi possivel gravar o estado da agenda do relatorio no SAP: {}", e.message)
        }
    }

    companion object {
        const val CODIGO = "INADIMPLENCIA"
    }
}
