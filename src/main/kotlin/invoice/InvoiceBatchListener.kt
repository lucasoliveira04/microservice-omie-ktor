package com.omie.invoice

import com.omie.broker.MessageBroker
import com.omie.invoice.dto.BatchDto
import com.omie.processing.BatchProcessor
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class InvoiceBatchListener(
    private val broker: MessageBroker,
    private val inputQueue: String,
    private val processor: BatchProcessor
) {
    private val logger = LoggerFactory.getLogger(InvoiceBatchListener::class.java)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun start() {
        logger.info("InvoiceBatchListener iniciando na fila : $inputQueue")

        broker.consume(inputQueue) { payload ->
            handleMessage(payload)
        }
    }

    private suspend fun handleMessage(payload: String) {
        val batch = try {
            json.decodeFromString<BatchDto>(payload)
        } catch (e: Exception) {
            logger.error("Payload mal formado, enviando pra DLQ. Payload bruto: $payload", e)
            throw e
        }

        logger.info(
            "Lote recebido: numeroLote={}, loteId={}, correlationId={}, " +
                    "origem={}, faturas={}",
            batch.numeroLote, batch.loteId, batch.correlationId,
            batch.origem, batch.faturas.size
        )

        processor.process(batch)
    }
}