package br.andrew.sap.services

import br.andrew.sap.model.SalesTaxCode
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SalesTaxCodeService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<SalesTaxCode>(env, restTemplate, authService) {


    override fun path(): String {
        return "/b1s/v1/SalesTaxCodes"
    }
}