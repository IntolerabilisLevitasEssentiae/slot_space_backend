package com.slotspace

import com.slotspace.models.Token
import com.slotspace.routes.ABEARERNAME
import com.slotspace.routes.bearerAuthenticate
import com.slotspace.routes.configureAuth
import io.ktor.client.HttpClient
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.TypeInfo

fun Application.configureRouting(client: HttpClient) {
    install(Authentication) {
        bearer(ABEARERNAME) {
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
        // отдача файлов напрямую из resources/static
        staticResources("/static", "static")

//        get("/") { // todo: delete. Это базовый пример
//            call.respondText("Hello, World!")
//        }
//        get("/json/kotlinx-serialization") {
//            call.respond(mapOf("hello" to "world"))
//        }
        configureAuth(client = client)
        bearerAuthenticate {
            get("/auth1") {
                call.respondText("${call.principal<UserIdPrincipal>()?.name}")
            }
        }
    }
}