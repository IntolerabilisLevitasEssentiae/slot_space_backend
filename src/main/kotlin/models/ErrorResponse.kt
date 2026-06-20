package com.slotspace.models

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall

data class ErrorResponse<T: Any>(
    val status: HttpStatusCode,
    val message: T
)

suspend inline fun <reified T: Any> RoutingCall.respond(response: ErrorResponse<T>) {
    this.respond(status = response.status, message = response.message)
}
