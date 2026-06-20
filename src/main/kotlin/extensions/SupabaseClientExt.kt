package com.slotspace.extensions

import com.slotspace.models.dto.session.SavedSessionDto
import com.slotspace.models.exception.SessionExpiredException
import com.slotspace.utils.Const
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

suspend fun SupabaseClient.getSession(accessToken: String): SavedSessionDto {
    val session = from(Const.Tables.SessionTable.NAME)
        .select {
            filter {
                eq(Const.Tables.SessionTable.Column.TOKEN_ACCESS, accessToken)
            }
        }.decodeSingleOrNull<SavedSessionDto>()

    if (session == null) {
        throw SessionExpiredException()
    }

    return session
}
