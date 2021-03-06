package com.okta.developer.alert.service

import com.okta.developer.alert.AlertApp
import com.okta.developer.alert.config.ANONYMOUS_USER
import com.okta.developer.alert.config.DEFAULT_LANGUAGE
import com.okta.developer.alert.config.TestSecurityConfiguration
import com.okta.developer.alert.domain.User
import com.okta.developer.alert.repository.UserRepository
import com.okta.developer.alert.security.ANONYMOUS
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.transaction.annotation.Transactional

private const val DEFAULT_LOGIN = "johndoe"
private const val DEFAULT_EMAIL = "johndoe@localhost"
private const val DEFAULT_FIRSTNAME = "john"
private const val DEFAULT_LASTNAME = "doe"
private const val DEFAULT_IMAGEURL = "http://placehold.it/50x50"
private const val DEFAULT_LANGKEY = "dummy"

/**
 * Integration tests for [UserService].
 */
@SpringBootTest(classes = [AlertApp::class, TestSecurityConfiguration::class])
@Transactional
class UserServiceIT {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userService: UserService

    private lateinit var user: User

    private lateinit var userDetails: MutableMap<String, Any>

    @BeforeEach
    fun init() {
        user = User(
            login = DEFAULT_LOGIN,
            activated = true,
            email = DEFAULT_EMAIL,
            firstName = DEFAULT_FIRSTNAME,
            lastName = DEFAULT_LASTNAME,
            imageUrl = DEFAULT_IMAGEURL,
            langKey = DEFAULT_LANGKEY
        )
        userDetails = mutableMapOf(
            "sub" to DEFAULT_LOGIN,
            "email" to DEFAULT_EMAIL,
            "given_name" to DEFAULT_FIRSTNAME,
            "family_name" to DEFAULT_LASTNAME,
            "picture" to DEFAULT_IMAGEURL
        )
    }

    @Test
    @Transactional
    fun assertThatAnonymousUserIsNotGet() {
        user.id = ANONYMOUS_USER
        user.login = ANONYMOUS_USER
        if (!userRepository.findOneByLogin(ANONYMOUS_USER).isPresent) {
            userRepository.saveAndFlush(user)
        }
        val pageable = PageRequest.of(0, userRepository.count().toInt())
        val allManagedUsers = userService.getAllManagedUsers(pageable)
        assertNotNull(allManagedUsers)
        assertThat(allManagedUsers.content.stream()
            .noneMatch { user -> ANONYMOUS_USER == user.login })
            .isTrue()
    }

    @Test
    @Transactional
    fun testDefaultUserDetails() {
        val authentication = createMockOAuth2AuthenticationToken(userDetails)
        val userDTO = userService.getUserFromAuthentication(authentication)

        assertThat(userDTO.login).isEqualTo(DEFAULT_LOGIN)
        assertThat(userDTO.firstName).isEqualTo(DEFAULT_FIRSTNAME)
        assertThat(userDTO.lastName).isEqualTo(DEFAULT_LASTNAME)
        assertThat(userDTO.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(userDTO.activated).isTrue()
        assertThat(userDTO.langKey).isEqualTo(DEFAULT_LANGUAGE)
        assertThat(userDTO.imageUrl).isEqualTo(DEFAULT_IMAGEURL)
        assertThat(userDTO.authorities).contains(ANONYMOUS)
    }

    @Test
    @Transactional
    fun testUserDetailsWithUsername() {
        userDetails["preferred_username"] = "TEST"
        val authentication = createMockOAuth2AuthenticationToken(userDetails)
        val userDTO = userService.getUserFromAuthentication(authentication)
        assertThat(userDTO.login).isEqualTo("test")
    }

    @Test
    @Transactional
    fun testUserDetailsWithLangKey() {
        userDetails["langKey"] = DEFAULT_LANGKEY
        userDetails["locale"] = "en-US"
        val authentication = createMockOAuth2AuthenticationToken(userDetails)
        val userDTO = userService.getUserFromAuthentication(authentication)
        assertThat(userDTO.langKey).isEqualTo(DEFAULT_LANGKEY)
    }

    @Test
    @Transactional
    fun testUserDetailsWithLocale() {
        userDetails["locale"] = "it-IT"
        val authentication = createMockOAuth2AuthenticationToken(userDetails)
        val userDTO = userService.getUserFromAuthentication(authentication)
        assertThat(userDTO.langKey).isEqualTo("it")
    }

    @Test
    @Transactional
    fun testUserDetailsWithUSLocaleUnderscore() {
        userDetails["locale"] = "en_US"
        val authentication = createMockOAuth2AuthenticationToken(userDetails)
        val userDTO = userService.getUserFromAuthentication(authentication)
        assertThat(userDTO.langKey).isEqualTo("en")
    }

    @Test
    @Transactional
    fun testUserDetailsWithUSLocaleDash() {
        userDetails["locale"] = "en-US"
        val authentication = createMockOAuth2AuthenticationToken(userDetails)
        val userDTO = userService.getUserFromAuthentication(authentication)
        assertThat(userDTO.langKey).isEqualTo("en")
    }

    private fun createMockOAuth2AuthenticationToken(userDetails: Map<String, Any?>): OAuth2AuthenticationToken {
        val authorities = listOf(SimpleGrantedAuthority(ANONYMOUS))
        val usernamePasswordAuthenticationToken =
            UsernamePasswordAuthenticationToken(ANONYMOUS_USER, ANONYMOUS_USER, authorities)
        usernamePasswordAuthenticationToken.details = userDetails
        val user = DefaultOAuth2User(authorities, userDetails, "sub")

        return OAuth2AuthenticationToken(user, authorities, "oidc")
    }
}
