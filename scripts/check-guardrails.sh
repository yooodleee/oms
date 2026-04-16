#!/bin/bash
# check-guardrails.sh
# 모든 가드레일 계층을 순서대로 실행한다.
# Principle 6 (강제 제약) 구현체 — docs/guardrails/system.md 참조

set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FAIL=0

echo "============================================"
echo " OMS Guardrail Check"
echo " docs/guardrails/system.md 참조"
echo "============================================"

# ─────────────────────────────────────────────
# Layer 1: 코드 스타일 (Checkstyle)
# 규칙: docs/guardrails/code-style.md
# ─────────────────────────────────────────────
echo ""
echo "[Layer 1] Code Style (Checkstyle)"
echo "  규칙: config/checkstyle/checkstyle.xml"

if ./gradlew checkstyleMain -q 2>&1; then
    echo "  ✓ PASSED — 스타일 규칙 위반 없음"
else
    echo "  ✗ FAILED — 스타일 위반 감지"
    echo "  수정 방법: build/reports/checkstyle/main.html 참조"
    echo "  가이드: docs/guardrails/code-style.md"
    FAIL=$((FAIL + 1))
fi

# ─────────────────────────────────────────────
# Layer 2: 아키텍처 경계 + 도메인 격리 (ArchUnit)
# 규칙: docs/guardrails/architecture-boundaries.md
# ─────────────────────────────────────────────
echo ""
echo "[Layer 2] Architecture Boundaries (ArchUnit)"
echo "  규칙: ARCH-1~6"

if ./gradlew test --tests "com.sparta.oms.architecture.ArchitectureTest" -q 2>&1; then
    echo "  ✓ PASSED — 아키텍처 경계 위반 없음"
else
    echo "  ✗ FAILED — 아키텍처 경계 위반 감지"
    echo "  수정 방법: build/reports/tests/test/index.html 참조"
    echo "  가이드: docs/guardrails/architecture-boundaries.md"
    FAIL=$((FAIL + 1))
fi

# ─────────────────────────────────────────────
# Layer 3: 의존성 감사
# 규칙: docs/guardrails/dependency-policy.md
# ─────────────────────────────────────────────
echo ""
echo "[Layer 3] Dependency Audit"
echo "  승인된 의존성 목록: docs/guardrails/dependency-policy.md"

APPROVED_DEPS=(
    "spring-boot-starter-data-jpa"
    "spring-boot-starter-validation"
    "spring-boot-starter-web"
    "lombok"
    "mysql-connector-j"
    "archunit"
    "junit-platform-launcher"
    "spring-boot-starter-data-jpa-test"
    "spring-boot-starter-validation-test"
    "checkstyle"
)

RUNTIME_DEPS=$(./gradlew dependencies --configuration runtimeClasspath -q 2>/dev/null \
    | grep -oP '(?<=--- )[\w\.\-]+:[\w\.\-]+' \
    | sort -u 2>/dev/null || echo "")

UNAPPROVED=0
while IFS= read -r dep; do
    FOUND=false
    for approved in "${APPROVED_DEPS[@]}"; do
        if [[ "$dep" == *"$approved"* ]]; then
            FOUND=true
            break
        fi
    done
    if [ "$FOUND" = "false" ] && [ -n "$dep" ]; then
        echo "  ⚠ 미승인 의존성 감지: $dep"
        echo "    → docs/guardrails/dependency-policy.md 에서 승인 절차 확인"
        UNAPPROVED=$((UNAPPROVED + 1))
    fi
done <<< "$RUNTIME_DEPS"

if [ "$UNAPPROVED" -eq 0 ]; then
    echo "  ✓ PASSED — 모든 의존성이 승인 목록에 있음"
fi

# ─────────────────────────────────────────────
# Layer 4: 인터페이스 계약 검증 (도메인 규칙 테스트)
# 규칙: docs/guardrails/interface-contracts.md
# ─────────────────────────────────────────────
echo ""
echo "[Layer 4] Interface Contracts"
echo "  규칙: docs/guardrails/interface-contracts.md"

if ./gradlew test --tests "com.sparta.oms.product.service.*" \
                  --tests "com.sparta.oms.order.service.*" -q 2>&1; then
    echo "  ✓ PASSED — 인터페이스 계약 위반 없음"
else
    echo "  ✗ FAILED — 인터페이스 계약 위반 감지"
    FAIL=$((FAIL + 1))
fi

# ─────────────────────────────────────────────
# 결과 요약
# ─────────────────────────────────────────────
echo ""
echo "============================================"
echo " 가드레일 검사 결과"
echo "============================================"

if [ "$FAIL" -eq 0 ]; then
    echo "  ✓ 모든 가드레일 통과 — PR 생성 가능"
    exit 0
else
    echo "  ✗ ${FAIL}개 가드레일 실패 — 수정 후 재실행 필요"
    echo ""
    echo "  수정 가이드: docs/guardrails/system.md"
    exit 1
fi
