package com.slotspace.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val access: String,
    val refresh: String
)
