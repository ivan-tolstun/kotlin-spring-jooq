package de.tolstun.testcontainer.extension.api.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import java.time.temporal.ChronoUnit


interface AuthService {


    fun buildHmacToken(body: Map<String, Any>,
                       expires: Long,
                       timeType: ChronoUnit): String


    fun parseHmacToken(jwtAsString: String): Jws<Claims>


    fun <E> parseHmacToken(jwtAsString: String,
                           clazz: Class<E>): E


}