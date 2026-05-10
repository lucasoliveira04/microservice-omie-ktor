package com.omie.processing

import com.omie.idempotency.IdempotencyStore
import com.omie.invoice.dto.InvoiceDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory

class IdempotencyFilter(
    private val store: IdempotencyStore,
    private val lockTtlSeconds: Long = 60L
) {
    private val logger = LoggerFactory.getLogger(IdempotencyFilter::class.java)

    suspend fun filter(faturas: List<InvoiceDto>): FilterResult = coroutineScope {
        // Etapa 1 — consulta em paralelo "já foi processada?"
        val classificadas = faturas.map { fatura ->
            async {
                val key = fatura.codigoLancamentoIntegracao
                val processada = store.isProcessed(key)
                fatura to processada
            }
        }.awaitAll()

        val jaProcessadas = classificadas.filter { it.second }.map { it.first }
        val candidatas = classificadas.filterNot { it.second }.map { it.first }

        // Etapa 2 — tenta lock em paralelo nas candidatas
        val locks = candidatas.map { fatura ->
            async {
                val key = fatura.codigoLancamentoIntegracao
                val conseguiu = store.tryLock(key, lockTtlSeconds)
                fatura to conseguiu
            }
        }.awaitAll()

        val novas = locks.filter { it.second }.map { it.first }
        val emProcessamento = locks.filterNot { it.second }.map { it.first }

        if (jaProcessadas.isNotEmpty()) {
            logger.info(
                "Faturas já processadas (puladas): qtd={}, ids={}",
                jaProcessadas.size,
                jaProcessadas.map { it.codigoLancamentoIntegracao }
            )
        }

        if (emProcessamento.isNotEmpty()) {
            logger.warn(
                "Faturas em processamento por outra instância (puladas): qtd={}, ids={}",
                emProcessamento.size,
                emProcessamento.map { it.codigoLancamentoIntegracao }
            )
        }

        FilterResult(
            novas = novas,
            jaProcessadas = jaProcessadas,
            emProcessamento = emProcessamento
        )
    }
}

data class FilterResult(
    val novas: List<InvoiceDto>,
    val jaProcessadas: List<InvoiceDto>,
    val emProcessamento: List<InvoiceDto>
)