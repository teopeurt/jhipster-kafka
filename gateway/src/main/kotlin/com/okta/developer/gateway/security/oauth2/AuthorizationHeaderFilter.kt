package com.okta.developer.gateway.security.oauth2

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.okta.developer.gateway.client.TokenRelayRequestInterceptor
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE
import org.springframework.core.Ordered

class AuthorizationHeaderFilter(private val headerUtil: AuthorizationHeaderUtil) : ZuulFilter() {

    override fun filterType(): String = PRE_TYPE

    override fun filterOrder(): Int = Ordered.LOWEST_PRECEDENCE

    override fun shouldFilter(): Boolean = true

    override fun run(): Any? {
        val ctx = RequestContext.getCurrentContext()
        val authorizationHeader = headerUtil.getAuthorizationHeader()
        ctx.addZuulRequestHeader(TokenRelayRequestInterceptor.AUTHORIZATION, authorizationHeader)
        return null
    }
}
