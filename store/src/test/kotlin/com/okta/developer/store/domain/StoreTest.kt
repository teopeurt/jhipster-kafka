package com.okta.developer.store.domain

import com.okta.developer.store.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StoreTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Store::class)
        val store1 = Store()
        store1.id = "id1"
        val store2 = Store()
        store2.id = store1.id
        assertThat(store1).isEqualTo(store2)
        store2.id = "id2"
        assertThat(store1).isNotEqualTo(store2)
        store1.id = null
        assertThat(store1).isNotEqualTo(store2)
    }
}
