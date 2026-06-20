package com.slotspace.di

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.networkModule() {
    dependencies {
        provide<HttpClient> {
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(
//                        Json {
//                            serializersModule = SerializersModule {
//                                polymorphic()
//                            }
//                        }
                    )
                }
            }
        }
    }
}
