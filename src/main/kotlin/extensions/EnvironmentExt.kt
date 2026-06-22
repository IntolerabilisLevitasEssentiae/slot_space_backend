package com.slotspace.extensions

import io.ktor.server.application.ApplicationEnvironment

val ApplicationEnvironment.jwtSecret: String get() = config.property("jwt.secret").getString()
val ApplicationEnvironment.jwtRefreshSecret: String get() = config.property("jwt.refresh_secret").getString()
val ApplicationEnvironment.jwtIssuer: String get() = config.property("jwt.issuer").getString()
val ApplicationEnvironment.jwtAudience: String get() = config.property("jwt.audience").getString()

val ApplicationEnvironment.supabaseUrl: String get() = config.property("supabase.url").getString()
val ApplicationEnvironment.supabaseKey: String get() = config.property("supabase.key").getString()
