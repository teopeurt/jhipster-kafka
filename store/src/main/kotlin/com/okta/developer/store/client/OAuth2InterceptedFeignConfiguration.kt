package com.okta.developer.store.client

import com.okta.developer.store.security.oauth2.AuthorizationHeaderUtil
import org.springframework.context.annotation.Bean

class OAuth2InterceptedFeignConfiguration {

    @Bean(name = ["oauth2RequestInterceptor"])
    fun getOAuth2RequestInterceptor(authorizationHeaderUtil: AuthorizationHeaderUtil) =
        TokenRelayRequestInterceptor(authorizationHeaderUtil)
}
