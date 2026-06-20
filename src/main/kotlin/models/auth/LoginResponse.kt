package com.slotspace.models.auth

import com.slotspace.utils.Const
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName(Const.Tables.ProfileTable.COLUMN.USER_ID)
    val id: Long
)
