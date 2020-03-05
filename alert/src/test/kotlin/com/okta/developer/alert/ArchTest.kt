package com.okta.developer.alert

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchTest {

    @Test
    fun servicesAndRepositoriesShouldNotDependOnWebLayer() {

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.okta.developer.alert")

        noClasses()
            .that()
                .resideInAnyPackage("com.okta.developer.alert.service..")
            .or()
                .resideInAnyPackage("com.okta.developer.alert.repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("..com.okta.developer.alert.web..")
        .because("Services and repositories should not depend on web layer")
        .check(importedClasses)
    }
}
