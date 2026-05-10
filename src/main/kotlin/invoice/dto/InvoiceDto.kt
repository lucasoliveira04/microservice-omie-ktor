package com.omie.invoice.dto

import kotlinx.serialization.Serializable

@Serializable
data class InvoiceDto(
    val id: String,
    val codigoLancamentoIntegracao: String,
    val codigoCliente: Long,
    val dataVencimento: String,
    val valor: Double,
    val codigoCategoria: String,
    val dataPrevisao: String,
    val idContaCorrente: Long,
    val keyFatura: String,
    val tipoCliente: String
)
