package com.omie.invoice.dto.event

import com.omie.invoice.dto.InvoiceDto
import kotlinx.serialization.Serializable

@Serializable
data class FaturaErroEvent(
    val faturaId: String,
    val codigoLancamentoIntegracao: String,
    val correlationId: String,
    val loteId: String,
    val faultcode: String,
    val faultstring: String,
    val tipoErro: String,
    val payloadOriginal: InvoiceDto,
    val ocorridoEm: String
)