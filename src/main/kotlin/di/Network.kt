package com.slotspace.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.network() {
    dependencies {
        provide<HttpClient> { HttpClient(CIO) }
    }
}
