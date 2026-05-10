package com.omie.idempotency.redis

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig

fun ApplicationConfig.toRedisConfig(): RedisConfig {
    val redis = config("redis")
    return RedisConfig(
        host = redis.property("host").getString(),
        port = redis.property("port").getString().toInt(),
        database = redis.property("database").getString().toInt(),
        ttlDays = redis.property("ttlDays").getString().toLong()
    )
}

fun Application.redisConfig(): RedisConfig =
    environment.config.toRedisConfig()