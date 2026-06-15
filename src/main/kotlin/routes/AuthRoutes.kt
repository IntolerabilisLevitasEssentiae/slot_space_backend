package com.slotspace.routes

import com.slotspace.models.Token
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post

fun Routing.configureAuth(client: HttpClient) {
    post("/auth/anonymous") {
//        val text = client.get("https://google.com").body<String>()  // todo: отправка запросов
//        println("$text")
        call.respond(
            HttpStatusCode.OK,
            message = Token(
                access = "access",
                refresh = "refresh"
            ),
        )
    }
}

fun Route.bearerAuthenticate(build: Route.() -> Unit) {
    authenticate(ABEARERNAME, build = build)
}

const val ABEARERNAME = "auth-bearer"
