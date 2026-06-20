package com.slotspace.models.dto.user

import com.slotspace.utils.Const
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CreateProfileDto(
    @SerialName(Const.Tables.ProfileTable.COLUMN.USER_ID)
    val id: Long,
    @SerialName(Const.Tables.ProfileTable.COLUMN.CREATED_AT)
    val createdAt: Instant
)
