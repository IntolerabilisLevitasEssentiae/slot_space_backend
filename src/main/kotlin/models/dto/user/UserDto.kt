package com.slotspace.models.dto.user

import com.slotspace.utils.Const
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName(Const.Tables.UserTable.COLUMN.LOGIN)
    val login: String,
    @SerialName(Const.Tables.UserTable.COLUMN.PASSWORD)
    val password: String,
)
