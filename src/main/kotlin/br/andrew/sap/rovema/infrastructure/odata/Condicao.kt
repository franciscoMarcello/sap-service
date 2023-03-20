package br.andrew.sap.rovema.infrastructure.odata

enum class Condicao(val value: String) {
    GREAT_EQUAL("ge"),
    GREAT("gt"),
    EQUAL("eq"),
    STARTS_WITH("startswith"),
    IN("in");


    fun get(coluna : String, value : String) : String{
        return when(this) {
            STARTS_WITH -> "startswith(${coluna}, ${value})"
            EQUAL -> "${coluna} eq ${value}"
            else -> "Tipo de dado é invalido para a condição"
        }
    }

    fun get(coluna: String, value: List<*>): String {
        return when(this) {
            IN -> {
                value.map { EQUAL.get(coluna,"'${it}'") }
                        .joinToString(" or ")
            }
            else -> "Tipo de dado é invalido para a condição"
        }
    }

}