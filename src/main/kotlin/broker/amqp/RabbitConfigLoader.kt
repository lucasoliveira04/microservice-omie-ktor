package com.omie.broker.amqp

import io.ktor.server.application.*
import io.ktor.server.config.*

fun ApplicationConfig.toRabbitConfig(): RabbitConfig {
    val conn = config("rabbitmq.connection")
    val queues = config("rabbitmq.queues")
    val consumer = config("rabbitmq.consumer")

    return RabbitConfig(
        connection = RabbitConfig.ConnectionConfig(
            host = conn.property("host").getString(),
            port = conn.property("port").getString().toInt(),
            username = conn.property("username").getString(),
            password = conn.property("password").getString(),
            virtualHost = conn.property("virtualHost").getString()
        ),
        queues = RabbitConfig.QueuesConfig(
            input = queues.property("input").getString(),
            success = queues.property("success").getString(),
            error = queues.property("error").getString()
        ),
        consumer = RabbitConfig.ConsumerConfig(
            prefetch = consumer.property("prefetch").getString().toInt()
        )
    )
}

fun Application.rabbitConfig(): RabbitConfig = environment.config.toRabbitConfig()