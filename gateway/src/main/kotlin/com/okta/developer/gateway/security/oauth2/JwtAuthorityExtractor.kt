package com.okta.developer.gateway.security.oauth2

import com.okta.developer.gateway.security.extractAuthorityFromClaims
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class JwtAuthorityExtractor : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt) = extractAuthorityFromClaims(jwt.claims)
}
