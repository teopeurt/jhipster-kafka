package com.okta.developer.alert.domain

import com.okta.developer.alert.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StoreAlertTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(StoreAlert::class)
        val storeAlert1 = StoreAlert()
        storeAlert1.id = 1L
        val storeAlert2 = StoreAlert()
        storeAlert2.id = storeAlert1.id
        assertThat(storeAlert1).isEqualTo(storeAlert2)
        storeAlert2.id = 2L
        assertThat(storeAlert1).isNotEqualTo(storeAlert2)
        storeAlert1.id = null
        assertThat(storeAlert1).isNotEqualTo(storeAlert2)
    }
}
