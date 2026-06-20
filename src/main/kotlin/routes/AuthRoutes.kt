package com.slotspace.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.slotspace.extensions.*
import com.slotspace.models.auth.*
import com.slotspace.models.dto.session.SavedSessionDto
import com.slotspace.models.dto.session.SessionDto
import com.slotspace.models.dto.user.CreateProfileDto
import com.slotspace.models.dto.user.SavedProfileDto
import com.slotspace.models.dto.user.SavedUserDto
import com.slotspace.models.dto.user.UserDto
import com.slotspace.models.exception.MissedAccessTokenException
import com.slotspace.models.exception.SessionExpiredException
import com.slotspace.utils.Const
import com.slotspace.utils.Const.ACCESS_TOKEN_EXPIRES_AT_MILLIS
import com.slotspace.utils.Const.AUTH_JWT_NAME
import com.slotspace.utils.Const.REFRESH_TOKEN_EXPIRES_AT_MILLIS
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.from
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlin.time.Clock

fun Routing.configureAuth(supabase: SupabaseClient) {
    val jwtSecret = environment.jwtSecret
    val jwtRefreshSecret = environment.jwtRefreshSecret
    val jwtAudience = environment.jwtAudience
    val jwtIssuer = environment.jwtIssuer

    handleAnonymous(
        supabase = supabase,
        jwtSecret = jwtSecret,
        jwtRefreshSecret = jwtRefreshSecret,
        jwtAudience = jwtAudience,
        jwtIssuer = jwtIssuer,
    )

    handleRefresh(
        supabase = supabase,
        jwtSecret = jwtSecret,
        jwtRefreshSecret = jwtRefreshSecret,
        jwtAudience = jwtAudience,
        jwtIssuer = jwtIssuer,
    )

    handleRegistration(supabase = supabase)

    handleLogin(supabase = supabase)

    handleLogout(supabase = supabase)
}

fun Route.jwtAuthenticate(build: Route.() -> Unit) {
    authenticate(AUTH_JWT_NAME, build = build)
}

private fun Routing.handleAnonymous(
    supabase: SupabaseClient,
    jwtSecret: String,
    jwtRefreshSecret: String,
    jwtAudience: String,
    jwtIssuer: String,
) {
    post("/auth/anonymous") {
        val role = Role.Guest

        val accessToken = buildToken(
            jwtSecret = jwtSecret,
            jwtAudience = jwtAudience,
            jwtIssuer = jwtIssuer,
        )

        val refreshExpiresAt = Clock.System.now().toEpochMilliseconds() + REFRESH_TOKEN_EXPIRES_AT_MILLIS

        val refreshToken = buildToken(
            jwtSecret = jwtRefreshSecret,
            jwtAudience = jwtAudience,
            jwtIssuer = jwtIssuer,
            expiresAt = Date(refreshExpiresAt)
        )

        try {
            supabase
                .from(Const.Tables.SessionTable.NAME)
                .insert(
                    value = SessionDto(
                        userId = null,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        refreshExpiresAt = refreshExpiresAt,
                        role = role,
                    )
                )

            call.respond(
                status = HttpStatusCode.OK,
                message = TokenResponse(
                    access = accessToken,
                    refresh = refreshToken
                ),
            )
        } catch (e: PostgrestRestException) {
            e.printStackTrace()
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = e.message ?: "Can`t save tokens to db, request fails"
            )
        } catch (e: HttpRequestTimeoutException) {
            e.printStackTrace()
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = e.message ?: "Can`t save tokens to db, http timeout"
            )
        } catch (e: HttpRequestException) {
            e.printStackTrace()
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = e.message ?: "Can`t save tokens to db, network issues"
            )
        }
    }
}

