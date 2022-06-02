package de.tolstun.testcontainer.extension.impl.client

import de.tolstun.testcontainer.extension.api.auth.AuthService
import de.tolstun.testcontainer.extension.impl.auth.AuthServiceImpl
import io.restassured.RestAssured
import io.restassured.http.Header
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import java.net.URI
import java.time.temporal.ChronoUnit


class RestApiClient(private val host: String,
                    private val port: Int,
                    private val basePath: String?,
                    private val jwtKey: String) {


    private val authService: AuthService = AuthServiceImpl(jwtKey)


    fun call(authHeader: Header? = null,
             callProcess: (RequestSpecification) -> Response): Response {

        val requestSpecification = RestAssured
            .given()
            .let { requestSpec ->  if(authHeader != null) requestSpec.header(authHeader) else requestSpec }
            .baseUri("http://${this.host}:${this.port}${basePath ?: ""}")

        return callProcess(requestSpecification)
    }


    fun call(jwtBody: Map<String, Any>? = null,
             jwtExpires: Long? = null,
             callProcess: (RequestSpecification) -> Response): Response {

        val tokenHeader =
            if (jwtBody != null && jwtBody.isNotEmpty())
                buildJwtHeader(jwtBody = jwtBody, jwtExpires = jwtExpires ?: 5000L, timeType = ChronoUnit.MILLIS)
            else null

        return call(tokenHeader, callProcess)
    }


    fun buildUri(path: String? = null): URI =

        URI("http://$host:$port${basePath ?: ""}${path ?: ""}")


    fun buildJwtHeader(jwtBody: Map<String, Any>,
                               jwtExpires: Long,
                               timeType: ChronoUnit) =

        if(jwtBody.isNotEmpty())
            headerJwtToken(body = jwtBody, jwtExpires, timeType)
        else null


    private fun headerJwtToken(body: Map<String, Any>,
                               expires: Long,
                               timeType: ChronoUnit): Header =

        Header("Authorization", "Bearer " + authService.buildHmacToken(body, expires, timeType))


}