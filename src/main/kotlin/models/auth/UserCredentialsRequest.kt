package com.slotspace.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserCredentialsRequest(
    val login: String,
    val password: String,
)
