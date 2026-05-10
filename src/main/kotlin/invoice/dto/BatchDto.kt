package com.omie.invoice.dto

import kotlinx.serialization.Serializable

@Serializable
data class BatchDto(
    val numeroLote: Int,
    val loteId: String,
    val correlationId: String,
    val dataCriacao: String,
    val origem: String,
    val faturas: List<InvoiceDto>
)
