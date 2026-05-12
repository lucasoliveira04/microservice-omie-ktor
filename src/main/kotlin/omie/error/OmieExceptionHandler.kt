package com.omie.omie.error

object OmieExceptionHandler {
    fun shouldSendToDlq(tipo: OmieErroTipo): Boolean {
        return when (tipo) {
            OmieErroTipo.CREDENCIAL_INVALIDA -> true
            OmieErroTipo.CONSUMO_REDUNDANTE -> true
            else -> false
        }
    }
}