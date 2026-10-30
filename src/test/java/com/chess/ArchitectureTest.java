package com.chess;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/** Fails when a production dependency points in the wrong direction. */
final class ArchitectureTest {

    private static final String DOMAIN = "com.chess.domain..";
    private static final String[] ENGINE_PACKAGES = {
        "com.chess.domain.command..",
        "com.chess.domain.movement..",
        "com.chess.domain.rule..",
        "com.chess.domain.draw..",
        "com.chess.domain.state..",
        "com.chess.domain.game.."
    };

    private static JavaClasses production;

    @BeforeAll
    static void importProductionClasses() {
        production = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.chess");
    }

    @Test
    void theCoreKnowsNeitherTheOuterLayersNorJavaFxNorJdbc() {
        noClasses().that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.chess.ui..", "com.chess.persistence..", "com.chess.configuration..",
                        "javafx..", "java.sql..")
                .check(production);
    }

    @Test
    void theUiKnowsNeitherPersistenceNorConfiguration() {
        noClasses().that().resideInAPackage("com.chess.ui..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.chess.persistence..", "com.chess.configuration..")
                .check(production);
    }

    @Test
    void persistenceKnowsNeitherTheUiNorConfiguration() {
        noClasses().that().resideInAPackage("com.chess.persistence..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.chess.ui..", "com.chess.configuration..")
                .check(production);
    }

    @Test
    void theUiAndPersistenceOnlyUseThePortsAndTheModel() {
        noClasses().that().resideInAnyPackage("com.chess.ui..", "com.chess.persistence..")
                .should().dependOnClassesThat().resideInAnyPackage(ENGINE_PACKAGES)
                .check(production);
    }

    @Test
    void thePortsAndTheModelDoNotKnowTheEngine() {
        noClasses().that().resideInAnyPackage("com.chess.domain.port..", "com.chess.domain.model..")
                .should().dependOnClassesThat().resideInAnyPackage(ENGINE_PACKAGES)
                .check(production);
    }

    @Test
    void theModelDoesNotKnowThePorts() {
        noClasses().that().resideInAPackage("com.chess.domain.model..")
                .should().dependOnClassesThat().resideInAPackage("com.chess.domain.port..")
                .check(production);
    }

    @Test
    void theDomainPackagesHaveNoCycles() {
        slices().matching("com.chess.domain.(*)..")
                .should().beFreeOfCycles()
                .check(production);
    }
}
