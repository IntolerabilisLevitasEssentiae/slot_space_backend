package com.slotspace

import com.slotspace.models.Token
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.TypeInfo

fun Application.configureRouting() {
    install(Authentication) {
        bearer("auth-bearer") {
            authenticate { tokenCredential ->
                if (tokenCredential.token.contains("1234")) {
                    UserIdPrincipal("Ok")
                } else {
                    null
                }
            }
        }
    }

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        post("/auth/anonymous") {
            call.respond(
                HttpStatusCode.OK,
                message = Token(
                    access = "access",
                    refresh = "refresh"
                ),
            )
        }
        authenticate("auth-bearer") {
            get("/auth1") {
                call.respondText("${call.principal<UserIdPrincipal>()?.name}")
            }
        }
    }
}