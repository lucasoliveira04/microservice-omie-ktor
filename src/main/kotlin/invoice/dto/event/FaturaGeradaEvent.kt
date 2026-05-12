package com.omie.invoice.dto.event

import com.omie.invoice.dto.InvoiceDto
import kotlinx.serialization.Serializable

@Serializable
data class FaturaGeradaEvent(
    val faturaId: String,
    val codigoLancamentoIntegracao: String,
    val codigoLancamentoOmie: Long,
    val correlationId: String,
    val loteId: String,
    val payloadOriginal: InvoiceDto,
    val ocorridoEm: String
)