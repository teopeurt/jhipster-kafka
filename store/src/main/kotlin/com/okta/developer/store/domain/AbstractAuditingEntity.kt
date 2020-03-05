package com.okta.developer.store.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable
import java.time.Instant
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Field

/**
 * Base abstract class for entities which will hold definitions for created, last modified by and created,
 * last modified by date.
 */
abstract class AbstractAuditingEntity(

    @CreatedBy
    @Field("created_by")
    @JsonIgnore
    var createdBy: String? = null,

    @CreatedDate
    @Field("created_date")
    @JsonIgnore
    var createdDate: Instant? = Instant.now(),

    @LastModifiedBy
    @Field("last_modified_by")
    @JsonIgnore
    var lastModifiedBy: String? = null,

    @LastModifiedDate
    @Field("last_modified_date")
    @JsonIgnore
    var lastModifiedDate: Instant? = Instant.now()

) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }
}
