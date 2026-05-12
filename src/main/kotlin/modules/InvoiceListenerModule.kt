package com.omie.modules

import com.omie.broker.amqp.rabbitConfig
import com.omie.invoice.InvoiceBatchListener
import com.omie.processing.BatchProcessor
import com.omie.processing.BatchProcessorParams
import com.omie.processing.IdempotencyFilter
import com.omie.processing.OmieRequestMapper
import io.ktor.server.application.*
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

fun Application.configureInvoiceListener() {
    val logger = LoggerFactory.getLogger("InvoiceListenerModule")

    val listenerScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("invoice-listener"))

    val broker = messageBroker
    val inputQueue = rabbitConfig().queues.input

    val mapper = OmieRequestMapper()
    val filter = IdempotencyFilter(idempotencyStore)
    val processor = BatchProcessor(
        BatchProcessorParams(
            mapper = mapper,
            omieClient = omieClient,
            idempotencyFilter = filter,
            idempotencyStore = idempotencyStore,
            broker = broker,
            successQueue = rabbitConfig().queues.success,
            errorQueue = rabbitConfig().queues.error
        )
    )
    val listener = InvoiceBatchListener(broker, inputQueue, processor)

    monitor.subscribe(ApplicationStarted) {
        listenerScope.launch {
            try {
                listener.start()
            } catch (e: Exception) {
                logger.error("InvoiceBatchListener falhou", e)
            }
        }
        logger.info("InvoiceBatchListener agendado pra iniciar")
    }

    monitor.subscribe(ApplicationStopped) {
        logger.info("Encerrando InvoiceListener scope")
        listenerScope.cancel()
    }
}