package br.andrew.sap.model

import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.self.vendafutura.ContratoParse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.Exception

class ContratoParseTest {

    @Test
    fun verificaValorContrato(){
        val produtos = listOf(Product("windson","100","1.7732",10)
            .also {
                it.U_preco_negociado = 1.7732
                it.ItemDescription = "Windson"
                it.U_preco_base = 1.77
                it.DiscountPercent = 0.0
                it.CommisionPercent = 0.0
                it.MeasureUnit = "windson"
            })
        val document = Invoice("","",produtos,"2").also {
            it.docEntry = 666
            it.cardName = "windson"
        }
        val contrato = ContratoParse.parse(document)
        Assertions.assertEquals(666,contrato.U_orderDocEntry)
        Assertions.assertEquals("177.32",contrato.total().toString())
        Assertions.assertEquals(0.0,contrato.U_valorFrete)
    }

    @Test
    fun testaValorFrete(){
        val produtos = listOf(Product("windson","100","1.7732",10)
            .also {
                it.U_preco_negociado = 1.7732
                it.ItemDescription = "Windson"
                it.U_preco_base = 1.77
                it.DiscountPercent = 0.0
                it.CommisionPercent = 0.0
                it.MeasureUnit = "windson"
            })
        val document = Invoice("","",produtos,"2").also {
            it.docEntry = 666
            it.cardName = "windson"
            it.documentAdditionalExpenses = mutableListOf(AdditionalExpenses(1,100.00))
        }
        val contrato = ContratoParse.parse(document)
        Assertions.assertEquals(666,contrato.U_orderDocEntry)
        Assertions.assertEquals("277.32",contrato.total().toString())
        Assertions.assertEquals(100.00,contrato.U_valorFrete)
    }

    @Test
    fun valorNegociadoComErro(){
        val produtos = listOf(Product("windson","100","1.7732",10)
            .also {
                it.U_preco_negociado = 0.0
                it.ItemDescription = "Windson"
                it.U_preco_base = 1.77
                it.DiscountPercent = 0.0
                it.CommisionPercent = 0.0
                it.MeasureUnit = "windson"
            })
        val document = Invoice("","",produtos,"2").also {
            it.docEntry = 666
        }
        val erro = assertThrows<Exception> {
            ContratoParse.parse(document)
        }
    }
}