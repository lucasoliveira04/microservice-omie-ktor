package com.omie.omie.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatusLoteItem(
    @SerialName("codigo_lancamento_integracao")
    val codigoLancamentoIntegracao: String,

    @SerialName("codigo_lancamento_omie")
    val codigoLancamentoOmie: Long? = null,

    @SerialName("codigo_status")
    val codigoStatus: String,

    @SerialName("descricao_status")
    val descricaoStatus: String
)
