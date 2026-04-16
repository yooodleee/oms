package com.sparta.oms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 가드레일 시스템 — 아키텍처 경계, 도메인 격리, 인터페이스 계약을 기계적으로 강제한다.
 *
 * 이 테스트가 실패하면 구조적 규칙이 위반된 것이다.
 * 코드를 수정하지 않고 이 테스트를 비활성화하는 것은 금지된다.
 *
 * @see docs/guardrails/system.md
 * @see docs/guardrails/architecture-boundaries.md
 * @see docs/constraints/enforcement-map.md (ARCH-1 ~ ARCH-6)
 */
@DisplayName("가드레일: 아키텍처 경계 강제 검사")
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void loadClasses() {
        classes = new ClassFileImporter().importPackages("com.sparta.oms");
    }

    // ══════════════════════════════════════════════════════════
    // ARCH-1, ARCH-2: 레이어 의존성 방향
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[ARCH-1, ARCH-2] 레이어 의존성 방향")
    class LayerDependencyTests {

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
    }

    // ══════════════════════════════════════════════════════════
    // ARCH-3: 도메인 격리 (Domain Isolation)
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[ARCH-3] 도메인 격리")
    class DomainIsolationTests {

        @Test
        @DisplayName("[ARCH-3] Product 도메인은 Order 도메인에 의존할 수 없다")
        void product_domain_must_not_depend_on_order_domain() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("com.sparta.oms.product..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.sparta.oms.order..");

            rule.check(classes);
        }

        @Test
        @DisplayName("[ARCH-3] Order Service는 Product Service에 직접 의존할 수 없다 (Repository 경유 필수)")
        void order_service_must_not_depend_on_product_service() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("com.sparta.oms.order.service..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.sparta.oms.product.service..");

            rule.check(classes);
        }
    }

    // ══════════════════════════════════════════════════════════
    // ARCH-4: 명명 규칙 (Naming Conventions)
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[ARCH-4] 명명 규칙")
    class NamingConventionTests {

        @Test
        @DisplayName("[ARCH-4] Controller 클래스는 'Controller' 접미사를 가져야 한다")
        void controllers_should_be_named_ending_with_controller() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..controller..")
                    .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().haveSimpleNameEndingWith("Controller");

            rule.check(classes);
        }

        @Test
        @DisplayName("[ARCH-4] Service 클래스는 'Service' 접미사를 가져야 한다")
        void services_should_be_named_ending_with_service() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..service..")
                    .and().areAnnotatedWith("org.springframework.stereotype.Service")
                    .should().haveSimpleNameEndingWith("Service");

            rule.check(classes);
        }

        @Test
        @DisplayName("[ARCH-4] DTO 클래스는 dto 패키지에 위치해야 한다")
        void dto_classes_must_reside_in_dto_package() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Dto")
                    .should().resideInAPackage("..dto..");

            rule.check(classes);
        }

        @Test
        @DisplayName("[ARCH-4] Entity 클래스는 entity 패키지에 위치해야 한다")
        void entity_classes_must_reside_in_entity_package() {
            ArchRule rule = classes()
                    .that().areAnnotatedWith("jakarta.persistence.Entity")
                    .should().resideInAPackage("..entity..");

            rule.check(classes);
        }
    }

    // ══════════════════════════════════════════════════════════
    // ARCH-5: 애너테이션 계약 (Annotation Contracts)
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[ARCH-5] 애너테이션 계약")
    class AnnotationContractTests {

        @Test
        @DisplayName("[ARCH-5] Service 패키지 클래스는 @Service 또는 @Transactional을 가져야 한다")
        void service_classes_must_have_service_annotation() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..service..")
                    .and().areNotInterfaces()
                    .should().beAnnotatedWith("org.springframework.stereotype.Service");

            rule.check(classes);
        }

        @Test
        @DisplayName("[ARCH-5] Controller 패키지 클래스는 @RestController를 가져야 한다")
        void controller_classes_must_have_rest_controller_annotation() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..controller..")
                    .and().areNotInterfaces()
                    .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController");

            rule.check(classes);
        }
    }

    // ══════════════════════════════════════════════════════════
    // ARCH-6: 의존성 제한 (Dependency Restrictions)
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[ARCH-6] 의존성 제한")
    class DependencyRestrictionTests {

        @Test
        @DisplayName("[ARCH-6] 애플리케이션 코드는 테스트 전용 라이브러리에 의존할 수 없다")
        void application_code_must_not_depend_on_test_libraries() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("com.sparta.oms..")
                    .and().resideOutsideOfPackage("..test..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.junit..",
                            "org.mockito..",
                            "com.tngtech.archunit.."
                    );

            rule.check(classes);
        }

        @Test
        @DisplayName("[ARCH-6] sun.* 패키지는 사용할 수 없다 (JDK 내부 API 금지)")
        void application_code_must_not_use_sun_packages() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("com.sparta.oms..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("sun..");

            rule.check(classes);
        }
    }
}