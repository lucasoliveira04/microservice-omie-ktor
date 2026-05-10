package com.omie.omie

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig

data class OmieConfig(
    val url: String,
    val appKey: String,
    val appSecret: String
)

fun ApplicationConfig.toOmieConfig(): OmieConfig {
    val omie = config("omie")
    return OmieConfig(
        url = omie.property("url").getString(),
        appKey = omie.property("appKey").getString(),
        appSecret = omie.property("appSecret").getString()
    )
}

fun Application.omieConfig(): OmieConfig =
    environment.config.toOmieConfig()