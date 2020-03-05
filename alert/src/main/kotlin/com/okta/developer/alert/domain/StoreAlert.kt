package com.okta.developer.alert.domain

import java.io.Serializable
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * A StoreAlert.
 */
@Entity
@Table(name = "store_alert")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class StoreAlert(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @get: NotNull
    @Column(name = "store_name", nullable = false)
    var storeName: String? = null,

    @get: NotNull
    @Column(name = "store_status", nullable = false)
    var storeStatus: String? = null,

    @get: NotNull
    @Column(name = "timestamp", nullable = false)
    var timestamp: Instant? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoreAlert) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "StoreAlert{" +
        "id=$id" +
        ", storeName='$storeName'" +
        ", storeStatus='$storeStatus'" +
        ", timestamp='$timestamp'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
