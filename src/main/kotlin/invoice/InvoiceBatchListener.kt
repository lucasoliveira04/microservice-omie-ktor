package com.omie.invoice

import com.omie.broker.MessageBroker
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class InvoiceBatchListener(
    private val broker: MessageBroker,
    private val inputQueue: String
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
        val message = try {
            json.decodeFromString<InvoiceBatchMessage>(payload)
        } catch (e: Exception) {
            logger.error("Payload mal formado, enviando pra DLQ. Payload bruto: $payload", e)
            throw e
        }

        logger.info(
            "Lote recebido: loteId=${message.loteId}, " +
                    "faturas=${message.faturas.size}, ids=${message.faturas.map { it.id }}"
        )
    }
}