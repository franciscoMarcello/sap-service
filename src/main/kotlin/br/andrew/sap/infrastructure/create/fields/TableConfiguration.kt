package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.*
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
class TableConfiguration(userFieldsMDService: UserFieldsMDService,
                         tableService: UserTablesMDService) {

    init {
        tableService.findOrCreate(TableMd(
            "COMISSAO","Tabela com regras de comissao",TbType.bott_MasterData
        ))

        listOf(
            FieldMd("porcentagem","comissão em porcentagem","@COMISSAO",DbType.db_Float),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            FieldMd("desconto","Desconto (%) do vendedor","@COMISSAO",DbType.db_Float),
        ).forEach { userFieldsMDService.findOrCreate(it) }


        listOf(
            FieldMd("tipoComissao","Selecionar Comissao","OPLN",DbType.db_Alpha)
                .also { it.linkedUDO = "comissao" },
        ).forEach { userFieldsMDService.findOrCreate(it) }
    }
}