private fun Routing.handleRefresh(
    supabase: SupabaseClient,
    jwtSecret: String,
    jwtRefreshSecret: String,
    jwtAudience: String,
    jwtIssuer: String,
) {
    post("/auth/refresh") {
        try {
            val requestToken = call.receive<TokenRefreshRequest>().token
            JWT.require(Algorithm.HMAC256(jwtRefreshSecret))
                .withAudience(jwtAudience)
                .withIssuer(jwtIssuer)
                .build()
                .verify(requestToken)

            val session = supabase
                .from(Const.Tables.SessionTable.NAME)
                .select { filter { eq(Const.Tables.SessionTable.Column.TOKEN_REFRESH, requestToken) } }
                .decodeSingle<SavedSessionDto>()

            val access = buildToken(
                jwtSecret = jwtSecret,
                jwtAudience = jwtAudience,
                jwtIssuer = jwtIssuer,
            )

            val refreshExpiresAt = Clock.System.now().toEpochMilliseconds() + REFRESH_TOKEN_EXPIRES_AT_MILLIS

            val refresh = buildToken(
                jwtSecret = jwtRefreshSecret,
                jwtAudience = jwtAudience,
                jwtIssuer = jwtIssuer,
                expiresAt = Date(refreshExpiresAt),
            )

            supabase
                .from(Const.Tables.SessionTable.NAME)
                .update(
                    update = {
                        set(Const.Tables.SessionTable.Column.TOKEN_ACCESS, access)
                        set(Const.Tables.SessionTable.Column.TOKEN_REFRESH, refresh)
                        set(Const.Tables.SessionTable.Column.EXPIRES_AT_REFRESH, refreshExpiresAt)
                    },
                    request = {
                        filter {
                            eq(Const.Tables.SessionTable.Column.ID, session.id)
                        }
                    }
                )

            call.respond(
                status = HttpStatusCode.OK,
                message = TokenResponse(
                    access = access,
                    refresh = refresh,
                )
            )
        } catch (e: JWTVerificationException) {
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = e.message ?: "JWT verification is failed!"
            )
        } catch (e: PostgrestRestException) {
            e.printStackTrace()
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = e.message ?: "Can`t update tokens to db, request fails"
            )
        } catch (e: HttpRequestTimeoutException) {
            e.printStackTrace()
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = e.message ?: "Can`t update tokens to db, http timeout"
            )
        } catch (e: HttpRequestException) {
            e.printStackTrace()
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = e.message ?: "Can`t update tokens to db, network issues"
            )
        }
    }
}

