package com.okta.developer.alert.web.rest

import com.okta.developer.alert.AlertApp
import com.okta.developer.alert.config.TestSecurityConfiguration
import com.okta.developer.alert.domain.StoreAlert
import com.okta.developer.alert.repository.StoreAlertRepository
import com.okta.developer.alert.web.rest.errors.ExceptionTranslator
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
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
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator

/**
 * Integration tests for the [StoreAlertResource] REST controller.
 *
 * @see StoreAlertResource
 */
@SpringBootTest(classes = [AlertApp::class, TestSecurityConfiguration::class])
class StoreAlertResourceIT {

    @Autowired
    private lateinit var storeAlertRepository: StoreAlertRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var validator: Validator

    private lateinit var restStoreAlertMockMvc: MockMvc

    private lateinit var storeAlert: StoreAlert

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val storeAlertResource = StoreAlertResource(storeAlertRepository)
        this.restStoreAlertMockMvc = MockMvcBuilders.standaloneSetup(storeAlertResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        storeAlert = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createStoreAlert() {
        val databaseSizeBeforeCreate = storeAlertRepository.findAll().size

        // Create the StoreAlert
        restStoreAlertMockMvc.perform(
            post("/api/store-alerts")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(storeAlert))
        ).andExpect(status().isCreated)

        // Validate the StoreAlert in the database
        val storeAlertList = storeAlertRepository.findAll()
        assertThat(storeAlertList).hasSize(databaseSizeBeforeCreate + 1)
        val testStoreAlert = storeAlertList[storeAlertList.size - 1]
        assertThat(testStoreAlert.storeName).isEqualTo(DEFAULT_STORE_NAME)
        assertThat(testStoreAlert.storeStatus).isEqualTo(DEFAULT_STORE_STATUS)
        assertThat(testStoreAlert.timestamp).isEqualTo(DEFAULT_TIMESTAMP)
    }

    @Test
    @Transactional
    fun createStoreAlertWithExistingId() {
        val databaseSizeBeforeCreate = storeAlertRepository.findAll().size

        // Create the StoreAlert with an existing ID
        storeAlert.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restStoreAlertMockMvc.perform(
            post("/api/store-alerts")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(storeAlert))
        ).andExpect(status().isBadRequest)

