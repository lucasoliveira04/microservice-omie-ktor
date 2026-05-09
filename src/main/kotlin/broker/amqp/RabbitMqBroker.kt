package com.omie.broker.amqp

import com.omie.broker.MessageBroker
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.MessageProperties
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

class RabbitMqBroker(
    private val config : RabbitConfig
) : MessageBroker {

    private val logger = LoggerFactory.getLogger(RabbitMqBroker::class.java)

    private val connection : Connection
    private val channel : Channel

    // Scope dedicado pros consumers. Cancelar = parar todos os consumers.
    // SupervisorJob: falha em uma coroutine não derruba as outras.
    private val consumerScope = CoroutineScope(
        Dispatchers.IO + SupervisorJob() + CoroutineName("rabbitmq-consumer")
    )

    init {
        val factory = ConnectionFactory().apply {
            host = config.connection.host
            port = config.connection.port
            username = config.connection.username
            password = config.connection.password
            virtualHost = config.connection.virtualHost
            // Recovery automático se a conexão cair
            isAutomaticRecoveryEnabled = true
            isTopologyRecoveryEnabled = true
        }

        try {
            connection = factory.newConnection("omie-service")
            channel = connection.createChannel()
            channel.basicQos(config.consumer.prefetch)
            logger.info("RabbitMQ conectado em ${config.connection.host}:${config.connection.port}")
        } catch (e: Exception) {
            throw IllegalStateException("Falha ao conectar no RabbitMQ", e)
        }
    }

    private fun declareQueueWithDlq(queueName: String) {
        val dlxName = "$queueName.dlx"
        val dlqName = "$queueName.dlq"

        channel.exchangeDeclare(dlxName, BuiltinExchangeType.DIRECT, true)
        channel.queueDeclare(dlqName, true, false, false, null)
        channel.queueBind(dlqName, dlxName, queueName)

        val args = mapOf<String, Any>(
            "x-dead-letter-exchange" to dlxName,
            "x-dead-letter-routing-key" to dlqName
        )

        channel.queueDeclare(queueName, true, false, false, args)
    }

    override suspend fun publish(destination: String, payload: String) {
        withContext(Dispatchers.IO) {
            try {
                declareQueueWithDlq(destination)
                channel.basicPublish(
                    "",
                    destination,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    payload.toByteArray(Charsets.UTF_8),
                )

                logger.debug("Publicado em '$destination': ${payload.take(100)}")
            } catch (e: Exception) {
                throw IllegalStateException("Falha ao conectar no publicado em $destination", e)
            }
        }
    }

    override suspend fun consume(source: String, handler: suspend (payload: String) -> Unit) {
        withContext(Dispatchers.IO) {
            declareQueueWithDlq(source)
        }

        val deliverCallback = DeliverCallback { _, delivery ->
            val deliveryTag = delivery.envelope.deliveryTag
            val payload = String(delivery.body, Charsets.UTF_8)

            consumerScope.launch {
                try {
                    handler(payload)
                    channel.basicAck(deliveryTag, false)
                    logger.debug("ACK tag=$deliveryTag")
                } catch (e: Exception) {
                    logger.error("Falha ao processar tag=$deliveryTag, enviando pra DLQ", e)
                    channel.basicNack(deliveryTag, false, false)
                }
            }
        }

        val cancelCallback = CancelCallback { consumerTag ->
            logger.warn("Consumer cancelado pelo Broker: $consumerTag")
        }

        withContext(Dispatchers.IO) {
            channel.basicConsume(
                source,
                false,
                deliverCallback,
                cancelCallback
            )
        }

        logger.info("Consumer ativo na fila '$source'")

        // Suspende até o scope ser cancelado
        awaitCancellation()
    }

    override suspend fun close() {
        logger.info("Encerrando RabbitMqBroker")
        try {
            consumerScope.cancel()
            withContext(Dispatchers.IO) {
                if (channel.isOpen) channel.close()
                if (connection.isOpen) connection.close()
            }

            logger.info("RabbitMqBroker encerrado")
        } catch (e: Exception) {
            logger.error("Falha ao processar no RabbitMQ", e)
        }
    }
}