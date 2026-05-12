package com.omie.omie.error

enum class OmieErroTipo(val codes: Set<Int>) {
    JA_CADASTRADO(setOf(102)),
    CLIENTE_NAO_ENCONTRADO(setOf(1035)),
    CONTA_CORRENTE_NAO_ENCONTRADA(setOf(1140)),
    LOTE_EXCEDE_LIMITE(setOf(1003)),
    CONSUMO_REDUNDANTE(setOf(6)),
    CREDENCIAL_INVALIDA(emptySet()),
    DESCONHECIDO(emptySet());

    companion object {
        private val byCode: Map<Int, OmieErroTipo> =
            entries.flatMap { tipo -> tipo.codes.map { code -> code to tipo } }.toMap()

        fun fromCode(code: Int?): OmieErroTipo =
            if (code != null) byCode[code] ?: DESCONHECIDO else DESCONHECIDO

        fun fromFaultcode(faultcode: String): OmieErroTipo {
            if (faultcode == "SOAP-ENV:Server") return CREDENCIAL_INVALIDA
            val code = faultcode.substringAfterLast("-").toIntOrNull()
            return fromCode(code)
        }
    }
}