package com.sparta.oms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 아키텍처 제약 조건을 기계적으로 강제한다.
 *
 * 이 테스트가 실패하면 레이어 의존성 규칙이 위반된 것이다.
 * @see docs/constraints/enforcement-map.md (ARCH-1, ARCH-2, ARCH-3)
 * @see docs/constraints/domain-rules.md
 */
@DisplayName("아키텍처 제약 조건 강제 검사")
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void loadClasses() {
        classes = new ClassFileImporter().importPackages("com.sparta.oms");
    }

    @Test
    @DisplayName("[ARCH-1] Controller는 Repository에 직접 의존할 수 없다")
    void controller_must_not_depend_on_repository_directly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("..repository..");

        rule.check(classes);
    }

    @Test
    @DisplayName("[ARCH-2] Repository는 Service나 Controller에 의존할 수 없다")
    void repository_must_not_depend_on_service_or_controller() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..service..", "..controller..");

        rule.check(classes);
    }

    @Test
    @DisplayName("[ARCH-2] Service는 Controller에 의존할 수 없다")
    void service_must_not_depend_on_controller() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat()
                .resideInAPackage("..controller..");

        rule.check(classes);
    }

    @Test
    @DisplayName("[ARCH-2] Controller 클래스는 'Controller' 접미사를 가져야 한다")
    void controllers_should_be_named_ending_with_controller() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().haveSimpleNameEndingWith("Controller");

        rule.check(classes);
    }

    @Test
    @DisplayName("[ARCH-2] Service 클래스는 'Service' 접미사를 가져야 한다")
    void services_should_be_named_ending_with_service() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areAnnotatedWith("org.springframework.stereotype.Service")
                .should().haveSimpleNameEndingWith("Service");

        rule.check(classes);
    }
}