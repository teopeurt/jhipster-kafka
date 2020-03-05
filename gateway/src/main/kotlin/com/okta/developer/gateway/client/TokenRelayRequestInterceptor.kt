package com.okta.developer.gateway.client

import com.okta.developer.gateway.security.oauth2.AuthorizationHeaderUtil
import feign.RequestInterceptor
import feign.RequestTemplate

class TokenRelayRequestInterceptor(private val authorizationHeaderUtil: AuthorizationHeaderUtil) : RequestInterceptor {

    override fun apply(template: RequestTemplate) {
        template.header(AUTHORIZATION, authorizationHeaderUtil.getAuthorizationHeader())
    }

    companion object {
        const val AUTHORIZATION: String = "Authorization"
    }
}
