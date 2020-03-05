package com.okta.developer.gateway.gateway.accesscontrol

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import io.github.jhipster.config.JHipsterProperties
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.zuul.filters.RouteLocator
import org.springframework.http.HttpStatus

/**
 * Zuul filter for restricting access to backend micro-services endpoints.
 */
class AccessControlFilter(
    private val routeLocator: RouteLocator,
    private val jHipsterProperties: JHipsterProperties
) : ZuulFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun filterType() = "pre"

    override fun filterOrder() = 0

    /**
     * Filter requests on endpoints that are not in the list of authorized microservices endpoints.
     */
    override fun shouldFilter(): Boolean {
        val requestUri = RequestContext.getCurrentContext().request.requestURI
        val contextPath = RequestContext.getCurrentContext().request.contextPath

        // If the request Uri does not start with the path of the authorized endpoints, we block the request
        for (route in routeLocator.routes) {
            val serviceUrl = contextPath + route.fullPath
            val serviceName = route.id

            // If this route correspond to the current request URI
            // We do a substring to remove the "**" at the end of the route URL
            if (requestUri.startsWith(serviceUrl.substring(0, serviceUrl.length - 2))) {
                return !isAuthorizedRequest(serviceUrl, serviceName, requestUri)
            }
        }
        return true
    }

    private fun isAuthorizedRequest(serviceUrl: String, serviceName: String, requestUri: String): Boolean {
        val authorizedMicroservicesEndpoints = jHipsterProperties.gateway.authorizedMicroservicesEndpoints

        // If the authorized endpoints list was left empty for this route, all access are allowed
        if (authorizedMicroservicesEndpoints[serviceName] == null) {
            log.debug(
                "Access Control: allowing access for {}, as no access control policy has been set up for service: {}",
                requestUri, serviceName
            )
            return true
        } else {
            val authorizedEndpoints = authorizedMicroservicesEndpoints[serviceName]

            if (authorizedEndpoints != null) {
                // Go over the authorized endpoints to control that the request URI matches it
                for (endpoint in authorizedEndpoints) {
                    // We do a substring to remove the "**/" at the end of the route URL
                    val gatewayEndpoint = serviceUrl.substring(0, serviceUrl.length - 3) + endpoint
                    if (requestUri.startsWith(gatewayEndpoint)) {
                        log.debug(
                            "Access Control: allowing access for {}, as it matches the following authorized microservice endpoint: {}",
                            requestUri, gatewayEndpoint
                        )
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun run(): Any? {
        RequestContext.getCurrentContext().apply {
            responseStatusCode = HttpStatus.FORBIDDEN.value()
            setSendZuulResponse(false)
            log.debug("Access Control: filtered unauthorized access on endpoint {}", request.requestURI)
        }
        return null
    }
}
