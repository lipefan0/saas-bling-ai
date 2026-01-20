package br.com.visibilitia.saas_bling.config

import br.com.visibilitia.saas_bling.entity.UserEntity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val secret: String,
    @Value("\${jwt.expiration}")
    private val expirationInMs: Long
) {
    private val key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))

    fun generateToken(user: UserEntity): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationInMs)
        return Jwts.builder()
            .subject(user.email)
            .claim("userId", user.id)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getEmailFromToken(token: String): String {
        return getClaimsFromToken(token).subject
    }

    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            return true
        } catch (e: Exception) {
            println(e.message)
        }
        return false
    }
}