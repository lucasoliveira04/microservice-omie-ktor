package com.omie.idempotency.dto

import kotlinx.serialization.Serializable

@Serializable
data class IdempotencyPayload(
    val loteId: String,
    val codigoOmie: Long?,
    val processedAt: String
)
