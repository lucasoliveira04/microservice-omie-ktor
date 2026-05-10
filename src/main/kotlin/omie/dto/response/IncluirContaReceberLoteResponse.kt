package com.omie.omie.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncluirContaReceberLoteResponse(
    val lote: Int,
    @SerialName("codigo_status")
    val codigoStatus: String,
    @SerialName("descricao_status")
    val descricaoStatus: String,
    @SerialName("status_lote")
    val statusLote: List<StatusLoteItem>
)