        // Validate the StoreAlert in the database
        val storeAlertList = storeAlertRepository.findAll()
        assertThat(storeAlertList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun checkStoreNameIsRequired() {
        val databaseSizeBeforeTest = storeAlertRepository.findAll().size
        // set the field null
        storeAlert.storeName = null

        // Create the StoreAlert, which fails.

        restStoreAlertMockMvc.perform(
            post("/api/store-alerts")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(storeAlert))
        ).andExpect(status().isBadRequest)

        val storeAlertList = storeAlertRepository.findAll()
        assertThat(storeAlertList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkStoreStatusIsRequired() {
        val databaseSizeBeforeTest = storeAlertRepository.findAll().size
        // set the field null
        storeAlert.storeStatus = null

        // Create the StoreAlert, which fails.

        restStoreAlertMockMvc.perform(
            post("/api/store-alerts")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(storeAlert))
        ).andExpect(status().isBadRequest)

        val storeAlertList = storeAlertRepository.findAll()
        assertThat(storeAlertList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkTimestampIsRequired() {
        val databaseSizeBeforeTest = storeAlertRepository.findAll().size
        // set the field null
        storeAlert.timestamp = null

        // Create the StoreAlert, which fails.

        restStoreAlertMockMvc.perform(
            post("/api/store-alerts")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(storeAlert))
        ).andExpect(status().isBadRequest)

        val storeAlertList = storeAlertRepository.findAll()
        assertThat(storeAlertList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun getAllStoreAlerts() {
        // Initialize the database
        storeAlertRepository.saveAndFlush(storeAlert)

        // Get all the storeAlertList
        restStoreAlertMockMvc.perform(get("/api/store-alerts?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(storeAlert.id?.toInt())))
            .andExpect(jsonPath("$.[*].storeName").value(hasItem(DEFAULT_STORE_NAME)))
            .andExpect(jsonPath("$.[*].storeStatus").value(hasItem(DEFAULT_STORE_STATUS)))
            .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())))
    }

    @Test
    @Transactional
    fun getStoreAlert() {
        // Initialize the database
        storeAlertRepository.saveAndFlush(storeAlert)

        val id = storeAlert.id
        assertNotNull(id)

        // Get the storeAlert
        restStoreAlertMockMvc.perform(get("/api/store-alerts/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.storeName").value(DEFAULT_STORE_NAME))
            .andExpect(jsonPath("$.storeStatus").value(DEFAULT_STORE_STATUS))
            .andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP.toString()))
    }

    @Test
    @Transactional
    fun getNonExistingStoreAlert() {
        // Get the storeAlert
        restStoreAlertMockMvc.perform(get("/api/store-alerts/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateStoreAlert() {
        // Initialize the database
        storeAlertRepository.saveAndFlush(storeAlert)

        val databaseSizeBeforeUpdate = storeAlertRepository.findAll().size

        // Update the storeAlert
        val id = storeAlert.id
        assertNotNull(id)
        val updatedStoreAlert = storeAlertRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedStoreAlert are not directly saved in db
        em.detach(updatedStoreAlert)
        updatedStoreAlert.storeName = UPDATED_STORE_NAME
        updatedStoreAlert.storeStatus = UPDATED_STORE_STATUS
        updatedStoreAlert.timestamp = UPDATED_TIMESTAMP

        restStoreAlertMockMvc.perform(
            put("/api/store-alerts")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedStoreAlert))
        ).andExpect(status().isOk)

        // Validate the StoreAlert in the database
        val storeAlertList = storeAlertRepository.findAll()
        assertThat(storeAlertList).hasSize(databaseSizeBeforeUpdate)
        val testStoreAlert = storeAlertList[storeAlertList.size - 1]
        assertThat(testStoreAlert.storeName).isEqualTo(UPDATED_STORE_NAME)
        assertThat(testStoreAlert.storeStatus).isEqualTo(UPDATED_STORE_STATUS)
        assertThat(testStoreAlert.timestamp).isEqualTo(UPDATED_TIMESTAMP)
    }

    @Test
    @Transactional
    fun updateNonExistingStoreAlert() {
        val databaseSizeBeforeUpdate = storeAlertRepository.findAll().size

        // Create the StoreAlert

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStoreAlertMockMvc.perform(
            put("/api/store-alerts")
                .contentType(APPLICATION_JSON)
                .content(convertObjectToJsonBytes(storeAlert))
        ).andExpect(status().isBadRequest)

        // Validate the StoreAlert in the database
        val storeAlertList = storeAlertRepository.findAll()
        assertThat(storeAlertList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteStoreAlert() {
        // Initialize the database
        storeAlertRepository.saveAndFlush(storeAlert)

        val databaseSizeBeforeDelete = storeAlertRepository.findAll().size

        val id = storeAlert.id
        assertNotNull(id)

        // Delete the storeAlert
        restStoreAlertMockMvc.perform(
            delete("/api/store-alerts/{id}", id)
                .accept(APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val storeAlertList = storeAlertRepository.findAll()
        assertThat(storeAlertList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_STORE_NAME = "AAAAAAAAAA"
        private const val UPDATED_STORE_NAME = "BBBBBBBBBB"

        private const val DEFAULT_STORE_STATUS = "AAAAAAAAAA"
        private const val UPDATED_STORE_STATUS = "BBBBBBBBBB"

        private val DEFAULT_TIMESTAMP: Instant = Instant.ofEpochMilli(0L)
        private val UPDATED_TIMESTAMP: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): StoreAlert {
            val storeAlert = StoreAlert(
                storeName = DEFAULT_STORE_NAME,
                storeStatus = DEFAULT_STORE_STATUS,
                timestamp = DEFAULT_TIMESTAMP
            )

            return storeAlert
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): StoreAlert {
            val storeAlert = StoreAlert(
                storeName = UPDATED_STORE_NAME,
                storeStatus = UPDATED_STORE_STATUS,
                timestamp = UPDATED_TIMESTAMP
            )

            return storeAlert
        }
    }
}
