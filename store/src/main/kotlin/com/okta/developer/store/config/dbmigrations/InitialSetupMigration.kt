package com.okta.developer.store.config.dbmigrations

import com.github.mongobee.changeset.ChangeLog
import com.github.mongobee.changeset.ChangeSet
import com.okta.developer.store.domain.Authority
import com.okta.developer.store.security.ADMIN
import com.okta.developer.store.security.USER
import org.springframework.data.mongodb.core.MongoTemplate

/**
 * Creates the initial database setup.
 */
@ChangeLog(order = "001")
@Suppress("unused")
class InitialSetupMigration {

    @ChangeSet(order = "01", author = "initiator", id = "01-addAuthorities")
    fun addAuthorities(mongoTemplate: MongoTemplate) {
        val adminAuthority = Authority(ADMIN)
        val userAuthority = Authority(USER)

        mongoTemplate.save(adminAuthority)
        mongoTemplate.save(userAuthority)
    }
}
