#!/bin/bash
# validate-docs.sh
# 문서 최신성·링크 유효성을 검증한다.
# Principle 3 (In-Repository Knowledge) + Required Output 3 (Knowledge Management) 구현체

set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PASS=0
FAIL=0
WARN=0

echo "========================================"
echo " OMS Knowledge System Validation"
echo "========================================"

# ─────────────────────────────────────────
# 1. 코드 참조 유효성 검사 (references_code)
#    docs의 frontmatter에 선언된 코드 경로가 실제로 존재하는지 확인
# ─────────────────────────────────────────
echo ""
echo "[1] Code Reference Validation (references_code)"

check_reference() {
  local doc="$1"
  local ref="$2"
  if [ -e "$ROOT/$ref" ]; then
    echo "  ✓ $ref"
    PASS=$((PASS + 1))
  else
    echo "  ✗ STALE: $ref (참조 코드 없음, 선언 문서: $doc)"
    FAIL=$((FAIL + 1))
  fi
}

# ADR-0001: 소프트 삭제
check_reference "docs/adr/0001-soft-delete-for-products.md" \
  "src/main/java/com/sparta/oms/product/entity/Product.java"
check_reference "docs/adr/0001-soft-delete-for-products.md" \
  "src/main/java/com/sparta/oms/product/repository/ProductRepository.java"

# ADR-0002: 재고 원자성
check_reference "docs/adr/0002-atomic-stock-decrease.md" \
  "src/main/java/com/sparta/oms/product/repository/ProductRepository.java"

# ADR-0003: JOIN FETCH
check_reference "docs/adr/0003-join-fetch-for-orders.md" \
  "src/main/java/com/sparta/oms/order/repository/OrderRepository.java"

# ArchUnit 테스트
check_reference "docs/constraints/enforcement-map.md" \
  "src/test/java/com/sparta/oms/architecture/ArchitectureTest.java"

# CONTEXT.md 파일
check_reference "docs/architecture/overview.md" \
  "src/main/java/com/sparta/oms/product/CONTEXT.md"
check_reference "docs/architecture/overview.md" \
  "src/main/java/com/sparta/oms/order/CONTEXT.md"

# ─────────────────────────────────────────
# 2. 내부 링크 유효성 검사
#    문서 내 Markdown 링크 대상 파일이 존재하는지 확인
# ─────────────────────────────────────────
echo ""
echo "[2] Internal Link Validation"

find "$ROOT/docs" "$ROOT/agents" "$ROOT/tests" "$ROOT/plans" \
  -name "*.md" 2>/dev/null | while read -r doc; do

  # [text](path) 또는 [text](path#anchor) 패턴 추출
  grep -oE '\]\([^)]+\)' "$doc" 2>/dev/null \
    | sed 's/^](//' | sed 's/)$//' | sed 's/#.*//' \
    | grep -v '^http' \
    | grep -v '^$' \
    | while read -r link; do

      # 상대 경로를 절대 경로로 변환
      dir="$(dirname "$doc")"
      target="$(cd "$dir" 2>/dev/null && realpath -m "$link" 2>/dev/null || echo "")"

      if [ -n "$target" ] && [ ! -e "$target" ]; then
        echo "  ✗ BROKEN LINK: $link"
        echo "    in: ${doc#$ROOT/}"
        FAIL=$((FAIL + 1))
      fi
  done
done

echo "  (링크 검사 완료)"

# ─────────────────────────────────────────
# 3. 필수 frontmatter 존재 여부
#    _template.md를 제외한 모든 docs/*.md에 verified_at 필드 확인
# ─────────────────────────────────────────
echo ""
echo "[3] Frontmatter Completeness (verified_at)"

find "$ROOT/docs" -name "*.md" ! -name "_template.md" | while read -r doc; do
  if ! grep -q "^verified_at:" "$doc" 2>/dev/null; then
    echo "  ⚠ MISSING frontmatter: ${doc#$ROOT/}"
    WARN=$((WARN + 1))
  fi
done

echo "  (frontmatter 검사 완료)"

# ─────────────────────────────────────────
# 4. enforcement-map MISSING 항목 경고
# ─────────────────────────────────────────
echo ""
echo "[4] Enforcement Map — MISSING Items"

MISSING_COUNT=$(grep -c "❌ MISSING" "$ROOT/docs/constraints/enforcement-map.md" 2>/dev/null || echo "0")
if [ "$MISSING_COUNT" -gt "0" ]; then
  echo "  ⚠ enforcement-map에 ❌ MISSING 항목 ${MISSING_COUNT}건 존재"
  echo "    → 새 기능 구현 시 해당 항목을 ✅ ENFORCED로 전환해야 함"
  WARN=$((WARN + MISSING_COUNT))
else
  echo "  ✓ 모든 규칙이 ENFORCED 상태"
  PASS=$((PASS + 1))
fi

# ─────────────────────────────────────────
# 5. docs/index.md 문서 목록과 실제 파일 동기화 확인
# ─────────────────────────────────────────
echo ""
echo "[5] Index Sync Check"

INDEX_COUNT=$(grep -c "^| \`" "$ROOT/docs/index.md" 2>/dev/null || echo "0")
ACTUAL_COUNT=$(find "$ROOT/docs" -name "*.md" ! -name "_template.md" ! -name "index.md" ! -name "knowledge-graph.md" | wc -l | tr -d ' ')

echo "  index.md 등록 문서: ${INDEX_COUNT}건"
echo "  실제 docs/ 파일: ${ACTUAL_COUNT}건"

if [ "$INDEX_COUNT" -lt "$ACTUAL_COUNT" ]; then
  echo "  ⚠ index.md에 등록되지 않은 문서가 있을 수 있음 — docs/index.md 갱신 필요"
  WARN=$((WARN + 1))
fi

# ─────────────────────────────────────────
# 결과 요약
# ─────────────────────────────────────────
echo ""
echo "========================================"
echo " 결과 요약"
echo "========================================"
echo "  PASS: $PASS"
echo "  WARN: $WARN  (작업 진행 가능, 갱신 권장)"
echo "  FAIL: $FAIL  (즉시 수정 필요)"
echo "========================================"

if [ "$FAIL" -gt "0" ]; then
  echo ""
  echo "  ✗ FAIL 항목이 존재합니다. 문서를 수정하거나 코드 참조를 갱신하세요."
  exit 1
else
  echo ""
  echo "  ✓ 치명적 오류 없음. WARN 항목은 선택적으로 갱신하세요."
  exit 0
fi