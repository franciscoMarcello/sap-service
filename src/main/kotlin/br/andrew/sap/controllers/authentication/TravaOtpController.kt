package br.andrew.sap.controllers.authentication

import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.integracao.TelegramRequestService
import br.andrew.sap.services.security.TravaOtpService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * Gera o codigo de liberacao (OTP) das travas da TransactionNotification.
 *
 * Restricao de quem pode gerar:
 *  - producao (spring.security.disable=false): via rules.yml, papel `liberacao_trava`
 *    (admin ja cobre tudo pelo curinga). O RoleBasedAuthorizationFilter barra o resto com 403.
 *  - homologacao (spring.security.disable=true): a cadeia vira permitAll e o filtro de
 *    papel nao roda; o endpoint fica aberto para teste. O check em codigo abaixo so
 *    atua quando `auth` e um User autenticado de verdade.
 *
 * O segredo nao trafega para o front: o front so recebe o codigo ja calculado.
 */
@RestController
@RequestMapping("trava")
class TravaOtpController(
    val travaOtpService: TravaOtpService,
    val telegramService: TelegramRequestService,
) {
    private val log = LoggerFactory.getLogger(TravaOtpController::class.java)

    @GetMapping("/otp")
    fun gerar(
        @RequestParam("regra") regra: String,
        auth: Authentication,
    ): TravaOtpResponse {
        val user = auth as? User
        if (user != null && !(user.isAdmin() || user.roles.contains(ROLE)))
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissao para gerar codigo de liberacao de trava")

        val regraNorm = regra.trim().uppercase()
        val codigo = travaOtpService.gerar(regraNorm)
        val quem = user?.let { "${it._name} (${it.id})" } ?: "desconhecido"

        // Auditoria: registra QUEM e QUAL regra, nunca o codigo em si.
        log.info("Codigo de liberacao de trava gerado por {} para regra {}", quem, regraNorm)
        runCatching {
            telegramService.send("Liberacao de trava: $quem gerou codigo para a regra '$regraNorm'")
        }

        return TravaOtpResponse(
            regra = regraNorm,
            codigo = codigo,
            expiraEmSegundos = travaOtpService.segundosParaProximaJanela(),
        )
    }

    companion object {
        const val ROLE = "liberacao_trava"
    }
}

data class TravaOtpResponse(
    val regra: String,
    val codigo: String,
    val expiraEmSegundos: Long,
)
