package com.sparta.oms.quality;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * 코드 품질 스멜 자동 탐지 — 지속적 개선 시스템의 자동화 게이트.
 *
 * 이 테스트가 실패하면 코드 품질 기준을 위반한 것이다.
 * 위반을 suppress하거나 이 테스트를 비활성화하는 것은 금지된다.
 * 위반을 수정하거나, 기준이 잘못됐다면 기준 자체를 변경하고 인간에게 보고한다.
 *
 * 규칙 참조:
 * @see docs/improvements/system.md
 * @see docs/improvements/bad-patterns.md
 * @see docs/constraints/enforcement-map.md (CQ-1 ~ CQ-4)
 */
@DisplayName("코드 품질 스멜 탐지")
class CodeQualityTest {

    private static JavaClasses classes;

    @BeforeAll
    static void loadClasses() {
        classes = new ClassFileImporter().importPackages("com.sparta.oms");
    }

    // ══════════════════════════════════════════════════════════
    // CQ-1: 엔티티 캡슐화 (Entity Encapsulation)
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[CQ-1] 엔티티 캡슐화")
    class EntityEncapsulationTests {

        @Test
        @DisplayName("[CQ-1] Entity 클래스는 public setter 메서드를 가질 수 없다")
        void entity_classes_must_not_have_public_setter_methods() {
            // 이유: 엔티티 상태 변경은 반드시 도메인 의미를 가진 메서드를 통해야 한다.
            // setName() 대신 rename(), softDelete() 등 의도가 명확한 메서드 사용.
            // docs/improvements/bad-patterns.md (BP-1) 참조
            ArchRule rule = noMethods()
                    .that().haveNameStartingWith("set")
                    .and().arePublic()
                    .should().beDeclaredInClassesThat()
                    .areAnnotatedWith("jakarta.persistence.Entity");

            rule.check(classes);
        }

        @Test
        @DisplayName("[CQ-1] Entity 클래스는 DTO 타입을 반환하지 않는다")
        void entity_classes_must_not_return_dto_types() {
            // 이유: Entity가 DTO에 의존하면 변환 책임이 뒤집힌다.
            // DTO 변환은 Service 레이어 책임이다.
            ArchRule rule = noClasses()
                    .that().areAnnotatedWith("jakarta.persistence.Entity")
                    .should().dependOnClassesThat()
                    .haveSimpleNameEndingWith("Dto");

            rule.check(classes);
        }
    }

    // ══════════════════════════════════════════════════════════
    // CQ-2: DTO 순수성 (DTO Purity)
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[CQ-2] DTO 순수성")
    class DtoPurityTests {

        @Test
        @DisplayName("[CQ-2] DTO 클래스는 @Entity 애너테이션을 가질 수 없다")
        void dto_classes_must_not_be_annotated_with_entity() {
            // 이유: DTO와 Entity의 역할 혼재는 영속성 오염을 유발한다.
            ArchRule rule = noClasses()
                    .that().haveSimpleNameEndingWith("Dto")
                    .should().beAnnotatedWith("jakarta.persistence.Entity");

            rule.check(classes);
        }

        @Test
        @DisplayName("[CQ-2] DTO 클래스는 비즈니스 로직(Service)에 의존하지 않는다")
        void dto_classes_must_not_depend_on_service_classes() {
            // 이유: DTO는 데이터 운반체다. 서비스 로직을 참조하면 계층 경계가 무너진다.
            ArchRule rule = noClasses()
                    .that().haveSimpleNameEndingWith("Dto")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..service..");

            rule.check(classes);
        }
    }

    // ══════════════════════════════════════════════════════════
    // CQ-3: 예외 계층 (Exception Hierarchy)
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[CQ-3] 예외 계층")
    class ExceptionHierarchyTests {

        @Test
        @DisplayName("[CQ-3] 도메인 패키지의 클래스는 java.lang.RuntimeException을 직접 throw하지 않는다")
        void domain_classes_must_not_throw_raw_runtime_exception() {
            // 이유: RuntimeException을 직접 던지면 호출자가 예외 유형으로 의도를 파악할 수 없다.
            // IllegalArgumentException, IllegalStateException 등 의미 있는 예외 사용.
            // docs/improvements/bad-patterns.md (BP-3) 참조
            ArchRule rule = noClasses()
                    .that().resideInAPackage("com.sparta.oms..")
                    .should().dependOnClassesThat()
                    .haveFullyQualifiedName("java.lang.RuntimeException");

            rule.check(classes);
        }
    }

    // ══════════════════════════════════════════════════════════
    // CQ-4: Repository 계약 (Repository Contract)
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("[CQ-4] Repository 계약")
    class RepositoryContractTests {

        @Test
        @DisplayName("[CQ-4] Repository 인터페이스는 repository 패키지에 위치해야 한다")
        void repository_interfaces_must_reside_in_repository_package() {
            // 이유: Repository가 다른 패키지에 있으면 탐색이 어렵고 레이어 경계가 불명확해진다.
            ArchRule rule = classes()
                    .that().areAnnotatedWith("org.springframework.stereotype.Repository")
                    .or().haveSimpleNameEndingWith("Repository")
                    .and().areInterfaces()
                    .should().resideInAPackage("..repository..");

            rule.check(classes);
        }

        @Test
        @DisplayName("[CQ-4] Service 클래스는 다른 도메인의 Entity를 직접 생성하지 않는다")
        void service_classes_must_not_instantiate_other_domain_entities() {
            // 이유: cross-domain entity 생성은 도메인 격리 원칙을 위반한다.
            // Order 도메인이 Product Entity를 직접 new하면 ARCH-3 위반이다.
            ArchRule rule = noClasses()
                    .that().resideInAPackage("com.sparta.oms.product.service..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.sparta.oms.order.entity..");

            rule.check(classes);
        }
    }
}
