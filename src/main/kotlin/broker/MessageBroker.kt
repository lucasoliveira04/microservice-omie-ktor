package com.omie.broker

interface MessageBroker {
    suspend fun publish(destination: String, payload: String)
    suspend fun consume(source: String, handler: suspend (payload: String) -> Unit)
    suspend fun close()
}