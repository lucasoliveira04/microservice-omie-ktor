package com.omie.processing

import com.omie.idempotency.IdempotencyStore
import com.omie.idempotency.dto.IdempotencyPayload
import com.omie.omie.OmieClient
import com.omie.invoice.dto.BatchDto
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant

class BatchProcessor(
    private val mapper: OmieRequestMapper,
    private val omieClient: OmieClient,
    private val idempotencyFilter: IdempotencyFilter,
    private val idempotencyStore: IdempotencyStore
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


        val batchSomenteNovas = batch.copy(faturas = filterResult.novas)
        val param = mapper.toIncluirContaReceberLoteParam(batchSomenteNovas)

        val response = try {
            omieClient.incluirContaReceberLote(param)
        } catch (e: Exception) {
            logger.error(
                "Falha na chamada OMIE: loteId={}, motivo={}",
                batch.loteId, e.message, e
            )
            idempotencyStore.liberarLocks(filterResult.novas)
            throw e
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
            logger.info(
                "  ✓ {} → codigoOmie={}",
                item.codigoLancamentoIntegracao, item.codigoLancamentoOmie
            )
        }

        erros.forEach { item ->
            idempotencyStore.releaseLock(item.codigoLancamentoIntegracao)
            logger.warn(
                "  ✗ {} → status={}, descricao={}",
                item.codigoLancamentoIntegracao, item.codigoStatus, item.descricaoStatus
            )
        }
    }
}