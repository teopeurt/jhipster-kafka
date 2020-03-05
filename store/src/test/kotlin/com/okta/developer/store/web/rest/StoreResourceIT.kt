package com.okta.developer.store.web.rest

import com.okta.developer.store.StoreApp
import com.okta.developer.store.config.TestSecurityConfiguration
import com.okta.developer.store.domain.Store
import com.okta.developer.store.domain.enumeration.StoreStatus
import com.okta.developer.store.repository.StoreRepository
import com.okta.developer.store.web.rest.errors.ExceptionTranslator
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.Validator

/**
 * Integration tests for the [StoreResource] REST controller.
 *
 * @see StoreResource
 */
@SpringBootTest(classes = [StoreApp::class, TestSecurityConfiguration::class])
class StoreResourceIT {

    @Autowired
    private lateinit var storeRepository: StoreRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var validator: Validator

    private lateinit var restStoreMockMvc: MockMvc

    private lateinit var store: Store

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val storeResource = StoreResource(storeRepository)
        this.restStoreMockMvc = MockMvcBuilders.standaloneSetup(storeResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        storeRepository.deleteAll()
        store = createEntity()
    }

    @Test
    @Throws(Exception::class)
    fun createStore() {
        val databaseSizeBeforeCreate = storeRepository.findAll().size

        // Create the Store
        restStoreMockMvc.perform(
            post("/api/stores")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(store))
        ).andExpect(status().isCreated)

        // Validate the Store in the database
        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeCreate + 1)
        val testStore = storeList[storeList.size - 1]
        assertThat(testStore.name).isEqualTo(DEFAULT_NAME)
        assertThat(testStore.address).isEqualTo(DEFAULT_ADDRESS)
        assertThat(testStore.status).isEqualTo(DEFAULT_STATUS)
        assertThat(testStore.createTimestamp).isEqualTo(DEFAULT_CREATE_TIMESTAMP)
        assertThat(testStore.updateTimestamp).isEqualTo(DEFAULT_UPDATE_TIMESTAMP)
    }

    @Test
    fun createStoreWithExistingId() {
        val databaseSizeBeforeCreate = storeRepository.findAll().size

        // Create the Store with an existing ID
        store.id = "existing_id"

        // An entity with an existing ID cannot be created, so this API call must fail
        restStoreMockMvc.perform(
            post("/api/stores")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(store))
        ).andExpect(status().isBadRequest)

        // Validate the Store in the database
        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    fun checkNameIsRequired() {
        val databaseSizeBeforeTest = storeRepository.findAll().size
        // set the field null
        store.name = null

        // Create the Store, which fails.

        restStoreMockMvc.perform(
            post("/api/stores")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(store))
        ).andExpect(status().isBadRequest)

        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    fun checkAddressIsRequired() {
        val databaseSizeBeforeTest = storeRepository.findAll().size
        // set the field null
        store.address = null

        // Create the Store, which fails.

        restStoreMockMvc.perform(
            post("/api/stores")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(store))
        ).andExpect(status().isBadRequest)

        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    fun checkCreateTimestampIsRequired() {
        val databaseSizeBeforeTest = storeRepository.findAll().size
        // set the field null
        store.createTimestamp = null

        // Create the Store, which fails.

        restStoreMockMvc.perform(
            post("/api/stores")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(store))
        ).andExpect(status().isBadRequest)

        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    fun getAllStores() {
        // Initialize the database
        storeRepository.save(store)

        // Get all the storeList
        restStoreMockMvc.perform(get("/api/stores?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(store.id)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].createTimestamp").value(hasItem(DEFAULT_CREATE_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].updateTimestamp").value(hasItem(DEFAULT_UPDATE_TIMESTAMP.toString())))
    }

    @Test
    fun getStore() {
        // Initialize the database
        storeRepository.save(store)

        val id = store.id
        assertNotNull(id)

        // Get the store
        restStoreMockMvc.perform(get("/api/stores/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.createTimestamp").value(DEFAULT_CREATE_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.updateTimestamp").value(DEFAULT_UPDATE_TIMESTAMP.toString()))
    }

    @Test
    fun getNonExistingStore() {
        // Get the store
        restStoreMockMvc.perform(get("/api/stores/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    fun updateStore() {
        // Initialize the database
        storeRepository.save(store)

        val databaseSizeBeforeUpdate = storeRepository.findAll().size

        // Update the store
        val id = store.id
        assertNotNull(id)
        val updatedStore = storeRepository.findById(id).get()
        updatedStore.name = UPDATED_NAME
        updatedStore.address = UPDATED_ADDRESS
        updatedStore.status = UPDATED_STATUS
        updatedStore.createTimestamp = UPDATED_CREATE_TIMESTAMP
        updatedStore.updateTimestamp = UPDATED_UPDATE_TIMESTAMP

        restStoreMockMvc.perform(
            put("/api/stores")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedStore))
        ).andExpect(status().isOk)

        // Validate the Store in the database
        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeUpdate)
        val testStore = storeList[storeList.size - 1]
        assertThat(testStore.name).isEqualTo(UPDATED_NAME)
        assertThat(testStore.address).isEqualTo(UPDATED_ADDRESS)
        assertThat(testStore.status).isEqualTo(UPDATED_STATUS)
        assertThat(testStore.createTimestamp).isEqualTo(UPDATED_CREATE_TIMESTAMP)
        assertThat(testStore.updateTimestamp).isEqualTo(UPDATED_UPDATE_TIMESTAMP)
    }

    @Test
    fun updateNonExistingStore() {
        val databaseSizeBeforeUpdate = storeRepository.findAll().size

        // Create the Store

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStoreMockMvc.perform(
            put("/api/stores")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(store))
        ).andExpect(status().isBadRequest)

        // Validate the Store in the database
        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    fun deleteStore() {
        // Initialize the database
        storeRepository.save(store)

        val databaseSizeBeforeDelete = storeRepository.findAll().size

        val id = store.id
        assertNotNull(id)

        // Delete the store
        restStoreMockMvc.perform(
            delete("/api/stores/{id}", id)
                .accept(APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private const val DEFAULT_ADDRESS = "AAAAAAAAAA"
        private const val UPDATED_ADDRESS = "BBBBBBBBBB"

        private val DEFAULT_STATUS: StoreStatus = StoreStatus.OPEN
        private val UPDATED_STATUS: StoreStatus = StoreStatus.CLOSED

        private val DEFAULT_CREATE_TIMESTAMP: Instant = Instant.ofEpochMilli(0L)
        private val UPDATED_CREATE_TIMESTAMP: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        private val DEFAULT_UPDATE_TIMESTAMP: Instant = Instant.ofEpochMilli(0L)
        private val UPDATED_UPDATE_TIMESTAMP: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(): Store {
            val store = Store(
                name = DEFAULT_NAME,
                address = DEFAULT_ADDRESS,
                status = DEFAULT_STATUS,
                createTimestamp = DEFAULT_CREATE_TIMESTAMP,
                updateTimestamp = DEFAULT_UPDATE_TIMESTAMP
            )

            return store
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(): Store {
            val store = Store(
                name = UPDATED_NAME,
                address = UPDATED_ADDRESS,
                status = UPDATED_STATUS,
                createTimestamp = UPDATED_CREATE_TIMESTAMP,
                updateTimestamp = UPDATED_UPDATE_TIMESTAMP
            )

            return store
        }
    }
}
