package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class Document(val CardCode : String,
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                    val DocDueDate : String?,
                    val DocumentLines : List<Product>,
                    private val BPL_IDAssignedToInvoice : String) {

    var comments: String? = null
    var docDate :String? = null
    var salesPersonCode: Int = -1
    var paymentGroupCode: String? = null
    var docEntry : Int? = null
    var docNum : String? = null
    var paymentMethod : String? = null
    var discountPercent : Double = 0.0
    var COGSCostingCode : String? = null
    var COGSCostingCode2 : String? = null
    var ControlAccount : String? = null
    var documentInstallments : List<Installment>? = null
    var journalMemo : String? = null
    var u_pedido_update : String? = "0";
    var u_id_pedido_forca: String? = null


    var documentAdditionalExpenses : List<AdditionalExpenses> = emptyList()
    var frete: Double? = null
        set(valor){

        field = valor
    }

    @JsonProperty("BPL_IDAssignedToInvoice")
    fun getBPL_IDAssignedToInvoice(): String {
        return BPL_IDAssignedToInvoice;
    }

    fun productsByTax(): Map<String, List<Product>> {
        return this.DocumentLines
                .filter { it.taxCode != null && it.taxCode!!.isNotEmpty() }
                .groupBy { it.taxCode!! }
    }

}

