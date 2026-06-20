package com.slotspace

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.slotspace.extensions.getAccessToken
import com.slotspace.extensions.getSession
import com.slotspace.extensions.jwtSecret
import com.slotspace.models.dto.user.SavedProfileDto
import com.slotspace.models.exception.MissedAccessTokenException
import com.slotspace.models.exception.SessionExpiredException
import com.slotspace.routes.configureAuth
import com.slotspace.routes.jwtAuthenticate
import com.slotspace.utils.Const
import com.slotspace.utils.Const.AUTH_JWT_NAME
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.from
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// todo: был плагин для регистрации cron-job-ы. Надо чистить сессии, сделать крч
fun Application.configureRouting(client: HttpClient, supabase: SupabaseClient) {
    val jwtSecret = environment.jwtSecret

    install(Authentication) {
        jwt(name = AUTH_JWT_NAME) {
            verifier(JWT.require(Algorithm.HMAC256(jwtSecret)).build())

            validate { credential ->
                // no additional validation
                JWTPrincipal(credential.payload)
            }

            challenge { _, _ ->
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = "Auth error"
                )
            }
        }
    }

    routing {
        // отдача файлов напрямую из resources/static
        staticResources("/static", "static")

        configureAuth(supabase = supabase)

        jwtAuthenticate {
            get("/profile") {
                try {
                    val session = supabase.getSession(accessToken = call.getAccessToken())
                    if (session.userId == null) {
                        return@get call.respond(
                            status = HttpStatusCode.Forbidden,
                            message = "Wrong access token"
                        )
                    }
                    val profile = supabase
                        .from(Const.Tables.ProfileTable.NAME)
                        .select {
                            filter {
                                eq(Const.Tables.ProfileTable.COLUMN.USER_ID, session.userId)
                            }
                            limit(1)
                        }.decodeSingle<SavedProfileDto>()

                    call.respond(status = HttpStatusCode.OK, message = profile)
                } catch (e: PostgrestRestException) {
                    e.printStackTrace()
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = e.message ?: "Can`t save user to db, request fails"
                    )
                } catch (e: HttpRequestTimeoutException) {
                    e.printStackTrace()
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = e.message ?: "Can`t save user to db, http timeout"
                    )
                } catch (e: HttpRequestException) {
                    e.printStackTrace()
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = e.message ?: "Can`t save user to db, network issues"
                    )
                } catch (e: SessionExpiredException) {
                    e.printStackTrace()
                    call.respond(
                        status = HttpStatusCode.Unauthorized,
                        message = e.message ?: "Session not exist! Check access token"
                    )
                } catch (e: MissedAccessTokenException) {
                    e.printStackTrace()
                    call.respond(
                        status = HttpStatusCode.Unauthorized,
                        message = e.message ?: "Missed access token"
                    )
                }
            }
        }
    }
}