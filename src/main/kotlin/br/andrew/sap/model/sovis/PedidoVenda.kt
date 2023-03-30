package br.andrew.sap.model.sovis

import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

class PedidoVenda(
        val idCliente : String,
        val idEmpresa : String,
        val idFormaPagamento: String,
        val idCondicaoPagamento : String,
        @JsonFormat(with = arrayOf(JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY))
        val produtos : List<Produto>,
        val codVendedor : String) {

    @JsonIgnore
    fun getOrder(): OrderSales {
        return OrderSales(idCliente, Date(),produtos.map { it.getProduct() },idEmpresa,"62")
    }
}

