package com.okta.developer.store.config

import com.github.mongobee.Mongobee
import com.mongodb.MongoClient
import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.domain.util.JSR310DateConverters.DateToZonedDateTimeConverter
import io.github.jhipster.domain.util.JSR310DateConverters.ZonedDateTimeToDateConverter
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

@Configuration
@EnableMongoRepositories("com.okta.developer.store.repository")
@Profile("!" + JHipsterConstants.SPRING_PROFILE_CLOUD)
@Import(value = [MongoAutoConfiguration::class])
@EnableMongoAuditing(auditorAwareRef = "springSecurityAuditorAware")
class DatabaseConfiguration {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun validatingMongoEventListener() = ValidatingMongoEventListener(validator())

    @Bean
    fun validator() = LocalValidatorFactoryBean()

    @Bean
    fun customConversions() =
        MongoCustomConversions(
            mutableListOf<Converter<*, *>>(
                DateToZonedDateTimeConverter.INSTANCE,
                ZonedDateTimeToDateConverter.INSTANCE
            )
        )

    @Bean
    fun mongobee(mongoClient: MongoClient, mongoTemplate: MongoTemplate, mongoProperties: MongoProperties): Mongobee {
        log.debug("Configuring Mongobee")
        return Mongobee(mongoClient).apply {
            setDbName(mongoProperties.mongoClientDatabase)
            setMongoTemplate(mongoTemplate)
            // package to scan for migrations
            setChangeLogsScanPackage("com.okta.developer.store.config.dbmigrations")
            isEnabled = true
        }
    }
}
