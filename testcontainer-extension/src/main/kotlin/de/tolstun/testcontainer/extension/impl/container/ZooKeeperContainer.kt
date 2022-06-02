package de.tolstun.testcontainer.extension.impl.container

import de.tolstun.testcontainer.extension.api.container.BaseContainer
import de.tolstun.testcontainer.extension.impl.network.CurrentDockerNetwork
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait


class ZooKeeperContainer(private val imageName: String = "docker.io/bitnami/zookeeper:3-debian-10",
                         val internalContainerName: String,
                         val internalDomain: String = "zookeeper",
                         val internalPort: Int = 2181,
                         private val environmentVariables: MutableMap<String, String>? = mutableMapOf(),
                         private val filesToBind: Map<String, String>? = emptyMap())

    : GenericContainer<ZooKeeperContainer>(imageName), BaseContainer<ZooKeeperContainer> {


    init {

        this
            .withSharedFiles(filesToBind)
            .withCreateContainerCmdModifier { it.withName(internalContainerName) }
            .withCreateContainerCmdModifier { it.withDomainName(internalDomain) }
            .withCreateContainerCmdModifier { it.withHostName(internalDomain) }
            .withNetwork(CurrentDockerNetwork.INTERN_NETWORK)
            .withNetworkMode(CurrentDockerNetwork.INTERN_NETWORK.id)
            .withNetworkAliases(internalDomain)
            .withExposedPorts(internalPort, 2888, 3888, 8080)
            .withEnv(

                environmentVariables
                    ?.takeIf { it.isNotEmpty() }
                    ?: mutableMapOf("ALLOW_ANONYMOUS_LOGIN" to "yes")
            )
            .waitingFor(Wait.forLogMessage(".*started.*\\n", 1))

        // addFixedExposedPort(INTERNAL_PORT, INTERNAL_PORT)
    }


    fun externalHost() =

        this.containerIpAddress


    fun externalPort() =

        this.getMappedPort(internalPort)


    override fun start() {

        super.start()

        LOGGER.info("""
            |{
            |   "$internalContainerName": {
            |       "internal": {
            |           "host": "${this.internalDomain}",
            |           "port": ${this.internalPort},
            |       },
            |       "external": {
            |           "host": "${this.externalHost()}",
            |           "port": ${this.externalPort()},
            |       }
            |   }
            |}
        """.trimMargin())
    }


    companion object {

        private val LOGGER = LoggerFactory.getLogger(ZooKeeperContainer::class.java)

    }


}