package dev.altencir.inventory;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
    @Test
    void webLayerDoesNotAccessRepositoriesOrJpa() {
        var classes = new ClassFileImporter().importPackages("dev.altencir.inventory");
        noClasses().that().resideInAPackage("..web..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "jakarta.persistence..")
                .check(classes);
    }

    @Test
    void domainDoesNotDependOnSpringOrWeb() {
        var classes = new ClassFileImporter().importPackages("dev.altencir.inventory");
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "..web..")
                .check(classes);
    }
}
