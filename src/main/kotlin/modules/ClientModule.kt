package com.omie.modules

import com.omie.client.OmieClient
import com.omie.client.OmieClientImpl
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.util.*
import org.slf4j.LoggerFactory

fun Application.configureClients() {

    val logger = LoggerFactory.getLogger("ClientModule")

    logger.info("Iniciando configuração dos clients HTTP")

    val omieUrl = environment.config
        .property("api.uri")
        .getString()

    logger.info("URL da API configurada: {}", omieUrl)

    val httpClient = HttpClient()

    logger.info("HttpClient criado")

    val omieClient: OmieClient =
        OmieClientImpl(
            httpClient = httpClient,
            omieUrl = omieUrl
        )

    logger.info("OmieClientImpl inicializado")

    attributes.put(
        AttributeKey("omieClient"),
        omieClient
    )

    logger.info("OmieClient registrado em Application.attributes")

    monitor.subscribe(ApplicationStopped) {
        logger.info("Encerrando HttpClient")
        httpClient.close()
    }
}