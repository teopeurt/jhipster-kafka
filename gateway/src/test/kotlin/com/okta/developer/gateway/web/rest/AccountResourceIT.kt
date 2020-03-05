package com.okta.developer.gateway.web.rest

import com.okta.developer.gateway.GatewayApp
import com.okta.developer.gateway.config.TestSecurityConfiguration
import com.okta.developer.gateway.security.ADMIN
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

/**
 * Integration tests for the [AccountResource] REST controller.
 */
@AutoConfigureMockMvc
@WithMockUser(value = TEST_USER_LOGIN)
@SpringBootTest(classes = [GatewayApp::class, TestSecurityConfiguration::class])
class AccountResourceIT {

    @Autowired
    private lateinit var restAccountMockMvc: MockMvc

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testGetExistingAccount() {
        val userDetails = mapOf<String, Any>(
            "sub" to TEST_USER_LOGIN,
            "email" to "john.doe@jhipster.com"
        )
        val authorities = listOf(SimpleGrantedAuthority(ADMIN))
        val user = DefaultOAuth2User(authorities, userDetails, "sub")
        val authentication = OAuth2AuthenticationToken(user, authorities, "oidc")
        TestSecurityContextHolder.getContext().authentication = authentication

        restAccountMockMvc.perform(
            get("/api/account")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("\$.login").value(TEST_USER_LOGIN))
            .andExpect(jsonPath("\$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("\$.authorities").value(ADMIN))
    }

    @Test
    @Throws(Exception::class)
    fun testGetUnknownAccount() {
        restAccountMockMvc.perform(
            get("/api/account")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isInternalServerError)
    }
}
