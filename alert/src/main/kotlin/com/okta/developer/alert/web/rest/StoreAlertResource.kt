package com.okta.developer.alert.web.rest

import com.okta.developer.alert.domain.StoreAlert
import com.okta.developer.alert.repository.StoreAlertRepository
import com.okta.developer.alert.web.rest.errors.BadRequestAlertException
import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import javax.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val ENTITY_NAME = "alertStoreAlert"
/**
 * REST controller for managing [com.okta.developer.alert.domain.StoreAlert].
 */
@RestController
@RequestMapping("/api")
@Transactional
class StoreAlertResource(
    private val storeAlertRepository: StoreAlertRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /store-alerts` : Create a new storeAlert.
     *
     * @param storeAlert the storeAlert to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new storeAlert, or with status `400 (Bad Request)` if the storeAlert has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/store-alerts")
    fun createStoreAlert(@Valid @RequestBody storeAlert: StoreAlert): ResponseEntity<StoreAlert> {
        log.debug("REST request to save StoreAlert : {}", storeAlert)
        if (storeAlert.id != null) {
            throw BadRequestAlertException(
                "A new storeAlert cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = storeAlertRepository.save(storeAlert)
        return ResponseEntity.created(URI("/api/store-alerts/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /store-alerts` : Updates an existing storeAlert.
     *
     * @param storeAlert the storeAlert to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated storeAlert,
     * or with status `400 (Bad Request)` if the storeAlert is not valid,
     * or with status `500 (Internal Server Error)` if the storeAlert couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/store-alerts")
    fun updateStoreAlert(@Valid @RequestBody storeAlert: StoreAlert): ResponseEntity<StoreAlert> {
        log.debug("REST request to update StoreAlert : {}", storeAlert)
        if (storeAlert.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = storeAlertRepository.save(storeAlert)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     storeAlert.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /store-alerts` : get all the storeAlerts.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of storeAlerts in body.
     */
    @GetMapping("/store-alerts")
    fun getAllStoreAlerts(): MutableList<StoreAlert> {
        log.debug("REST request to get all StoreAlerts")
        return storeAlertRepository.findAll()
    }

    /**
     * `GET  /store-alerts/:id` : get the "id" storeAlert.
     *
     * @param id the id of the storeAlert to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the storeAlert, or with status `404 (Not Found)`.
     */
    @GetMapping("/store-alerts/{id}")
    fun getStoreAlert(@PathVariable id: Long): ResponseEntity<StoreAlert> {
        log.debug("REST request to get StoreAlert : {}", id)
        val storeAlert = storeAlertRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(storeAlert)
    }
    /**
     *  `DELETE  /store-alerts/:id` : delete the "id" storeAlert.
     *
     * @param id the id of the storeAlert to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/store-alerts/{id}")
    fun deleteStoreAlert(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete StoreAlert : {}", id)

        storeAlertRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
