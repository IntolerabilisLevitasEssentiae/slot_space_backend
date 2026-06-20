package com.slotspace.extensions

import com.slotspace.models.exception.MissedAccessTokenException
import io.ktor.server.routing.RoutingCall
import kotlin.text.removePrefix

fun RoutingCall.getAccessToken(): String {
    val accessToken = request.headers["Authorization"]
        ?.removePrefix("Bearer ")
        ?.trim()

    if (accessToken.isNullOrBlank()) {
        throw MissedAccessTokenException()
    }

    return accessToken
}
