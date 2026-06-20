package com.slotspace.models.dto.user

import com.slotspace.models.auth.Gender
import com.slotspace.utils.Const
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SavedProfileDto(
    @SerialName(Const.Tables.ProfileTable.COLUMN.USER_ID)
    val userId: Long,
    @SerialName(Const.Tables.ProfileTable.COLUMN.NICKNAME)
    val nickname: String,
    @SerialName(Const.Tables.ProfileTable.COLUMN.FIRST_NAME)
    val firstName: String,
    @SerialName(Const.Tables.ProfileTable.COLUMN.MIDDLE_NAME)
    val middleName: String,
    @SerialName(Const.Tables.ProfileTable.COLUMN.LAST_NAME)
    val lastName: String,
    @SerialName(Const.Tables.ProfileTable.COLUMN.GENDER)
    val gender: Gender,
    @SerialName(Const.Tables.ProfileTable.COLUMN.AGE)
    val age: Int?,
    @SerialName(Const.Tables.ProfileTable.COLUMN.CREATED_AT)
    val createdAt: Instant,

)
