package com.okta.developer.alert.client

import com.okta.developer.alert.security.oauth2.AuthorizationHeaderUtil
import org.springframework.context.annotation.Bean

class OAuth2InterceptedFeignConfiguration {

    @Bean(name = ["oauth2RequestInterceptor"])
    fun getOAuth2RequestInterceptor(authorizationHeaderUtil: AuthorizationHeaderUtil) =
        TokenRelayRequestInterceptor(authorizationHeaderUtil)
}
