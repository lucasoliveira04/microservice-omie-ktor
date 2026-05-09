package com.omie.modules

import com.omie.broker.MessageBroker
import com.omie.broker.amqp.RabbitMqBroker
import com.omie.broker.amqp.rabbitConfig
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.util.AttributeKey
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
val MessageBrokerKey: AttributeKey<MessageBroker> = AttributeKey("MessageBroker")
val Application.messageBroker: MessageBroker
    get() = attributes[MessageBrokerKey]

fun Application.configureMessaging() {
    val logger = LoggerFactory.getLogger("MessagingModule")

    val config = rabbitConfig()
    val broker = RabbitMqBroker(config)

    attributes.put(MessageBrokerKey, broker)
    logger.info("Message registrado em Application.attributes")

    monitor.subscribe(ApplicationStopped) { application ->
        logger.info("Application encerrando - fechando broker")
        runBlocking { broker.close() }
    }
}