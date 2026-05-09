package com.omie.client.omie.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateInvoiceRequest(
    val invoiceId: String,
    val value: Double
)