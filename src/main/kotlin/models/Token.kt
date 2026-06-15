package com.slotspace.models

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val access: String,
    val refresh: String
)
