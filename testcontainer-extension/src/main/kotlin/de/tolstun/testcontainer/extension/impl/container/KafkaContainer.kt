package de.tolstun.testcontainer.extension.impl.container

import com.github.dockerjava.api.exception.DockerException
import de.tolstun.testcontainer.extension.impl.network.CurrentDockerNetwork
import de.tolstun.testcontainer.extension.api.container.BaseContainer
import de.tolstun.testcontainer.extension.async.Waiting
import de.tolstun.testcontainer.extension.impl.client.KafkaClient
import io.vertx.core.Future
import io.vertx.kafka.client.common.PartitionInfo
import io.vertx.kafka.client.producer.RecordMetadata
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.util.concurrent.TimeUnit


class KafkaContainer(private val imageName: String = "docker.io/bitnami/kafka:2-debian-10",
                     val internalContainerName: String,
                     val internalDomain: String = "kafka",
                     val internalPortInside: Int = 9092,
                     val internalPortOutside: Int = 19092,
                     private val environmentVariables: MutableMap<String, String>? = mutableMapOf(),
                     private val filesToBind: Map<String, String>? = emptyMap())

    : GenericContainer<KafkaContainer>(imageName), BaseContainer<KafkaContainer> {


    private var hostAddress = CurrentDockerNetwork.DOCKER_HOST.hostAddress

    private var freeExternalPort: Int = CurrentDockerNetwork.randomFreePort(internalPortOutside, internalPortOutside + 500)

    private val kafkaClient: KafkaClient by lazy {

        if(this.isRunning) KafkaClient(hostAddress, freeExternalPort)
        else throw DockerException("Container not running", 404)
    }


    init {

        this
            .withSharedFiles(filesToBind)
            .withCreateContainerCmdModifier { it.withName(internalContainerName) }
            .withCreateContainerCmdModifier { it.withDomainName(internalDomain) }
            .withCreateContainerCmdModifier { it.withHostName(internalDomain) }
            .withNetwork(CurrentDockerNetwork.INTERN_NETWORK)
            .withNetworkMode(CurrentDockerNetwork.INTERN_NETWORK.id)
            .withNetworkAliases(internalDomain)
            .withExposedPorts(internalPortInside, internalPortOutside)
            .withEnv(
                environmentVariables
                    ?.takeIf { it.isNotEmpty() }
                    ?: mutableMapOf(
                        "KAFKA_BROKER_ID" to "1",
                        "KAFKA_ZOOKEEPER_CONNECT" to "zookeeper:2181",
                        "ALLOW_PLAINTEXT_LISTENER" to "yes",
                        "KAFKA_LISTENERS" to "INSIDE://:$internalPortInside,OUTSIDE://:$internalPortOutside",
                        "KAFKA_ADVERTISED_LISTENERS" to "INSIDE://$internalDomain:$internalPortInside,OUTSIDE://$hostAddress:$freeExternalPort",
                        "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP" to "INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT",
                        "KAFKA_INTER_BROKER_LISTENER_NAME" to "INSIDE",
                        "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR" to "1",
                        "KAFKA_AUTO_CREATE_TOPICS_ENABLE" to "true")
            )
            .waitingFor(Wait.forLogMessage(".*Startup complete.*\\n", 1))

        addFixedExposedPort(freeExternalPort, internalPortOutside)
    }


    override fun start() {

        super.start()

        LOGGER.info("""
            |{
            |   "$internalContainerName": {
            |       "internal": {
            |           "host": "${this.internalDomain}",
            |           "port_inside": ${this.internalPortInside},
            |           "port_outside": "${this.internalPortOutside}",
            |       },
            |       "external": {
            |           "host": "${this.externalHost()}",
            |           "port": ${this.externalPort()},
            |       }
            |   }
            |}
        """.trimMargin())
    }


    fun listenKafka(vararg topic: String? = emptyArray()) =

        kafkaClient.listenKafka(topics = topic.toList().toTypedArray())


    fun listenKafka() =

        kafkaClient.listenKafka()


    fun listTopics(): Future<MutableMap<String, MutableList<PartitionInfo>>> =

        kafkaClient.listTopics()


    fun awaitMessages(topic: String,
                      timeout: Long = 5000,
                      unit: TimeUnit = TimeUnit.MILLISECONDS): List<ConsumerRecord<String, String>> {


        Waiting().waitProcess(timeout, unit) {
            kafkaClient.countMessagesFromCache(topic) > 0
        }

        return kafkaClient
            .pullMessagesFromCache(topic)
    }


    fun sendMessage(topic: String,
                    key: String? = null,
                    value: String): Future<RecordMetadata> =

        kafkaClient.sendMessage(topic = topic, key = key, value)


    fun externalHost() =

        containerIpAddress


    fun externalPort() =

        getMappedPort(internalPortOutside)


    companion object {

        private val LOGGER = LoggerFactory.getLogger(KafkaContainer::class.java)

    }


}