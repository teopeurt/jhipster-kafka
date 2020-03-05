package com.okta.developer.store.domain

import com.okta.developer.store.domain.enumeration.StoreStatus
import java.io.Serializable
import java.time.Instant
import javax.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

/**
 * A Store.
 */
@Document(collection = "store")
class Store(

    @Id
    var id: String? = null,

    @get: NotNull
    @Field("name")
    var name: String? = null,

    @get: NotNull
    @Field("address")
    var address: String? = null,

    @Field("status")
    var status: StoreStatus? = null,

    @get: NotNull
    @Field("create_timestamp")
    var createTimestamp: Instant? = null,

    @Field("update_timestamp")
    var updateTimestamp: Instant? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Store) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Store{" +
        "id=$id" +
        ", name='$name'" +
        ", address='$address'" +
        ", status='$status'" +
        ", createTimestamp='$createTimestamp'" +
        ", updateTimestamp='$updateTimestamp'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
