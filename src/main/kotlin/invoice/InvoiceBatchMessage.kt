package com.omie.invoice

import kotlinx.serialization.Serializable

@Serializable
data class InvoiceBatchMessage(
    val numeroLote: Int,
    val loteId: String,
    val correlationId: String,
    val dataCriacao: String,
    val origem: String,
    val faturas: List<FaturaMessage>
)

@Serializable
data class FaturaMessage(
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