package de.tolstun.testcontainer.extension.impl.container

import de.tolstun.testcontainer.extension.impl.network.CurrentDockerNetwork
import de.tolstun.testcontainer.extension.api.container.BaseRestApiContainer
import de.tolstun.testcontainer.extension.impl.client.RestApiClient
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.WaitStrategy


class RestApiContainer(private val imageName: String,
                       val internalContainerName: String,
                       val internalDomain: String,
                       val internalHttpPort: Int,
                       val internalHttpsPort: Int?,
                       val internalDebugPort: Int?,
                       val jwtKey: String,
                       private val environmentVariables: MutableMap<String, String>,
                       private val filesToBind: Map<String, String>? = emptyMap(),
                       private val _waitStrategy: WaitStrategy)

    : GenericContainer<RestApiContainer>(imageName), BaseRestApiContainer<RestApiContainer> {


    init {

        this
            .withSharedFiles(filesToBind)
            .withCreateContainerCmdModifier { it.withName(internalContainerName) }
            .withCreateContainerCmdModifier { it.withDomainName(internalDomain) }
            .withCreateContainerCmdModifier { it.withHostName(internalDomain) }
            .withNetwork(CurrentDockerNetwork.INTERN_NETWORK)
            .withNetworkMode(CurrentDockerNetwork.INTERN_NETWORK.id)
            .withNetworkAliases(internalDomain)
            .withExposedPorts(*listOfNotNull(internalHttpPort, internalHttpsPort, internalDebugPort).toTypedArray())
            .withEnv(environmentVariables)
            .waitingFor(_waitStrategy)

        // addFixedExposedPort(INTERNAL_PORT, INTERNAL_PORT)
    }


    override val restApiClient: RestApiClient by lazy {

        RestApiClient(host = externalHost(), port = externalHttpPort(), basePath = "", jwtKey = jwtKey)
    }


    override fun start() {

        super.start()

        LOGGER.info("""
            |{
            |   "$internalContainerName": {
            |       "internal": {
            |           "host": "${this.internalDomain}",
            |           "http_port": ${this.internalHttpsPort},
            |           "https_port": ${this.internalHttpsPort},
            |           "debug_port": ${this.internalDebugPort}
            |       },
            |       "external": {
            |           "host": "${this.externalHost()}",
            |           "http_port": ${this.externalHttpPort()},
            |           "https_port": ${this.externalHttpsPort()},
            |           "debug_port": ${this.externalDebugPort()}
            |       }
            |   }
            |}
        """.trimMargin())
    }


    fun externalHost() =

        containerIpAddress


    fun externalHttpPort() =

        getMappedPort(internalHttpPort)


    fun externalHttpsPort() =

        internalHttpsPort?.let { getMappedPort(it) }


    fun externalDebugPort() =

        internalDebugPort?.let { getMappedPort(it) }


    companion object {

        val LOGGER = LoggerFactory.getLogger(RestApiContainer::class.java)

    }


}