package com.slotspace

import io.ktor.server.engine.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.mainShutdown() {
    install(ShutDownUrl.ApplicationCallPlugin) {
        shutDownUrl = "/shutdown"
        exitCodeSupplier = { 0 }
    }
}
