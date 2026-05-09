package com.omie.broker.amqp

data class RabbitConfig(
    val connection: ConnectionConfig,
    val queues: QueuesConfig,
    val consumer: ConsumerConfig
) {
    data class ConnectionConfig(
        val host: String,
        val port: Int,
        val username: String,
        val password: String,
        val virtualHost: String
    )

    data class QueuesConfig(
        val input: String,
        val success: String,
        val error: String
    )

    data class ConsumerConfig(
        val prefetch: Int
    )
}
