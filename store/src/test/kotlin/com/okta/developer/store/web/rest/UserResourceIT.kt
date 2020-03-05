package com.okta.developer.store.web.rest

import com.okta.developer.store.StoreApp
import com.okta.developer.store.config.TestSecurityConfiguration
import com.okta.developer.store.domain.Authority
import com.okta.developer.store.domain.User
import com.okta.developer.store.repository.UserRepository
import com.okta.developer.store.security.ADMIN
import com.okta.developer.store.security.USER
import com.okta.developer.store.service.dto.UserDTO
import com.okta.developer.store.service.mapper.UserMapper
import java.time.Instant
import java.util.UUID
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Integration tests for the [UserResource] REST controller.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = [ADMIN])
@SpringBootTest(classes = [StoreApp::class, TestSecurityConfiguration::class])
class UserResourceIT {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userMapper: UserMapper

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var restUserMockMvc: MockMvc

    private lateinit var user: User

    @BeforeEach
    fun setup() {
        cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)!!.clear()
        cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)!!.clear()
    }

    @BeforeEach
    fun initTest() {
        userRepository.deleteAll()
        user = createEntity()
    }

    @Test
    @Throws(Exception::class)
    fun getAllUsers() {
        // Initialize the database
        userRepository.save(user)

        // Get all the users
        restUserMockMvc.perform(
            get("/api/users")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("\$.[*].login").value(hasItem(DEFAULT_LOGIN)))
            .andExpect(jsonPath("\$.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
            .andExpect(jsonPath("\$.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
            .andExpect(jsonPath("\$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("\$.[*].imageUrl").value(hasItem(DEFAULT_IMAGEURL)))
            .andExpect(jsonPath("\$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)))
    }

    @Test
    @Throws(Exception::class)
    fun getUser() {
        // Initialize the database
        userRepository.save(user)

        assertNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)!!.get(user.login!!))

        // Get the user
        restUserMockMvc.perform(get("/api/users/{login}", user.login))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("\$.login").value(user.login!!))
            .andExpect(jsonPath("\$.firstName").value(DEFAULT_FIRSTNAME))
            .andExpect(jsonPath("\$.lastName").value(DEFAULT_LASTNAME))
            .andExpect(jsonPath("\$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("\$.imageUrl").value(DEFAULT_IMAGEURL))
            .andExpect(jsonPath("\$.langKey").value(DEFAULT_LANGKEY))

        assertNotNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)!!.get(user.login!!))
    }

    @Test
    @Throws(Exception::class)
    fun getNonExistingUser() {
        restUserMockMvc.perform(get("/api/users/unknown"))
            .andExpect(status().isNotFound)
    }

    @Test
    @Throws(Exception::class)
    fun testUserEquals() {
        equalsVerifier(User::class)
        val user1 = User(id = "id1")
        val user2 = User(id = user1.id)
        assertThat(user1).isEqualTo(user2)
        user2.id = "id2"
        assertThat(user1).isNotEqualTo(user2)
        user1.id = null
        assertThat(user1).isNotEqualTo(user2)
    }

    @Test
    fun testUserDTOtoUser() {
        val userDTO = UserDTO(
            id = DEFAULT_ID,
            login = DEFAULT_LOGIN,
            firstName = DEFAULT_FIRSTNAME,
            lastName = DEFAULT_LASTNAME,
            email = DEFAULT_EMAIL,
            activated = true,
            imageUrl = DEFAULT_IMAGEURL,
            langKey = DEFAULT_LANGKEY,
            createdBy = DEFAULT_LOGIN,
            lastModifiedBy = DEFAULT_LOGIN,
            authorities = setOf(USER)
        )

        val user = userMapper.userDTOToUser(userDTO)
        assertNotNull(user)
        assertThat(user.id).isEqualTo(DEFAULT_ID)
        assertThat(user.login).isEqualTo(DEFAULT_LOGIN)
        assertThat(user.firstName).isEqualTo(DEFAULT_FIRSTNAME)
        assertThat(user.lastName).isEqualTo(DEFAULT_LASTNAME)
        assertThat(user.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(user.activated).isEqualTo(true)
        assertThat(user.imageUrl).isEqualTo(DEFAULT_IMAGEURL)
        assertThat(user.langKey).isEqualTo(DEFAULT_LANGKEY)
        assertThat(user.createdBy).isNull()
        assertThat(user.createdDate).isNotNull()
        assertThat(user.lastModifiedBy).isNull()
        assertThat(user.lastModifiedDate).isNotNull()
        assertThat(user.authorities).extracting("name").containsExactly(USER)
    }

    @Test
    fun testUserToUserDTO() {
        user.id = DEFAULT_ID
        user.createdBy = DEFAULT_LOGIN
        user.createdDate = Instant.now()
        user.lastModifiedBy = DEFAULT_LOGIN
        user.lastModifiedDate = Instant.now()
        user.authorities = mutableSetOf(Authority(name = USER))

        val userDTO = userMapper.userToUserDTO(user)

        assertThat(userDTO.id).isEqualTo(DEFAULT_ID)
        assertThat(userDTO.login).isEqualTo(DEFAULT_LOGIN)
        assertThat(userDTO.firstName).isEqualTo(DEFAULT_FIRSTNAME)
        assertThat(userDTO.lastName).isEqualTo(DEFAULT_LASTNAME)
        assertThat(userDTO.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(userDTO.isActivated()).isEqualTo(true)
        assertThat(userDTO.imageUrl).isEqualTo(DEFAULT_IMAGEURL)
        assertThat(userDTO.langKey).isEqualTo(DEFAULT_LANGKEY)
        assertThat(userDTO.createdBy).isEqualTo(DEFAULT_LOGIN)
        assertThat(userDTO.createdDate).isEqualTo(user.createdDate)
        assertThat(userDTO.lastModifiedBy).isEqualTo(DEFAULT_LOGIN)
        assertThat(userDTO.lastModifiedDate).isEqualTo(user.lastModifiedDate)
        assertThat(userDTO.authorities).containsExactly(USER)
        assertThat(userDTO.toString()).isNotNull()
    }

    @Test
    fun testAuthorityEquals() {
        val authorityA = Authority()
        assertThat(authorityA).isEqualTo(authorityA)
        assertThat(authorityA).isNotEqualTo(null)
        assertThat(authorityA).isNotEqualTo(Any())
        assertThat(authorityA.hashCode()).isEqualTo(31)
        assertThat(authorityA.toString()).isNotNull()

        val authorityB = Authority()
        assertThat(authorityA.name).isEqualTo(authorityB.name)

        authorityB.name = ADMIN
        assertThat(authorityA).isNotEqualTo(authorityB)

        authorityA.name = USER
        assertThat(authorityA).isNotEqualTo(authorityB)

        authorityB.name = USER
        assertThat(authorityA).isEqualTo(authorityB)
        assertThat(authorityA.hashCode()).isEqualTo(authorityB.hashCode())
    }

    companion object {

        private const val DEFAULT_LOGIN = "johndoe"

        private const val DEFAULT_ID = "id1"

        private const val DEFAULT_EMAIL = "johndoe@localhost"

        private const val DEFAULT_FIRSTNAME = "john"

        private const val DEFAULT_LASTNAME = "doe"

        private const val DEFAULT_IMAGEURL = "http://placehold.it/50x50"

        private const val DEFAULT_LANGKEY = "en"

        /**
         * Create a User.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which has a required relationship to the User entity.
         */
        @JvmStatic
        fun createEntity(): User {
            return User(
                id = UUID.randomUUID().toString(),
                login = DEFAULT_LOGIN,
                activated = true,
                email = DEFAULT_EMAIL,
                firstName = DEFAULT_FIRSTNAME,
                lastName = DEFAULT_LASTNAME,
                imageUrl = DEFAULT_IMAGEURL,
                langKey = DEFAULT_LANGKEY
            )
        }
    }

    fun assertPersistedUsers(userAssertion: (List<User>) -> Unit) {
        userAssertion(userRepository.findAll())
    }
}
