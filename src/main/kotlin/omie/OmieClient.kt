package com.omie.omie

import com.omie.omie.dto.request.IncluirContaReceberLoteParam
import com.omie.omie.dto.response.IncluirContaReceberLoteResponse

interface OmieClient {
    suspend fun incluirContaReceberLote(param: IncluirContaReceberLoteParam) : IncluirContaReceberLoteResponse
}