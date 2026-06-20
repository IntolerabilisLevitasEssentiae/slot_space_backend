package com.slotspace.models.dto.session

import com.slotspace.models.auth.Role
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SessionDto(
    @SerialName("user_id") val userId: Long?,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("refresh_expires_at") val refreshExpiresAt: Long,
    val role: Role,
)
