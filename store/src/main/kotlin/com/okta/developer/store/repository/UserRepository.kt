package com.okta.developer.store.repository

import com.okta.developer.store.domain.User
import java.util.Optional
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data MongoDB repository for the [User] entity.
 */
@Repository
interface UserRepository : MongoRepository<User, String> {

    @Cacheable(cacheNames = [USERS_BY_EMAIL_CACHE])
    fun findOneByEmailIgnoreCase(email: String?): Optional<User>

    @Cacheable(cacheNames = [USERS_BY_LOGIN_CACHE])
    fun findOneByLogin(login: String): Optional<User>

    fun findAllByLoginNot(pageable: Pageable, login: String): Page<User>

    companion object {

        const val USERS_BY_LOGIN_CACHE: String = "usersByLogin"

        const val USERS_BY_EMAIL_CACHE: String = "usersByEmail"
    }
}
