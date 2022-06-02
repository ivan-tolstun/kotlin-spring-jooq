package de.tolstun.testcontainer.extension.api.container

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import de.tolstun.testcontainer.extension.api.model.AutoTestConf
import de.tolstun.testcontainer.extension.api.model.ItemAutoTestConf
import de.tolstun.testcontainer.extension.api.model.VariableToSaveConf
import de.tolstun.testcontainer.extension.impl.behavior.AutoTestBehavior.readAsAutoTestConf
import de.tolstun.testcontainer.extension.impl.client.RestApiClient
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.junit.jupiter.api.Assertions
import org.testcontainers.containers.GenericContainer
import org.testcontainers.shaded.com.google.common.io.Resources
import java.io.File
import java.time.temporal.ChronoUnit


interface BaseRestApiContainer<SELF : GenericContainer<SELF>> : BaseContainer<SELF> {


    val restApiClient: RestApiClient


    fun GET(path: String,
            header: Map<String, String>? = null,
            queries: Map<String, Any>? = null,
            jwtBody: Map<String, Any>? = null,
            jwtExpires: Long? = null): Response {

        return restApiClient
            .call(jwtBody, jwtExpires) { requestSpecification ->

                requestSpecification
                    .let { rs -> if(header != null && header.isNotEmpty()) rs.headers(header) else rs }
                    .let { rs -> if(queries != null && queries.isNotEmpty()) rs.queryParams(queries) else rs }
                    .get(path)
            }
    }


    fun <T> POST(path: String,
                 header: Map<String, String>? = null,
                 body: T?,
                 jwtBody: Map<String, Any>? = null,
                 contentType: ContentType = ContentType.JSON,
                 jwtExpires: Long? = null): Response {

        return restApiClient
            .call(jwtBody, jwtExpires) { requestSpecification ->

                requestSpecification
                    .let { rs -> if(header != null && header.isNotEmpty()) rs.headers(header) else rs }
                    .let { rs -> if(body != null) rs.body(body) else rs }
                    .contentType(contentType)
                    .post(path)
            }
    }


    fun POST(path: String,
             header: Map<String, String>? = null,
             file: File,
             controlName: String,
             jwtBody: Map<String, Any>? = null,
             jwtExpires: Long? = null): Response {

        return restApiClient
            .call(jwtBody, jwtExpires) { requestSpecification ->

                requestSpecification
                    .let { rs -> if(header != null && header.isNotEmpty()) rs.headers(header) else rs }
                    .multiPart(controlName, file)
                    .post(path)
            }
    }


    fun <T> PUT(path: String,
                header: Map<String, String>? = null,
                body: T?,
                contentType: ContentType = ContentType.JSON,
                jwtBody: Map<String, Any>? = null,
                jwtExpires: Long? = null): Response {

        return restApiClient
            .call(jwtBody, jwtExpires) { requestSpecification ->

                requestSpecification
                    .let { rs -> if(header != null && header.isNotEmpty()) rs.headers(header) else rs }
                    .let { rs -> if(body != null) rs.body(body) else rs }
                    .contentType(contentType)
                    .put(path)
            }
    }


    fun PUT(path: String,
            header: Map<String, String>? = null,
            file: File,
            controlName: String,
            jwtBody: Map<String, Any>? = null,
            jwtExpires: Long? = null): Response {

        return restApiClient
            .call(jwtBody, jwtExpires) { requestSpecification ->

                requestSpecification
                    .let { rs -> if(header != null && header.isNotEmpty()) rs.headers(header) else rs }
                    .multiPart(controlName, file)
                    .put(path)
            }
    }


    fun <T> PATCH(path: String,
                  header: Map<String, String>? = null,
                  body: T?,
                  contentType: ContentType = ContentType.JSON,
                  jwtBody: Map<String, Any>? = null,
                  jwtExpires: Long? = null): Response {

        return restApiClient
            .call(jwtBody, jwtExpires) { requestSpecification ->

                requestSpecification
                    .let { rs -> if(header != null && header.isNotEmpty()) rs.headers(header) else rs }
                    .let { rs -> if(body != null) rs.body(body) else rs }
                    .contentType(contentType)
                    .patch(path)
            }
    }


