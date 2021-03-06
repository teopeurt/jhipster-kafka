package com.okta.developer.store

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchTest {

    @Test
    fun servicesAndRepositoriesShouldNotDependOnWebLayer() {

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.okta.developer.store")

        noClasses()
            .that()
                .resideInAnyPackage("com.okta.developer.store.service..")
            .or()
                .resideInAnyPackage("com.okta.developer.store.repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("..com.okta.developer.store.web..")
        .because("Services and repositories should not depend on web layer")
        .check(importedClasses)
    }
}
