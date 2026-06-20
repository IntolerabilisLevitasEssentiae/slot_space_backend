package com.slotspace.models.dto.user

import com.slotspace.utils.Const
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SavedUserDto(
    @SerialName(Const.Tables.UserTable.COLUMN.ID)
    val id: Long,
    @SerialName(Const.Tables.UserTable.COLUMN.LOGIN)
    val login: String,
    @SerialName(Const.Tables.UserTable.COLUMN.PASSWORD)
    val password: String,
    @SerialName(Const.Tables.UserTable.COLUMN.CREATED_AT)
    val createdAt: Instant,
)
