package br.andrew.sap.model.bank

import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.documents.Installment
import br.andrew.sap.model.documents.Invoice
import br.andrew.sap.model.uzzipay.Transaction
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class PaymentInvoice(val docEntry: Int, val sumApplied: Double, val invoiceType: PaymentType) {

    constructor(invoice: Invoice, transaction: Transaction, installment: Installment)
            : this(invoice.docEntry!!,transaction.receivedAmount!!, PaymentType.it_Invoice){
                this.InstallmentId = installment.InstallmentId
            }

    var InstallmentId : Int? = null
    fun duplicateFor(newNota: Document): PaymentInvoice {
        if(newNota.docEntry == null)
            throw Exception("Nao e possivel criar pagamento para um nota que nao tem referencia (docEntry is null)")
        return PaymentInvoice(newNota.docEntry!!, this.sumApplied, this.invoiceType)
    }
}