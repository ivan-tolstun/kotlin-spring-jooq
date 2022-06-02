package de.tolstun.integration_test

import de.tolstun.testcontainer.extension.impl.container.*
import de.tolstun.integration_test.config.DbDatasetDirectory
import de.tolstun.integration_test.config.DbMigrationDirectory
import de.tolstun.integration_test.config.YamlTestDirectory
import org.junit.Rule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
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


    private val updateDbDatasetV1 = {

        postgreSQLContainer.runMigrationScripts(
            *DbMigrationDirectory.ROLL_BACK_MIGRATION.paths.toTypedArray(),
            *DbMigrationDirectory.MIGRATION_1.paths.toTypedArray(),
            *DbDatasetDirectory.DATASET_V1.paths.toTypedArray()
        )
    }


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
    fun `auto rest api test from yaml file`() {

        updateDbDatasetV1()
        employeeContainer.runYamlTests(YamlTestDirectory.V1__EMPLOYEE.path)
        updateDbDatasetV1()
    }



    @Test
    fun `rest api test`() {

        updateDbDatasetV1()
        employeeContainer.runYamlTests(YamlTestDirectory.V1__EMPLOYEE.path)
        updateDbDatasetV1()
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
            jarFile = File("""./../spring-example-impl/target/spring-example-impl-0.1.0.jar"""),
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