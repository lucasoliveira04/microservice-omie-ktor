package com.omie.idempotency

import com.omie.invoice.dto.InvoiceDto

interface IdempotencyStore {
    suspend fun isProcessed(key: String): Boolean
    suspend fun tryLock(key: String, ttlSeconds: Long): Boolean
    suspend fun markAsProcessed(key: String, value: String)
    suspend fun releaseLock(key: String)
    suspend fun liberarLocks(faturas: List<InvoiceDto>)
    suspend fun close()
}