package de.tolstun.testcontainer.extension.impl.client

import de.tolstun.testcontainer.extension.async.Waiting
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.kafka.client.common.PartitionInfo
import io.vertx.kafka.client.consumer.KafkaConsumer
import io.vertx.kafka.client.consumer.KafkaConsumerRecords
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kafka.client.producer.RecordMetadata
import org.apache.commons.lang3.ObjectUtils
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.random.Random


class KafkaClient(val kafkaHost: String, 
                  val kafkaPort: Int) {


    val vertx = Vertx.vertx(VertxOptions().setWorkerPoolSize(40))

    // Save messages locally (not critical for tests)
    // !!! This method should not be used in a real service !!!
    private val messages = mutableMapOf<String, MutableList<ConsumerRecord<String, String>>>()

    private var kafkaConsumer: KafkaConsumer<String, String>? = null

    private val kafkaProducer: KafkaProducer<String, String> by lazy {
        KafkaProducer.create(vertx, kafkaProducerConfig(), String::class.java, String::class.java)
    }


    private val kafkaConsumerConfig = { mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "${kafkaHost}:${kafkaPort}",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
        ConsumerConfig.GROUP_ID_CONFIG to "kafka-client-" + Random.nextInt(0, 10000),
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
        ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG to "true",
        ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG to "100000",
        ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to "300000",
        ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG to "300000"
    ) }


    private val kafkaProducerConfig = { mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "${kafkaHost}:${kafkaPort}",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.CLIENT_ID_CONFIG to "kafka-client-" + Random.nextInt(0, 10000),
        ProducerConfig.ACKS_CONFIG to "1"
    ) }


    @Synchronized
    fun pullMessagesFromCache(topic: String): List<ConsumerRecord<String, String>> =

        messages[topic]?.let { foundList ->

            val result =  ObjectUtils.clone(foundList).toList()
            foundList.clear()
            result

        } ?: emptyList()


    @Synchronized
    fun countMessagesFromCache(topic: String): Int =

        messages[topic]?.count() ?: 0


    @Synchronized
    private fun pushMessagesToCache(topic: String, 
                                    vararg newMessages: ConsumerRecord<String, String>) {

        if(messages.contains(topic)) messages[topic]?.addAll(newMessages)
        else messages[topic] = newMessages.toMutableList()
    }


    fun listenKafka(periodic: Long = 1000, 
                    vararg topics: String? = emptyArray()): Future<Void> {

        val kafkaConsumer = refreshConsumer()

        val successHandlerProcess = { handler: KafkaConsumerRecords<String, String> ->

            LOGGER.info("""
                |Receiving messages from kafka: 
                |${handler.records().joinToString(", ")}
            """.trimMargin())

            handler
                .records()
                .takeIf { !it.isEmpty }
                ?.also { records ->

                    records.map {
                        // Save messages locally (not critical for tests)
                        // !!! This method should not be used in a real service !!!
                        pushMessagesToCache(it.topic(), it)
                    }
                }

            kafkaConsumer.commit()
        }

        val faultHandlerProcess = { timerId: Long, cause: Throwable ->
            LOGGER.error("Something went wrong when polling $cause")
            cause.printStackTrace() // Stop polling if something went wrong
            vertx.cancelTimer(timerId)
        }

        val messageHandlerProcess = { timerId: Long ->
            kafkaConsumer
                .poll(Duration.ofMillis(100))
                .onSuccess { successHandlerProcess(it) }
                .onFailure { cause ->  faultHandlerProcess(timerId, cause) }
        }


        return kafkaConsumer
            .let {
                if (topics.isNotEmpty()) it.subscribe(topics.toHashSet())
                else it.subscribe(Pattern.compile(".*"))
            }
            .onSuccess { vertx.setPeriodic(periodic) { timerId -> messageHandlerProcess(timerId) } }
    }


    fun sendMessage(topic: String,
                    key: String? = null,
                    value: String): Future<RecordMetadata> = kafkaProducer.send(

        if(key != null) KafkaProducerRecord.create(topic, key, value)
        else KafkaProducerRecord.create(topic, value)
    )


    fun listTopics(): Future<MutableMap<String, MutableList<PartitionInfo>>> =

        (kafkaConsumer ?: refreshConsumer()).listTopics()


    fun refreshConsumer(): KafkaConsumer<String, String> {

        val waiting = Waiting()

        kafkaConsumer
            ?.close()
            ?.onComplete { waiting.completeNow(); }
            ?.onSuccess{ LOGGER.info("Consumer is now closed") }
            ?.onFailure { cause -> LOGGER.error("Close failed: $cause") }
            ?.also {
                waiting.awaitCompletion(5, TimeUnit.SECONDS)
                waiting.completeNow()
            }

        messages.clear()

        return KafkaConsumer
            .create(vertx, kafkaConsumerConfig(), String::class.java, String::class.java)
            .also { kafkaConsumer = it }
    }


    companion object {

        private val LOGGER = LoggerFactory.getLogger(KafkaClient::class.java)

    }


}