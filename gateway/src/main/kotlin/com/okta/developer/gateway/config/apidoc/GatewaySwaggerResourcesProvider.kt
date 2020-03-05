package com.okta.developer.gateway.config.apidoc

import io.github.jhipster.config.JHipsterConstants
import org.springframework.cloud.netflix.zuul.filters.RouteLocator
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import springfox.documentation.swagger.web.SwaggerResource
import springfox.documentation.swagger.web.SwaggerResourcesProvider

/**
 * Retrieves all registered microservices Swagger resources.
 */
@Component
@Primary
@Profile(JHipsterConstants.SPRING_PROFILE_SWAGGER)
class GatewaySwaggerResourcesProvider(private val routeLocator: RouteLocator) : SwaggerResourcesProvider {

    override fun get(): MutableList<SwaggerResource> {
        val resources = mutableListOf(
            // Add the default swagger resource that correspond to the gateway's own swagger doc
            swaggerResource("default", "/v2/api-docs")
        )

        // Add the registered microservices swagger docs as additional swagger resources
        routeLocator.routes.forEach {
            resources.add(swaggerResource(it.id, it.fullPath.replace("**", "v2/api-docs")))
        }

        return resources
    }

    private fun swaggerResource(name: String, location: String) =
        SwaggerResource().apply {
            this.name = name
            this.location = location
            swaggerVersion = "2.0"
        }
}
