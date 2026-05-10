package com.omie.omie.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContaReceberCadastro(
    @SerialName("codigo_lancamento_integracao")
    val codigoLancamentoIntegracao: String,

    @SerialName("codigo_cliente_fornecedor")
    val codigoClienteFornecedor: Long,

    @SerialName("data_vencimento")
    val dataVencimento: String,

    @SerialName("valor_documento")
    val valorDocumento: Double,

    @SerialName("codigo_categoria")
    val codigoCategoria: String,

    @SerialName("data_previsao")
    val dataPrevisao: String,

    @SerialName("id_conta_corrente")
    val idContaCorrente: Long
)
