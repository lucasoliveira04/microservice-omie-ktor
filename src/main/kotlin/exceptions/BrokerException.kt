package com.omie.exceptions

class BrokerException(
    message: String,
    causer: Throwable? = null
) : RuntimeException(message, causer) {}