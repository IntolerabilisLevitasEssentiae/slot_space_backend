package com.slotspace.utils

object Const {

    const val AUTH_JWT_NAME = "auth-jwt"

    const val ACCESS_TOKEN_EXPIRES_AT_MILLIS = 300_000L // 5 минут
    const val REFRESH_TOKEN_EXPIRES_AT_MILLIS = 604800000L // 604800000L = 7 дней

    const val SUPABASE_EDGE_F_BASE_URL = "https://wglgfynfptjskhgwawbw.supabase.co/functions/v1"

    object Tables {

        object SessionTable {

            const val NAME = "session"

            object Column {
                const val ID = "id"
                const val USER_ID = "user_id"
                const val TOKEN_ACCESS = "access_token"
                const val TOKEN_REFRESH = "refresh_token"
                const val ROLE = "role"
                const val EXPIRES_AT_REFRESH = "refresh_expires_at"
            }
        }

        object UserTable {

            const val NAME = "user"

            object Column {
                const val ID = "id"
                const val LOGIN = "login"
                const val PASSWORD = "password"
                const val CREATED_AT = "created_at"
            }
        }

        object ProfileTable {

            const val NAME = "profile"

            object Column {
                const val USER_ID = "user_id"
                const val NICKNAME = "nickname"
                const val FIRST_NAME = "first_name"
                const val MIDDLE_NAME = "middle_name"
                const val LAST_NAME = "last_name"
                const val GENDER = "gender"
                const val AGE = "age"
                const val CREATED_AT = "created_at"
            }
        }
    }
}
