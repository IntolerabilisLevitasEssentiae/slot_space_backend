package com.slotspace.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.dataModule() {
    dependencies {
        provide<SupabaseClient> {
            createSupabaseClient(
                supabaseUrl = "https://wglgfynfptjskhgwawbw.supabase.co",
                supabaseKey = "sb_secret_-63KQfRCx0LilhLwteYY_Q_EjB4pFon"
            ) {
                install(Postgrest)
                defaultLogLevel = LogLevel.DEBUG
            }
        }
    }
}
