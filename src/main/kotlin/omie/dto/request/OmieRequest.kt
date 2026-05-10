package com.omie.omie.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmieRequest<T>(
    val call: String,

    @SerialName("app_key")
    val appKey: String,

    @SerialName("app_secret")
    val appSecret: String,
    val param: List<T>,
)
