package com.okta.developer.gateway.config

import com.okta.developer.gateway.gateway.accesscontrol.AccessControlFilter
import com.okta.developer.gateway.gateway.responserewriting.SwaggerBasePathRewritingFilter
import io.github.jhipster.config.JHipsterProperties
import org.springframework.cloud.netflix.zuul.filters.RouteLocator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfiguration {

    @Configuration
    class SwaggerBasePathRewritingConfiguration {
        @Bean
        fun swaggerBasePathRewritingFilter() = SwaggerBasePathRewritingFilter()
    }

    @Configuration
    class AccessControlFilterConfiguration {
        @Bean
        fun accessControlFilter(routeLocator: RouteLocator, jHipsterProperties: JHipsterProperties) =
            AccessControlFilter(routeLocator, jHipsterProperties)
    }
}
