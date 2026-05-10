package com.omie.idempotency.redis

data class RedisConfig(
    val host: String,
    val port: Int,
    val database: Int,
    val ttlDays: Long
)