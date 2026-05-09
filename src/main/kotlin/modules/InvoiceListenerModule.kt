package com.omie.modules

import com.omie.broker.amqp.rabbitConfig
import com.omie.invoice.InvoiceBatchListener
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

fun Application.configureInvoiceListener() {
    val logger = LoggerFactory.getLogger("InvoiceListenerModule")

    val listenerScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("invoice-listener"))

    val broker = messageBroker
    val inputQueue = rabbitConfig().queues.input

    val listener = InvoiceBatchListener(broker, inputQueue)

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