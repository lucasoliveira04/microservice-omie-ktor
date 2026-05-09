package com.omie.client

import com.omie.client.omie.dto.CreateInvoiceRequest
import com.omie.client.omie.dto.CreateInvoiceResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class OmieClientImpl(
    private val httpClient: HttpClient,
    private val omieUrl: String
) : OmieClient {

    override suspend fun createInvoice(request: CreateInvoiceRequest): CreateInvoiceResponse {
        return httpClient.post("$omieUrl/invoices") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}