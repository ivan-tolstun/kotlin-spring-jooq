package de.tolstun.integration_test

import de.tolstun.testcontainer.extension.impl.container.*
import de.tolstun.integration_test.config.DbDatasetDirectory
import de.tolstun.integration_test.config.DbMigrationDirectory
import de.tolstun.integration_test.config.YamlTestDirectory
import de.tolstun.testcontainer.rest.dto.SortDto
import org.junit.Rule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.matchers.JUnitMatchers.containsString
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.time.Duration


@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("EMPLOYEE TEST CONTAINER :: INTEGRATION_TEST")
open class ExampleTest {


    @BeforeAll
    fun setUp() =

        allContainers
            .filterNot { it.isCreated }
            .filterNot { it.isRunning }
            .forEach { it.start() }


    @AfterAll
    fun shutdown() =

        allContainers
            .forEach { it.close() }



    @Test
    fun `check all containers are running`() {
        allContainers.forEach { assertEquals(true, it.isRunning) }
    }



    @Test
    fun `test GET employees`() {

        postgreSQLContainer.runMigrationScripts(
            *DbMigrationDirectory.ROLL_BACK_MIGRATION.paths.toTypedArray(),
            *DbMigrationDirectory.MIGRATION_1.paths.toTypedArray(),
            *DbDatasetDirectory.DATASET_V1.paths.toTypedArray()
        )

        employeeContainer.GET(
            path = "/api/employees",
            queries = mapOf(
                "sorting" to SortDto(field = "email", order = "desc")
            )
        )
            .then()
            .statusCode(200)
            .body(containsString(
                "{\"email\":\"device.manager@exceet.de\",\"firstName\":\"Device\",\"lastName\":\"Manager\"}," +
                        "{\"email\":\"Roman.Rem@exceet.de\",\"firstName\":\"Rem\",\"lastName\":\"Roman\"}," +
                        "{\"email\":\"Ivan.Tol@exceet.de\",\"firstName\":\"Tol\",\"lastName\":\"Ivan\"}," +
                        "{\"email\":\"Ilgar.Bos@exceet.de\",\"firstName\":\"Bos\",\"lastName\":\"Ilgar\"}"
            ))
    }



    @Test
    fun `test GET employees with selected emails`() {

        postgreSQLContainer.runMigrationScripts(
            *DbMigrationDirectory.ROLL_BACK_MIGRATION.paths.toTypedArray(),
            *DbMigrationDirectory.MIGRATION_1.paths.toTypedArray(),
            *DbDatasetDirectory.DATASET_V1.paths.toTypedArray()
        )

        employeeContainer.GET(
            path = "/api/employees",
            queries = mapOf(
                "sorting" to SortDto(field = "email", order = "desc"),
                "employeeEmails" to listOf("device.manager@exceet.de", "Roman.Rem@exceet.de")
            )
        )
            .then()
            .statusCode(200)
            .body(containsString(
                "{\"email\":\"device.manager@exceet.de\",\"firstName\":\"Device\",\"lastName\":\"Manager\"}," +
                        "{\"email\":\"Roman.Rem@exceet.de\",\"firstName\":\"Rem\",\"lastName\":\"Roman\"}"
            ))
    }



    @Test
    fun `test GET employees with email field only`() {

        postgreSQLContainer.runMigrationScripts(
            *DbMigrationDirectory.ROLL_BACK_MIGRATION.paths.toTypedArray(),
            *DbMigrationDirectory.MIGRATION_1.paths.toTypedArray(),
            *DbDatasetDirectory.DATASET_V1.paths.toTypedArray()
        )

        employeeContainer.GET(
            path = "/api/employees",
            queries = mapOf(
                "sorting" to SortDto(field = "email", order = "desc"),
                "employeeEmails" to listOf("device.manager@exceet.de", "Roman.Rem@exceet.de"),
                "selectFields" to listOf("email")
            )
        )
            .then()
            .statusCode(200)
            .body(containsString(
                "{\"email\":\"device.manager@exceet.de\"}," +
                        "{\"email\":\"Roman.Rem@exceet.de\"}"
            ))

    }



    @Test
    fun `auto rest api test from yaml file`() {

        postgreSQLContainer.runMigrationScripts(
            *DbMigrationDirectory.ROLL_BACK_MIGRATION.paths.toTypedArray(),
            *DbMigrationDirectory.MIGRATION_1.paths.toTypedArray(),
            *DbDatasetDirectory.DATASET_V1.paths.toTypedArray()
        )

        employeeContainer.runYamlTests(YamlTestDirectory.V1__EMPLOYEE.path)
    }



    companion object {

        private val LOGGER = LoggerFactory.getLogger(ExampleTest::class.java)

        const val JWT_KEY = "EtTIeXN7A5VNjq9U5k5AmipoYxwYsLbfUhNe1oeNXO08jvrZX66bpwHPfowZgyMZ"


        @Rule
        val postgreSQLContainer: PostgreSQLContainer = PostgreSQLContainer(
            imageName = "postgres:11-alpine",
            internalContainerName = "db--itest",
            internalDomain = "db",
            databaseName = "basedb",
            userName = "device-manager",
            password = "device-manager",
            environmentVariables = mutableMapOf("TZ" to "Europe/Berlin"))


        @Rule
        val employeeContainer: JavaRestApiContainer = JavaRestApiContainer(
            jarFile = File("""./../spring-example-impl/target/spring-example-impl-0.1.1.jar"""),
            internalContainerName = "employee--itest",
            internalDomain = "employee",
            internalHttpPort = 8080,
            jwtKey = JWT_KEY,
            environmentVariables = mutableMapOf(
                "DB_DATABASE" to postgreSQLContainer.databaseName,
                "DB_USER" to postgreSQLContainer.userName,
                "DB_PASSWORD" to postgreSQLContainer.password,
                "DB_URL" to postgreSQLContainer.internalDomain,
                "DB_PORT" to postgreSQLContainer.internalPort.toString(),
            ),
            internalHttpsPort = null,
            internalDebugPort = 5005,
            _waitStrategy = Wait.forHttp("/actuator/health").withReadTimeout(Duration.ofSeconds(25))
        )





        val allContainers: List<GenericContainer<*>> =

            listOf(postgreSQLContainer, employeeContainer,)


    }


}