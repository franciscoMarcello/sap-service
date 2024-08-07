package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


/*
create construte base on json
{
      "SalesEmployeeCode": -1,
      "SalesEmployeeName": "-Nenhum vendedor-",
      "Remarks": null,
      "CommissionForSalesEmployee": 0.0,
      "CommissionGroup": 0,
      "Locked": "tYES",
      "EmployeeID": null,
      "Active": "tYES",
      "Telephone": null,
      "Mobile": null,
      "Fax": null,
      "Email": null,
      "U_Integracao_sovis": "1",
      "U_filial": null,
      "U_envia_relatorio": "1"
    }
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class SalePerson(val SalesEmployeeCode: Int,
                 val SalesEmployeeName : String,
                 private val Email: String?,
                 val U_Integracao_sovis: String,
                 val U_filial: String?,
                 val U_envia_relatorio: String,
                 val Active: String) {

    var u_password: String? = null


    @JsonIgnore
    fun getEmailAddress(): String {
        if(this.SalesEmployeeCode == -1 || Email == null)
            return emailDefault
        else
            return this.Email
    }

    companion object{
        @JsonIgnore
        var emailDefault : String = "";
    }
}

