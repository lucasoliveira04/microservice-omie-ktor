package com.omie.omie.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncluirContaReceberLoteParam(
    val lote: Int,
    @SerialName("conta_receber_cadastro")
    val contaReceberCadastro: List<ContaReceberCadastro>
)
