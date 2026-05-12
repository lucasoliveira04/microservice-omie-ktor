package com.omie.omie.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmieErrorResponse(
    @SerialName("faultstring")
    val faultstring: String,
    @SerialName("faultcode")
    val faultcode: String
) {
}
