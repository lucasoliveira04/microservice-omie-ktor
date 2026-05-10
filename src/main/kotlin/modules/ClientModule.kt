package com.omie.modules

import com.omie.omie.OmieClient
import com.omie.omie.OmieClientImpl
import com.omie.omie.HttpClientFactory
import com.omie.omie.omieConfig
import io.ktor.server.application.*
import io.ktor.util.AttributeKey
import org.slf4j.LoggerFactory

val OmieClientKey: AttributeKey<OmieClient> = AttributeKey("OmieClient")

val Application.omieClient: OmieClient
    get() = attributes[OmieClientKey]
fun Application.configureClients() {

    val logger = LoggerFactory.getLogger("ClientModule")

    val omieConfig = omieConfig()
    val httpClient = HttpClientFactory.create()
    val omieClient: OmieClient = OmieClientImpl(httpClient, omieConfig)

    attributes.put(OmieClientKey, omieClient)
    logger.info("OmieClient configurado (url=${omieConfig.url})")

    monitor.subscribe(ApplicationStopped) {
        logger.info("Encerrando HttpClient")
        httpClient.close()
    }
}