package com.omie.invoice.mapper

import com.omie.invoice.dto.InvoiceDto
import com.omie.invoice.dto.event.FaturaErroEvent
import com.omie.invoice.dto.event.FaturaGeradaEvent
import com.omie.omie.dto.response.StatusLoteItem
import com.omie.omie.error.OmieErroTipo
import com.omie.omie.error.OmieException
import java.time.Instant

object FaturaEventMapper {

    fun toGeradaEvent(
        item: StatusLoteItem,
        invoice: InvoiceDto,
        correlationId: String,
        loteId: String
    ): FaturaGeradaEvent = FaturaGeradaEvent(
        faturaId = invoice.id,
        codigoLancamentoIntegracao = item.codigoLancamentoIntegracao,
        codigoLancamentoOmie = item.codigoLancamentoOmie!!,
        correlationId = correlationId,
        loteId = loteId,
        payloadOriginal = invoice,
        ocorridoEm = Instant.now().toString()
    )

    fun toErroEventFromException(
        exception: OmieException,
        invoice: InvoiceDto,
        correlationId: String,
        loteId: String
    ): FaturaErroEvent = FaturaErroEvent(
        faturaId = invoice.id,
        codigoLancamentoIntegracao = invoice.codigoLancamentoIntegracao,
        correlationId = correlationId,
        loteId = loteId,
        faultcode = exception.errorResponse.faultcode,
        faultstring = exception.errorResponse.faultstring,
        tipoErro = exception.tipo.name,
        payloadOriginal = invoice,
        ocorridoEm = Instant.now().toString()
    )
    fun toErroEvent(
        item: StatusLoteItem,
        invoice: InvoiceDto,
        correlationId: String,
        loteId: String
    ): FaturaErroEvent = FaturaErroEvent(
        faturaId = invoice.id,
        codigoLancamentoIntegracao = item.codigoLancamentoIntegracao,
        correlationId = correlationId,
        loteId = loteId,
        faultcode = item.codigoStatus,
        faultstring = item.descricaoStatus,
        tipoErro = OmieErroTipo.fromCode(item.codigoStatus.toIntOrNull()).name,
        payloadOriginal = invoice,
        ocorridoEm = Instant.now().toString()
    )
}