package com.omie.modules

import com.omie.idempotency.IdempotencyStore
import com.omie.idempotency.redis.RedisIdempotencyStore
import com.omie.idempotency.redis.redisConfig
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.util.AttributeKey
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

val IdempotencyStoreKey: AttributeKey<IdempotencyStore> = AttributeKey("IdempotencyStore")

val Application.idempotencyStore: IdempotencyStore
    get() = attributes[IdempotencyStoreKey]

fun Application.configureIdempotency() {
    val logger = LoggerFactory.getLogger("IdempotencyModule")

    val config = redisConfig()
    val store: IdempotencyStore = RedisIdempotencyStore(config)

    attributes.put(IdempotencyStoreKey, store)
    logger.info("IdempotencyStore registrado em Application.attributes")

    monitor.subscribe(ApplicationStopped) {
        logger.info("Application encerrando — fechando IdempotencyStore")
        runBlocking { store.close() }
    }
}