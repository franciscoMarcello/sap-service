package br.andrew.sap.controllers.documents

import br.andrew.sap.model.payment.PaymentMethodDto
import br.andrew.sap.services.FormaPagamentoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("forma-pagamento")
class FormaPagamentoController(val service: FormaPagamentoService) {

    @GetMapping("/filial/{filial}/cardcode/{cardCode}")
    fun getByFilial(@PathVariable filial : Int, @PathVariable cardCode : String): List<PaymentMethodDto>? {
        return service.getByFilial(filial,cardCode)
    }
}