    fun PATCH(path: String,
              header: Map<String, String>? = null,
              file: File,
              controlName: String,
              jwtBody: Map<String, Any>? = null,
              jwtExpires: Long? = null): Response {

        return restApiClient
            .call(jwtBody, jwtExpires) { requestSpecification ->

                requestSpecification
                    .let { rs -> if(header != null && header.isNotEmpty()) rs.headers(header) else rs }
                    .multiPart(controlName, file)
                    .patch(path)
            }
    }


    fun DELETE(path: String,
               header: Map<String, String>? = null,
               queries: Map<String, Any>? = null,
               contentType: ContentType = ContentType.JSON,
               jwtBody: Map<String, Any>? = null,
               jwtExpires: Long? = null): Response {

        return restApiClient
            .call(jwtBody, jwtExpires) { requestSpecification ->

                requestSpecification
                    .let { rs -> if(header != null && header.isNotEmpty()) rs.headers(header) else rs }
                    .let { rs -> if(queries != null && queries.isNotEmpty()) rs.queryParams(queries) else rs }
                    .contentType(contentType)
                    .delete(path)
            }
    }


    fun runYamlTests(filePathFromSourcesFolder: String) {

        val pathAsString = Resources.getResource(filePathFromSourcesFolder).file
        val autoTestConf: AutoTestConf = File(pathAsString).readAsAutoTestConf()
        var savedVariables = autoTestConf.globalVariables
            ?.associate { it.key to it.value }
            ?: mapOf()

        autoTestConf
            .restApiTests
            .forEach { testConf ->
                savedVariables = savedVariables.plus(runItemYamlTest(testConf, savedVariables))
            }
    }


    private fun runItemYamlTest(testConf: ItemAutoTestConf,
                                savedVariables: Map<String, Any>): Map<String, Any> {


        val adjustedTestConf = replaceVariablesInTestConf(testConf, savedVariables)
        val response = callService(adjustedTestConf)

        Assertions.assertEquals(
            adjustedTestConf.responseStatusCode, response.statusCode, """

               | TEST NAME:  ${adjustedTestConf.testName}
               | REQUEST URI: ${restApiClient.buildUri(testConf.path)}
               | REQUEST QUERIES: ${testConf.queries}
               | REQUEST BODY: ${testConf.body}
               | REQUEST HEADER TOKEN: ${
                   
                   if(testConf.jwt?.body != null && testConf.jwt?.expires != null)
                    restApiClient.buildJwtHeader(testConf.jwt.body, testConf.jwt.expires, ChronoUnit.MILLIS)
                   else "null"
                   
               }

            """.trimMargin()
        )


        adjustedTestConf
            .responseBody
            ?.also { expectedResponseBody ->

                val expected = toJsonNode(expectedResponseBody) ?: expectedResponseBody
                val actual = toJsonNode(response.print()) ?: response.print()

                Assertions.assertEquals(
                    expected, actual, """

                       | TEST NAME:  ${adjustedTestConf.testName}
                       | REQUEST URI: ${restApiClient.buildUri(testConf.path)}
                       | REQUEST QUERIES: ${testConf.queries}
                       | REQUEST BODY: ${testConf.body}
                       | REQUEST HEADER TOKEN: ${
                           
                           if(testConf.jwt?.body != null && testConf.jwt?.expires != null)  
                               restApiClient.buildJwtHeader(testConf.jwt.body, testConf.jwt.expires, ChronoUnit.MILLIS)
                           else "null"
                           
                       }

                    """.trimMargin()
                )
        }

        return savedVariables.plus(

            findVariablesToSave(
                response = response,
                variablesToSave = testConf.variablesToSave)
        )
    }


