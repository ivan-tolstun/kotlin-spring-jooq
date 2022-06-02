package de.tolstun.testcontainer.extension.impl.container

import de.tolstun.testcontainer.extension.impl.network.CurrentDockerNetwork
import de.tolstun.testcontainer.extension.api.container.BaseContainer
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait


class NewManContainer private constructor(private val imageName: String = "postman/newman",
                                          val internalContainerName: String,
                                          val internalDomain: String = "newman",
                                          val internalPort: Int = 8080,
                                          val postmanCollectionFromSourcesFolder: String,
                                          val postmanEnvironmentFromSourcesFolder: String,
                                          private val environmentVariables: MutableMap<String, String>? = mutableMapOf(),
                                          private val filesToBind: Map<String, String>? = emptyMap())

    : GenericContainer<NewManContainer>(imageName), BaseContainer<NewManContainer> {


    init {

        this
            .withSharedFiles( mapOf(
                postmanCollectionFromSourcesFolder to "/etc/newman/test.postman_collection.json",
                postmanEnvironmentFromSourcesFolder to "/etc/newman/test.postman_environment.json"
            ) )
            .withSharedFiles(filesToBind)
            .withCreateContainerCmdModifier { it.withName(internalContainerName) }
            .withCreateContainerCmdModifier { it.withDomainName(internalDomain) }
            .withCreateContainerCmdModifier { it.withHostName(internalDomain) }
            .withNetwork(CurrentDockerNetwork.INTERN_NETWORK)
            .withNetworkMode(CurrentDockerNetwork.INTERN_NETWORK.id)
            .withNetworkAliases(internalDomain)
            .withExposedPorts(internalPort)
            .withEnv(

                environmentVariables
                    ?.takeIf { it.isNotEmpty() }
                    ?: mutableMapOf()
            )

            .withCommand("run",  "test.postman_collection.json", "-e", "test.postman_environment.json", "--bail")
            .waitingFor(Wait.defaultWaitStrategy())
    }


    override fun start() {

        super.start()

        LOGGER.info("""
            |{
            |   "$internalContainerName": {
            |       "internal": {
            |           "host": "${this.internalDomain}",
            |           "port": "${this.internalPort}",
            |       }
            |   }
            |}
        """.trimMargin())
    }


    companion object {


        private val LOGGER = LoggerFactory.getLogger(NewManContainer::class.java)


        fun runCollection(internalContainerName: String,
                          postmanCollectionFromSourcesFolder: String,
                          postmanEnvironmentFromSourcesFolder: String): Boolean {

            var newmanContainer = NewManContainer(
                internalContainerName = internalContainerName,
                postmanCollectionFromSourcesFolder = postmanCollectionFromSourcesFolder,
                postmanEnvironmentFromSourcesFolder = postmanEnvironmentFromSourcesFolder
            )

            try {

                newmanContainer.start()

                return newmanContainer.logs
                    .let {
                        LOGGER.info(it)
                        !it.contains("#  failure")
                    }

            } finally { newmanContainer.stop() }

        }


    }


}