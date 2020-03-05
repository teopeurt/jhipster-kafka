package com.okta.developer.alert.web.rest

import com.okta.developer.alert.config.KafkaProperties
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/alert-kafka")
class AlertKafkaResource(
    private val kafkaProperties: KafkaProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var producer: KafkaProducer<String, String>
    private lateinit var sseExecutorService: ExecutorService

    init {
        producer = KafkaProducer<String, String>(kafkaProperties.getProducerProps())
        sseExecutorService = Executors.newCachedThreadPool()
    }

    @PostMapping("/publish/{topic}")
    @Throws(*[ExecutionException::class, InterruptedException::class])
    fun publish(@PathVariable topic: String, @RequestParam message: String, @RequestParam(required = false) key: String?): PublishResult {
        log.debug("REST request to send to Kafka topic $topic with key $key the message : $message")
        val metadata = producer.send(ProducerRecord(topic, key, message)).get()
        return PublishResult(metadata.topic(), metadata.partition(), metadata.offset(), Instant.ofEpochMilli(metadata.timestamp()))
    }

    @GetMapping("/consume")
    fun consume(@RequestParam("topic") topics: List<String>, @RequestParam consumerParams: Map<String, String>): SseEmitter {
        log.debug("REST request to consume records from Kafka topics $topics")
        val consumerProps = kafkaProperties.getConsumerProps()
        consumerProps.putAll(consumerParams)
        consumerProps.remove("topic")

        val emitter = SseEmitter(0L)
        sseExecutorService.execute {
            val consumer = KafkaConsumer<String, String>(consumerProps)
            emitter.onCompletion(consumer::close)
            consumer.subscribe(topics)
            var exitLoop = false

            while (!exitLoop) {
                try {
                    val records = consumer.poll(Duration.ofSeconds(5L))
                    records.forEach { emitter.send(it.value()) }
                    emitter.send(SseEmitter.event().comment(""))
                } catch (ex: Exception) {
                    log.trace("Complete with error ${ex.message}", ex)
                    emitter.completeWithError(ex)
                    exitLoop = true
                }
            }
            consumer.close()
            emitter.complete()
        }
        return emitter
    }

    class PublishResult(
        val topic: String,
        val partition: Int,
        val offset: Long,
        val timestamp: Instant
    )
}