    private fun callService(testConf: ItemAutoTestConf) = when {


        testConf.method == "GET" -> GET(
            path = testConf.path,
            queries = testConf.queries,
            jwtBody = testConf.jwt?.body,
            jwtExpires = testConf.jwt?.expires)


        testConf.method == "PATCH" && testConf.uploadFile == null -> PATCH(
            path = testConf.path,
            body = testConf.body,
            jwtBody = testConf.jwt?.body,
            jwtExpires = testConf.jwt?.expires)


        testConf.method == "PATCH" && testConf.uploadFile != null -> PATCH(
            path = testConf.path,
            file = testConf.uploadFile?.filePath?.let { File(Resources.getResource(it).file) }!!,
            controlName = testConf.uploadFile?.controlName!!,
            jwtBody = testConf.jwt?.body,
            jwtExpires = testConf.jwt?.expires)


        testConf.method == "POST" && testConf.uploadFile == null -> POST(
            path = testConf.path,
            body = testConf.body,
            jwtBody = testConf.jwt?.body,
            jwtExpires = testConf.jwt?.expires)


        testConf.method == "POST" && testConf.uploadFile != null -> POST(
            path = testConf.path,
            file = testConf.uploadFile?.filePath?.let { File(Resources.getResource(it).file) }!!,
            controlName = testConf.uploadFile?.controlName!!,
            jwtBody = testConf.jwt?.body,
            jwtExpires = testConf.jwt?.expires)


        testConf.method == "PUT" && testConf.uploadFile == null -> PUT(
            path = testConf.path,
            body = testConf.body,
            jwtBody = testConf.jwt?.body,
            jwtExpires = testConf.jwt?.expires)


        testConf.method == "PUT" && testConf.uploadFile?.filePath != null -> PUT(
            path = testConf.path,
            file = testConf.uploadFile?.filePath?.let { File(Resources.getResource(it).file) }!!,
            controlName = testConf.uploadFile?.controlName!!,
            jwtBody = testConf.jwt?.body,
            jwtExpires = testConf.jwt?.expires)


        testConf.method == "DELETE" -> DELETE(
            path = testConf.path,
            queries = testConf.queries,
            jwtBody = testConf.jwt?.body,
            jwtExpires = testConf.jwt?.expires)


        else -> throw IllegalArgumentException("http request method is not supported by auto test")
    }


    private fun toJsonNode(json: String) = try {

        ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
            .readTree(json)
            .takeIf{ !it.isNull }
            //?.let { ObjectMapper().writeValueAsString(it) }

    } catch (e: Exception) { null }


    private fun findVariablesToSave(response: Response,
                                    variablesToSave: List<VariableToSaveConf>? = null) =
        variablesToSave
            ?.associate { (pathInJson, saveAs) ->
                (saveAs to response.path<Any>(pathInJson))
            } ?: emptyMap()


    private fun replaceVariablesInTestConf(testConf: ItemAutoTestConf,
                                           savedVariables: Map<String, Any>): ItemAutoTestConf {

        val mapper = ObjectMapper()

        val replaceMap = {  key: String, value: String, savedVariable: Map.Entry<String, Any> ->
            key to value.replace("""{{${savedVariable.key}}}""", mapper.writeValueAsString(savedVariable.value))
        }

        savedVariables.forEach { savedVariable ->

            testConf.queries = testConf.queries
                ?.map { (key, value) -> if(value is String) replaceMap(key, value, savedVariable) else key to value }
                ?.toMap()

            testConf.body = testConf.body
                ?.map { (key, value) -> if(value is String) replaceMap(key, value, savedVariable) else key to value }
                ?.toMap()

            testConf.path = testConf.path.replace(
                """{{${savedVariable.key}}}""", savedVariable.value.toString())

            testConf.responseBody = testConf.responseBody?.replace(
                """{{${savedVariable.key}}}""", mapper.writeValueAsString(savedVariable.value))
        }

        return testConf
    }


}