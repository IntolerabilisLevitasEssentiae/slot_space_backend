package com.slotspace.models.auth

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = RoleSerializer::class)
enum class Role {
    Guest, User
}

object RoleSerializer : KSerializer<Role> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "Role",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: Role,
    ) {
        encoder.encodeString(value.name.lowercase())
    }

    override fun deserialize(
        decoder: Decoder,
    ): Role {
        val value = decoder.decodeString()
        return Role.entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: throw SerializationException("Unknown role: $value")
    }
}


//import com.auth0.jwt.JWT
//import com.auth0.jwt.JWTCreator
//import com.auth0.jwt.interfaces.DecodedJWT
//import com.auth0.jwt.interfaces.Verification
//import com.slotspace.models.exception.InvalidJwtTokenException
//import io.ktor.server.auth.jwt.JWTPrincipal

//fun JWTCreator.Builder.withRole(role: Role): JWTCreator.Builder = apply {
//    withClaim(ROLE_KEY, role.name.lowercase())
//}
//
//fun JWTCreator.Builder.withUserId(id: Long?): JWTCreator.Builder = apply {
//    withClaim(USER_ID_KEY, id)
//}
//
//fun Verification.withRole(role: Role): Verification = apply {
//    withClaim(ROLE_KEY, role.name.lowercase())
//}
//
//fun isGuestToken(token: String): Boolean {
//    return JWT.decode(token).getClaim(ROLE_KEY).asString().equals(Role.Guest.name, ignoreCase = true)
//}
//
///**
// * Extracts the user role from the JWT token claims.
// *
// * This function retrieves the role claim from the decoded JWT, normalizes it by
// * capitalizing the first character (e.g., "guest" becomes "Guest"), and returns
// * the corresponding [Role] enum value.
// *
// * @return The [Role] extracted from the token.
// * @throws InvalidJwtTokenException if the role claim doesn't exist or match any [Role] enum value.
// */
//fun DecodedJWT.getRole(): Role {
//    val name = getClaim(ROLE_KEY)?.asString()?.takeIf { it.isNotBlank() }
//        ?.replaceFirstChar { it.uppercase() }
//        ?: throw InvalidJwtTokenException(message = "Role claim is null!")
//    return try {
//        Role.valueOf(name)
//    } catch (e: IllegalArgumentException) {
//        throw InvalidJwtTokenException(message = e.message ?: "$name not match Role values")
//    }
//}
//
//fun DecodedJWT.getUserId(): Long? {
//    return getClaim(USER_ID_KEY)?.asLong()
//}
//
//fun JWTPrincipal.getUserId(): Long? {
//    return payload.getClaim(USER_ID_KEY)?.asLong()
//}
//
//private const val ROLE_KEY = "role"
//
//private const val USER_ID_KEY = "user_id"
