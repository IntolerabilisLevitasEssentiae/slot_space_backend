package com.slotspace.models.exception

import com.auth0.jwt.exceptions.JWTVerificationException

class InvalidJwtTokenException(message: String) : JWTVerificationException(message)
