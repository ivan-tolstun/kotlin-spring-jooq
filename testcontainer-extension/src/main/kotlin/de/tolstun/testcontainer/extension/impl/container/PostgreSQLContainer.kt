package de.tolstun.testcontainer.extension.impl.container

import de.tolstun.testcontainer.extension.impl.network.CurrentDockerNetwork
import de.tolstun.testcontainer.extension.api.container.BaseContainer
import de.tolstun.testcontainer.extension.factory.DataAccessFactory
import org.jooq.impl.DefaultDSLContext
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.shaded.com.google.common.io.Resources
import java.io.File
import java.time.temporal.ChronoUnit


class PostgreSQLContainer(private val imageName: String,
                          val internalContainerName: String,
                          val internalDomain: String,
                          val internalPort: Int = 5432,
                          val databaseName: String,
                          val userName: String,
                          val password: String,
                          private val environmentVariables: MutableMap<String, String>,
                          private val filesToBind: Map<String, String>? = emptyMap(),)

    : GenericContainer<PostgreSQLContainer>(imageName), BaseContainer<PostgreSQLContainer> {


    init {

        val waitStrategy = (LogMessageWaitStrategy())
            .withRegEx(".*database system is ready to accept connections.*\\s")
            .withTimes(2)
            .withStartupTimeout(java.time.Duration.of(60L, ChronoUnit.SECONDS))

        this
            .withSharedFiles(filesToBind)
            .withCreateContainerCmdModifier { it.withName(internalContainerName) }
            .withCreateContainerCmdModifier { it.withDomainName(internalDomain) }
            .withCreateContainerCmdModifier { it.withHostName(internalDomain) }
            .withNetwork(CurrentDockerNetwork.INTERN_NETWORK)
            .withNetworkMode(CurrentDockerNetwork.INTERN_NETWORK.id)
            .withNetworkAliases(internalDomain)
            .withExposedPorts(internalPort)
            .withEnv(environmentVariables)
            .waitingFor(waitStrategy)

        // addFixedExposedPort(INTERNAL_PORT, INTERNAL_PORT)
    }


    val jooqDslContext: DefaultDSLContext by lazy {
        DataAccessFactory(jdbcUrl = externalJdbcUrl(), userName = userName, password = password)
            .defaultDSLContext
    }


    override fun configure() {
        addEnv("POSTGRES_DB", databaseName)
        addEnv("POSTGRES_USER", userName)
        addEnv("POSTGRES_PASSWORD", password)
    }


    fun runSqlScript(sql: String) =

        jooqDslContext.fetch(sql)


    fun runMigrationScripts(vararg filePathsFromSourcesFolder: String) {

        filePathsFromSourcesFolder.toMutableList().forEach { filePath ->
            val path = Resources.getResource(filePath).path
            runSqlScript(File(path).readText(Charsets.UTF_8))
        }
    }


    fun externalHost() =

        containerIpAddress


    fun externalPort() =

        getMappedPort(internalPort)


    fun driverClassName(): String =

        "org.postgresql.Driver"


    fun internalJdbcUrl(): String =

        "jdbc:postgresql://$internalDomain:$internalPort/$databaseName"


    fun externalJdbcUrl(): String =

        "jdbc:postgresql://${this.externalHost()}:${this.externalPort()}/$databaseName"


    override fun start() {

        super.start()

        LOGGER.info("""
            |{
            |   "$internalContainerName": {
            |       "userName": "$userName",
            |       "password": "$password",
            |       "internal": {
            |           "host": "${this.internalDomain}",
            |           "port": ${this.internalPort},
            |           "jdbc_url": "${this.internalJdbcUrl()}",
            |       },
            |       "external": {
            |           "host": "${this.externalHost()}",
            |           "port": ${this.externalPort()},
            |           "jdbc_url": "${this.externalJdbcUrl()}",
            |       }
            |   }
            |}
        """.trimMargin())
    }


    companion object {

        private val LOGGER = LoggerFactory.getLogger(PostgreSQLContainer::class.java)

    }


}