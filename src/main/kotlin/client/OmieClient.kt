package com.omie.client

import com.omie.client.omie.dto.CreateInvoiceRequest
import com.omie.client.omie.dto.CreateInvoiceResponse

interface OmieClient {
    suspend fun createInvoice(request: CreateInvoiceRequest) : CreateInvoiceResponse
}