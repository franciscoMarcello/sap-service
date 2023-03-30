package br.andrew.sap.model.sovis

import br.andrew.sap.model.documents.Product
import com.fasterxml.jackson.annotation.JsonIgnore

class Produto(val idProduto : String,
              val precoUnitario : Double,
              val quantidade : Double){

    @JsonIgnore
    fun getProduct(): Product {
        return Product(idProduto,quantidade.toString(),precoUnitario.toString())
    }

}