package com.okta.developer.store.repository

import com.okta.developer.store.domain.Authority
import org.springframework.data.mongodb.repository.MongoRepository

/**
 * Spring Data MongoDB repository for the [Authority] entity.
 */

interface AuthorityRepository : MongoRepository<Authority, String>
