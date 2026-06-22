package com.slotspace.di

import com.slotspace.extensions.supabaseKey
import com.slotspace.extensions.supabaseUrl
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
                supabaseUrl = this@dataModule.environment.supabaseUrl,
                supabaseKey = this@dataModule.environment.supabaseKey,
            ) {
                install(Postgrest)
                defaultLogLevel = LogLevel.DEBUG
            }
        }
    }
}
