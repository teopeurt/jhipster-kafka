package com.okta.developer.store.web.rest.vm

import com.okta.developer.store.service.dto.UserDTO

/**
 * View Model extending the [UserDTO], which is meant to be used in the user management UI.
 */
class ManagedUserVM : UserDTO() {

    override fun toString() = "ManagedUserVM{${super.toString()}}"
}
