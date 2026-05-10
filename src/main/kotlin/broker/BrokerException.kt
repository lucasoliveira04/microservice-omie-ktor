package com.omie.broker

class BrokerException(
    message: String,
    causer: Throwable? = null
) : RuntimeException(message, causer) {}