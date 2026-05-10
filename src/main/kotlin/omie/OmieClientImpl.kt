package com.omie.omie

import com.omie.omie.dto.request.IncluirContaReceberLoteParam
import com.omie.omie.dto.request.OmieRequest
import com.omie.omie.dto.response.IncluirContaReceberLoteResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class OmieClientImpl(
    private val httpClient: HttpClient,
    private val omieConfig: OmieConfig
) : OmieClient {

    override suspend fun incluirContaReceberLote(param: IncluirContaReceberLoteParam): IncluirContaReceberLoteResponse {
        val request = OmieRequest(
            call = OmieCalls.INCLUIR_CONTA_RECEBER_POR_LOTE,
            appKey = omieConfig.appKey,
            appSecret = omieConfig.appSecret,
            param = listOf(param)
        )

        return httpClient.post(omieConfig.url) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}