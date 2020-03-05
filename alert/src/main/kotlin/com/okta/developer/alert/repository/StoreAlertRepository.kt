package com.okta.developer.alert.repository

import com.okta.developer.alert.domain.StoreAlert
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [StoreAlert] entity.
 */
@Suppress("unused")
@Repository
interface StoreAlertRepository : JpaRepository<StoreAlert, Long>
