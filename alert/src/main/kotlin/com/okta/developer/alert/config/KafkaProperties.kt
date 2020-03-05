package com.okta.developer.alert.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "kafka", ignoreUnknownFields = true)
class KafkaProperties {

    private var bootStrapServers: String = "localhost:9092"

    private var consumer: MutableMap<String, Any> = hashMapOf()

    private var producer: MutableMap<String, Any> = hashMapOf()

    fun getBootStrapServers(): String {
        return bootStrapServers
    }

    fun setBootStrapServers(bootStrapServers: String) {
        this.bootStrapServers = bootStrapServers
    }

    fun getConsumerProps(): MutableMap<String, Any> {
        val properties = consumer
        if (!properties.containsKey("bootstrap.servers")) {
            properties["bootstrap.servers"] = bootStrapServers
        }
        return properties
    }

    fun setConsumer(consumer: MutableMap<String, Any>) {
        this.consumer = consumer
    }

    fun getProducerProps(): MutableMap<String, Any> {
        val properties = producer
        if (!properties.containsKey("bootstrap.servers")) {
            properties["bootstrap.servers"] = bootStrapServers
        }

        if (producer["key"] != null) {
            val key = producer["key"] as Map<String, Any>
            val serializer = key["serializer"]
            if (serializer != null) {
                properties["key.serializer"] = serializer
            }
        }

        if (producer["value"] != null) {
            val value = producer["value"] as Map<String, Any>
            val serializer = value["serializer"]
            if (serializer != null) {
                properties["value.serializer"] = serializer
            }
        }

        return properties
    }

    fun setProducer(producer: MutableMap<String, Any>) {
        this.producer = producer
    }
}
