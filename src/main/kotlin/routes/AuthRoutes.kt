package com.slotspace.routes

import com.slotspace.models.Token
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post

fun Routing.configureAuth() {
    post("/auth/anonymous") {
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
    authenticate("auth-bearer", build = build)
}
