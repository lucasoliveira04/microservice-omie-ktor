package com.omie.idempotency.redis

import com.omie.idempotency.IdempotencyStore
import com.omie.invoice.dto.InvoiceDto
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.SetArgs
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.reactive.RedisReactiveCommands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

class RedisIdempotencyStore(
    private val config: RedisConfig
) : IdempotencyStore {

    private val logger = LoggerFactory.getLogger(RedisIdempotencyStore::class.java)

    companion object {
        private const val PROCESSING_MARKER = "__PROCESSING__"
    }
    private val client: RedisClient
    private val connection: StatefulRedisConnection<String, String>
    private val commands: RedisReactiveCommands<String, String>

    private val ttlSeconds: Long = config.ttlDays * 86400L

    // Abre conexão com o Redis
    init {
        try {
            val uri = RedisURI.builder()
                .withHost(config.host)
                .withPort(config.port)
                .withDatabase(config.database)
                .build()

            client = RedisClient.create(uri)
            connection = client.connect()
            commands = connection.reactive()

            logger.info(
                "Redis conectado em {}:{} (db={}, ttl={}d)",
                config.host, config.port, config.database, config.ttlDays
            )
        } catch (e: Exception) {
            throw IllegalStateException("Falha ao conectar no Redis", e)
        }
    }

    override suspend fun isProcessed(key: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val value = commands.get(key).awaitFirstOrNull()
            value != null && value != PROCESSING_MARKER
        } catch (e: Exception) {
            logger.error("Erro ao consultar chave '$key' no Redis", e)
            false
        }
    }

    override suspend fun markAsProcessed(key: String, value: String) = withContext(Dispatchers.IO) {
        try {
            val args = SetArgs.Builder.ex(ttlSeconds)
            commands.set(key, value, args).awaitFirstOrNull()
            logger.debug("Chave marcada como processada: {}", key)
        } catch (e: Exception) {
            logger.error("Erro ao gravar chave '$key' no Redis", e)
        }
    }

    override suspend fun tryLock(key: String, ttlSeconds: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val args = SetArgs.Builder.nx().ex(ttlSeconds)
            val result = commands.set(key, PROCESSING_MARKER, args).awaitFirstOrNull()
            result == "OK"
        } catch (e: Exception) {
            logger.error("Erro ao adquirir lock '$key' no Redis", e)
            false
        }
    }

    override suspend fun releaseLock(key: String) = withContext(Dispatchers.IO) {
        try {
            commands.del(key).awaitFirstOrNull()
            logger.debug("Lock liberado: {}", key)
        } catch (e: Exception) {
            logger.error("Erro ao liberar lock '$key' no Redis", e)
        }
        Unit
    }

    override suspend fun liberarLocks(faturas: List<InvoiceDto>) {
        faturas.forEach { fatura ->
            releaseLock(fatura.codigoLancamentoIntegracao)
        }
    }

    override suspend fun close() {
        try {
            connection.close()
            client.close()
            logger.info("RedisIdempotencyStore encerrado")
        } catch (e: Exception) {
            logger.error("Erro ao fechar RedisIdempotencyStore", e)
        }
    }
}