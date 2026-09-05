package app.jobzy.api.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ArchitectureTest {
  JavaClasses classes =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("app.jobzy.api");

  @Test
  @DisplayName("Application is free of Cycles")
  void jobzy_isFreeOf_Cycles() {
    ArchRule myRule = slices().matching("app.jobzy.api.(*)..").should().beFreeOfCycles();
    myRule.check(classes);
  }

  @Test
  @DisplayName("Domain Classes do not rely on classes in Application or Adapter layer ")
  void domainClasses_doesNotRelyOn_ApplicationOrAdapter() {
    ArchRule myRule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..application..", "..adapter..");

    myRule.check(classes);
  }

  @Test
  @DisplayName(
      "Domain Classes only depends on java and shared packages, only uuid generation allowed")
  void domainClasses_doNot_haveDependencies_onFrameworks() {
    ArchRule myRule =
        classes()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                "java..",
                "app.jobzy.api.shared..",
                "app.jobzy.api.domain..",
                "com.fasterxml.uuid..");
    myRule.check(classes);
  }

  @Test
  @DisplayName("Application Classes only depend on domain, application and shared packages")
  void applicationClasses_onlyDependOn_DomainApplicationAndShared() {
    ArchRule myRule =
        noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..adapter..");
    myRule.check(classes);
  }
}
