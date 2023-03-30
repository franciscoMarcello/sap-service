package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class OrderSales(CardCode: String,
                 @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                 DocDueDate: Date,
                 DocumentLines: List<Product>,
                 BPL_IDAssignedToInvoice: String,
                 Usage: String) : Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice, Usage) {
}