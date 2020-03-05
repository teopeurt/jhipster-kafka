package com.okta.developer.gateway.web.rest

import com.okta.developer.gateway.security.ADMIN
import com.okta.developer.gateway.web.rest.vm.RouteVM
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.netflix.zuul.filters.RouteLocator
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing Gateway configuration.
 */
@RestController
@RequestMapping("/api/gateway")
class GatewayResource(private val routeLocator: RouteLocator, private val discoveryClient: DiscoveryClient) {

    /**
     * `GET  /routes` : get the active routes.
     *
     * @return the [ResponseEntity] with status `200 (OK)` and with body the list of routes.
     */
    @GetMapping("/routes")
    @Secured(ADMIN)
    fun activeRoutes(): ResponseEntity<List<RouteVM>> {
        val routeVMs =
            routeLocator.routes.mapNotNull { route ->
                RouteVM(
                    path = route.fullPath,
                    serviceId = route.id,
                    serviceInstances = discoveryClient.getInstances(route.location)
                )
            }
        return ResponseEntity.ok(routeVMs)
    }
}
