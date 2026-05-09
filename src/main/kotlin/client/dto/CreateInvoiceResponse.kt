package com.omie.client.omie.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateInvoiceResponse(
    val success: Boolean,
    val protocol: String
)