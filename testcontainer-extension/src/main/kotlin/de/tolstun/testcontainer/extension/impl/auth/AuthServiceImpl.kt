package de.tolstun.testcontainer.extension.impl.auth

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import de.tolstun.testcontainer.extension.api.auth.AuthService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.JacksonSerializer
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.security.Key
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.spec.SecretKeySpec


class AuthServiceImpl(private val secret: String) : AuthService {


    private val mapper = ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)


    private val hmacKeyDeprecated: Key = Keys.hmacShaKeyFor(
        secret.toByteArray(StandardCharsets.UTF_8))


    private val hmacKey: Key = SecretKeySpec(
        Base64.getDecoder().decode(secret),
        SignatureAlgorithm.HS256.jcaName)


    override fun buildHmacToken(body: Map<String, Any>,
                                expires: Long,
                                timeType: ChronoUnit): String {

        val now = Instant.now()
        val expiresInSeconds = Date.from(now.plus(expires, timeType))

        return Jwts.builder()
            .serializeToJsonWith(JacksonSerializer(mapper))
            .setIssuedAt(Date.from(now))
            .setExpiration(expiresInSeconds)
            .signWith(hmacKeyDeprecated)
            .setClaims(body).compact()
    }


    override fun parseHmacToken(jwtAsString: String): Jws<Claims> {

        return Jwts
            .parser()
            .setSigningKey(hmacKeyDeprecated)
            .parseClaimsJws(jwtAsString)
    }


    override fun <E> parseHmacToken(jwtAsString: String,
                                    clazz: Class<E>): E {

        val body = Jwts
            .parser()
            .setSigningKey(hmacKeyDeprecated)
            .parseClaimsJws(jwtAsString)
            .body

        return mapper.convertValue(body, clazz)
    }


}
