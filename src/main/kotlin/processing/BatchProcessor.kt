package com.omie.processing

import com.omie.broker.MessageBroker
import com.omie.idempotency.IdempotencyStore
import com.omie.idempotency.dto.IdempotencyPayload
import com.omie.omie.OmieClient
import com.omie.invoice.dto.BatchDto
import com.omie.invoice.dto.event.FaturaErroEvent
import com.omie.invoice.dto.event.FaturaGeradaEvent
import com.omie.invoice.mapper.FaturaEventMapper
import com.omie.omie.error.OmieErroTipo
import com.omie.omie.error.OmieException
import com.omie.omie.error.OmieExceptionHandler
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant

class BatchProcessor(
    private val mapper: OmieRequestMapper,
    private val omieClient: OmieClient,
    private val idempotencyFilter: IdempotencyFilter,
    private val idempotencyStore: IdempotencyStore,
    private val broker: MessageBroker,
    private val errorQueue: String,
    private val successQueue: String,
) {
    private val logger = LoggerFactory.getLogger(BatchProcessor::class.java)
    private val json = Json { ignoreUnknownKeys = true }
    suspend fun process(batch: BatchDto) {
        logger.info(
            "Processando lote: loteId={}, correlationId={}, faturas={}",
            batch.loteId, batch.correlationId, batch.faturas.size
        )

        val filterResult = idempotencyFilter.filter(batch.faturas)

        if (filterResult.novas.isEmpty()) {
            logger.info(
                "Nada novo pra processar: loteId={}, jaProcessadas={}, emProcessamento={}",
                batch.loteId,
                filterResult.jaProcessadas.size,
                filterResult.emProcessamento.size
            )
            return
        }

        val invoicePorCodigo = filterResult.novas.associateBy { it.codigoLancamentoIntegracao }

        val batchSomenteNovas = batch.copy(faturas = filterResult.novas)
        val param = mapper.toIncluirContaReceberLoteParam(batchSomenteNovas)

        val response = try {
            omieClient.incluirContaReceberLote(param)
        } catch (e: OmieException) {
            idempotencyStore.liberarLocks(filterResult.novas)
            if (OmieExceptionHandler.shouldSendToDlq(e.tipo)) throw e

            filterResult.novas.forEach { invoice ->
                val event = FaturaEventMapper.toErroEventFromException(
                    exception = e,
                    invoice = invoice,
                    correlationId = batch.correlationId,
                    loteId = batch.loteId
                )
                broker.publish(errorQueue, json.encodeToString(FaturaErroEvent.serializer(), event))
                logger.warn("  ✗ {} → tipo={}, motivo={}", invoice.codigoLancamentoIntegracao, e.tipo, e.message)
            }
            return
        }

        // Análise do lote inteiro
        if (response.codigoStatus != "0") {
            logger.warn(
                "Lote rejeitado pela OMIE: loteId={}, codigoStatus={}, descricao={}",
                batch.loteId, response.codigoStatus, response.descricaoStatus
            )
            idempotencyStore.liberarLocks(filterResult.novas)
            return
        }

        val sucessos = response.statusLote.filter { it.codigoStatus == "0" }
        val erros = response.statusLote.filter { it.codigoStatus != "0" }

        logger.info(
            "Lote processado: loteId={}, sucessos={}, erros={}",
            batch.loteId, sucessos.size, erros.size
        )

        sucessos.forEach { item ->
            val payload = IdempotencyPayload(
                loteId = batch.loteId,
                codigoOmie = item.codigoLancamentoOmie,
                processedAt = Instant.now().toString()
            )
            idempotencyStore.markAsProcessed(
                key = item.codigoLancamentoIntegracao,
                value = json.encodeToString(IdempotencyPayload.serializer(), payload)
            )

            val invoice = invoicePorCodigo[item.codigoLancamentoIntegracao]

            if (invoice == null) {
                logger.warn("Invoice não encontrada para correlacionar sucesso: {}", item.codigoLancamentoIntegracao)
                return@forEach
            }

            val event = FaturaEventMapper.toGeradaEvent(
                item = item,
                invoice = invoice,
                correlationId = batch.correlationId,
                loteId = batch.loteId
            )

            broker.publish(successQueue, json.encodeToString(FaturaGeradaEvent.serializer(), event))

            logger.info(
                "  ✓ {} → codigoOmie={}",
                item.codigoLancamentoIntegracao, item.codigoLancamentoOmie
            )
        }

        erros.forEach { item ->
            idempotencyStore.releaseLock(item.codigoLancamentoIntegracao)

            val invoice = invoicePorCodigo[item.codigoLancamentoIntegracao]

            if (invoice == null) {
                logger.warn("Invoice não encontrada para correlacionar erro: {}", item.codigoLancamentoIntegracao)
                return@forEach
            }

            val event = FaturaEventMapper.toErroEvent(
                item = item,
                invoice = invoice,
                correlationId = batch.correlationId,
                loteId = batch.loteId
            )

            broker.publish(
                errorQueue,
                json.encodeToString(FaturaErroEvent.serializer(),
                    event))

            logger.warn(
                "  ✗ {} → status={}, descricao={}",
                item.codigoLancamentoIntegracao, item.codigoStatus, item.descricaoStatus
            )
        }
    }
}