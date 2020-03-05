package com.okta.developer.store.repository

import com.okta.developer.store.domain.Store
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data MongoDB repository for the [Store] entity.
 */
@Suppress("unused")
@Repository
interface StoreRepository : MongoRepository<Store, String>