private fun Routing.handleLogin(supabase: SupabaseClient) {
    jwtAuthenticate {
        post("/auth/login") {
            try {
                val session = supabase.getSession(accessToken = call.getAccessToken())

                if (session.userId != null) {
                    return@post call.respond(
                        status = HttpStatusCode.Forbidden,
                        message = "Wrong access token"
                    )
                }

                val credentials = call.receive<UserCredentialsRequest>()
                // todo: провалидировать login и password // todo: шифровать пароль

                val savedUser = supabase
                    .from(Const.Tables.UserTable.NAME)
                    .select {
                        filter {
                            eq(Const.Tables.UserTable.COLUMN.LOGIN, credentials.login)
                        }
                        limit(1)
                    }
                    .decodeSingleOrNull<SavedUserDto>()

                if (savedUser == null) {
                    return@post call.respond(
                        status = HttpStatusCode.NotFound,
                        message = "User unfound"
                    )
                }

                if (savedUser.password != credentials.password) {
                    return@post call.respond(
                        status = HttpStatusCode.Forbidden,
                        message = "Wrong password"
                    )
                }
                supabase
                    .from(Const.Tables.SessionTable.NAME)
                    .update(
                        update = {
                            set(Const.Tables.SessionTable.Column.USER_ID, savedUser.id)
                            set(Const.Tables.SessionTable.Column.ROLE, Role.User)
                        },
                        request = {
                            filter {
                                eq(Const.Tables.SessionTable.Column.ID, session.id)
                            }
                        }
                    )
                // todo: удалить из токена роль? Удалить из токена userId?
                call.respond(
                    status = HttpStatusCode.OK,
                    message = LoginResponse(id = savedUser.id)
                )
            } catch (e: ContentTransformationException) {
                call.respond(
                    status = HttpStatusCode.UnprocessableEntity,
                    message = e.message ?: "Something wen`t wrong on body parse"
                )
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

private fun Routing.handleRegistration(supabase: SupabaseClient) {
    jwtAuthenticate {
        post("/auth/registration") {
            try {
                val accessToken = call.getAccessToken()
                val session = supabase.getSession(accessToken = accessToken)

                if (session.userId != null) {
                    return@post call.respond(
                        status = HttpStatusCode.Forbidden,
                        message = "Wrong access token"
                    )
                }

                val credentials = call.receive<UserCredentialsRequest>()
                // todo: провалидировать login и password // todo: шифровать пароль

                val user = supabase
                    .from(Const.Tables.UserTable.NAME)
                    .select {
                        filter {
                            eq(Const.Tables.UserTable.COLUMN.LOGIN, credentials.login)
                        }
                        limit(1)
                    }
                    .decodeSingleOrNull<SavedUserDto>()

                if (user != null) {
                    return@post call.respond(
                        status = HttpStatusCode.Conflict,
                        message = "User already exist"
                    )
                }

                val savedUser = supabase
                    .from(Const.Tables.UserTable.NAME)
                    .insert(
                        value = UserDto(
                            login = credentials.login,
                            password = credentials.password
                        )
                    ) {
                        select()
                    }.decodeSingle<SavedUserDto>()

                val savedProfile = supabase
                    .from(Const.Tables.ProfileTable.NAME)
                    .insert(
                        value = CreateProfileDto(
                            id = savedUser.id,
                            createdAt = savedUser.createdAt
                        )
                    ) {
                        select()
                    }.decodeSingle<SavedProfileDto>()

                supabase
                    .from(Const.Tables.SessionTable.NAME)
                    .update(
                        update = {
                            set(Const.Tables.SessionTable.Column.USER_ID, savedProfile.userId)
                            set(Const.Tables.SessionTable.Column.ROLE, Role.User)
                        },
                        request = {
                            filter {
                                eq(Const.Tables.SessionTable.Column.TOKEN_ACCESS, accessToken)
                            }
                        }
                    )

                call.respond(
                    status = HttpStatusCode.Created,
                    message = savedProfile
                )
            } catch (e: ContentTransformationException) {
                call.respond(
                    status = HttpStatusCode.UnprocessableEntity,
                    message = e.message ?: "Something wen`t wrong on body parse"
                )
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

private fun Routing.handleLogout(supabase: SupabaseClient) {
    jwtAuthenticate {
        post("/auth/logout") {
            try {
                val accessToken = call.getAccessToken()
                val session = supabase.getSession(accessToken = accessToken)

                if (session.userId == null) {
                    return@post call.respond(
                        status = HttpStatusCode.Forbidden,
                        message = "Wrong access token"
                    )
                }

                supabase
                    .from(Const.Tables.SessionTable.NAME)
                    .delete {
                        filter {
                            eq(Const.Tables.SessionTable.Column.TOKEN_REFRESH, session.refreshToken)
                            and {
                                eq(Const.Tables.SessionTable.Column.USER_ID, session.userId)
                            }
                        }
                    }

                call.respond(
                    status = HttpStatusCode.OK,
                    message = "Success logout"
                )
            } catch (e: ContentTransformationException) {
                call.respond(
                    status = HttpStatusCode.UnprocessableEntity,
                    message = e.message ?: "Something wen`t wrong on body parse"
                )
            } catch (e: PostgrestRestException) {
                e.printStackTrace()
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = e.message ?: "Can`t modify db, request fails"
                )
            } catch (e: HttpRequestTimeoutException) {
                e.printStackTrace()
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = e.message ?: "Can`t modify db, http timeout"
                )
            } catch (e: HttpRequestException) {
                e.printStackTrace()
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = e.message ?: "Can`t modify db, network issues"
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

private fun buildToken(
    jwtSecret: String,
    jwtAudience: String,
    jwtIssuer: String,
    expiresAtMillis: Long = ACCESS_TOKEN_EXPIRES_AT_MILLIS,
): String {
    return buildToken(
        jwtSecret = jwtSecret,
        jwtAudience = jwtAudience,
        jwtIssuer = jwtIssuer,
        expiresAt = Date(Clock.System.now().toEpochMilliseconds() + expiresAtMillis),
    )
}

private fun buildToken(
    jwtSecret: String,
    jwtAudience: String,
    jwtIssuer: String,
    expiresAt: Date,
): String {
    return JWT.create()
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .withExpiresAt(expiresAt)
        .sign(Algorithm.HMAC256(jwtSecret))
}
