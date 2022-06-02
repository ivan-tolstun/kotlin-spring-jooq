package de.tolstun.testcontainer.extension.impl.container

import com.github.dockerjava.api.model.HostConfig
import de.tolstun.testcontainer.extension.impl.network.CurrentDockerNetwork
import de.tolstun.testcontainer.extension.api.container.BaseRestApiContainer
import de.tolstun.testcontainer.extension.impl.client.RestApiClient
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.WaitStrategy
import org.testcontainers.images.builder.ImageFromDockerfile
import java.io.File
import java.io.FileNotFoundException


class JavaRestApiContainer(private val jarFile: File,
                           val internalContainerName: String,
                           val internalDomain: String,
                           val internalHttpPort: Int,
                           val internalHttpsPort: Int?,
                           val internalDebugPort: Int?,
                           val jwtKey: String,
                           private val environmentVariables: MutableMap<String, String>,
                           private val filesToBind: Map<String, String>? = emptyMap(),
                           private val _waitStrategy: WaitStrategy,
                           private val configureHost: (HostConfig) -> HostConfig = { it } )

    : GenericContainer<JavaRestApiContainer>(
            buildDockerImage(internalContainerName = internalContainerName, jarFile = jarFile, internalDebugPort = internalDebugPort)),
      BaseRestApiContainer<JavaRestApiContainer> {


    init {

        withCreateContainerCmdModifier { cmd ->
            cmd.withHostConfig(configureHost(cmd.hostConfig ?: HostConfig()))
        }

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

        internalHttpsPort
            ?.let { getMappedPort(it) }


    fun externalDebugPort() =

        internalDebugPort
            ?.let { getMappedPort(it) }


    companion object {


        private val LOGGER = LoggerFactory.getLogger(JavaRestApiContainer::class.java)


        fun buildDockerImage(internalContainerName: String,
                             jarFile: File,
                             baseImageName: String = "docker.exceet-secure-solutions.de/ess/java-11-openjdk-centos",
                             internalDebugPort: Int?): ImageFromDockerfile {

            val jarFilePath = jarFile
                .takeIf { it.exists() && it.name.endsWith(".jar", true) }
                ?.toPath()
                ?: throw FileNotFoundException("Jar file not found")

            val debugCommand =
                if(internalDebugPort != null)
                    "-Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$internalDebugPort"
                else ""

            return ImageFromDockerfile(internalContainerName)
                .withDockerfileFromBuilder { builder ->

                    builder
                        .from(baseImageName)
                        .workDir("/app")
                        .copy("/tmp/app.jar", "/app/app.jar")
                        .cmd("java $debugCommand -jar app.jar")
                        .build()
                }
                .withFileFromPath("/tmp/app.jar", jarFilePath)
        }


    }


}