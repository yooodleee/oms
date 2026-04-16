#!/bin/bash
# assess-quality.sh
# 코드 품질 자동 평가 스크립트 — 지속적 개선 시스템의 탐지 레이어
# 참조: docs/improvements/system.md, docs/improvements/quality-metrics.md
#
# 사용법:
#   scripts/assess-quality.sh            — 전체 평가 (권장)
#   scripts/assess-quality.sh --quick    — 빠른 평가 (테스트 스킵)
#   scripts/assess-quality.sh --fix      — 탐지 후 tech-debt-registry.md 자동 갱신

set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
QUICK=false
FIX=false
SCORE=100
ISSUES=()

for arg in "$@"; do
    case $arg in
        --quick) QUICK=true ;;
        --fix)   FIX=true ;;
    esac
done

echo "============================================================"
echo " OMS 코드 품질 자동 평가"
echo " docs/improvements/quality-metrics.md 기준 적용"
echo "============================================================"

# ─────────────────────────────────────────────────────────────────
# 1. 기술 부채 마커 탐지
# 탐지 대상: TODO, FIXME, HACK, XXX
# ─────────────────────────────────────────────────────────────────
echo ""
echo "[1] 기술 부채 마커 탐지"

DEBT_MARKERS=$(grep -rn "TODO\|FIXME\|HACK\|XXX" \
    "$ROOT/src/main/java/" \
    "$ROOT/src/test/java/" 2>/dev/null | \
    grep -v "Binary file" || true)

DEBT_COUNT=$(echo "$DEBT_MARKERS" | grep -c "[A-Z]" 2>/dev/null || echo 0)

if [ "$DEBT_COUNT" -eq 0 ]; then
    echo "  ✓ 부채 마커 없음"
else
    echo "  ✗ 부채 마커 ${DEBT_COUNT}개 발견:"
    echo "$DEBT_MARKERS" | sed 's/^/    /'
    SCORE=$((SCORE - DEBT_COUNT * 5))
    ISSUES+=("기술 부채 마커 ${DEBT_COUNT}개 (tech-debt-registry.md 갱신 필요)")
fi

# ─────────────────────────────────────────────────────────────────
# 2. enforcement-map.md MISSING 항목 탐지
# enforcement-map에 MISSING이 있으면 merge 불가
# ─────────────────────────────────────────────────────────────────
echo ""
echo "[2] enforcement-map.md MISSING 항목 탐지"

MISSING_COUNT=$(grep -c "❌ MISSING" "$ROOT/docs/constraints/enforcement-map.md" 2>/dev/null || echo 0)

if [ "$MISSING_COUNT" -eq 0 ]; then
    echo "  ✓ MISSING 항목 없음"
else
    echo "  ✗ MISSING 항목 ${MISSING_COUNT}개 발견 — merge 금지"
    grep -n "❌ MISSING" "$ROOT/docs/constraints/enforcement-map.md" | sed 's/^/    /'
    SCORE=$((SCORE - MISSING_COUNT * 25))
    ISSUES+=("enforcement-map MISSING ${MISSING_COUNT}개 (merge 차단)")
fi

# ─────────────────────────────────────────────────────────────────
# 3. 하드코딩 에러 메시지 탐지 (BP-5)
# 동일한 에러 메시지 문자열이 여러 곳에 중복되는지 확인
# ─────────────────────────────────────────────────────────────────
echo ""
echo "[3] 하드코딩 에러 메시지 탐지 (BP-5)"

HARDCODED_MSG=$(grep -rn '".*not found\|".*부족\|".*failed' \
    "$ROOT/src/main/java/" 2>/dev/null | \
    grep "throw new" || true)

HARDCODED_COUNT=$(echo "$HARDCODED_MSG" | grep -c "throw" 2>/dev/null || echo 0)

if [ "$HARDCODED_COUNT" -eq 0 ]; then
    echo "  ✓ 하드코딩 에러 메시지 없음"
else
    echo "  ⚠ 하드코딩 에러 메시지 ${HARDCODED_COUNT}개 (상수화 권고)"
    echo "$HARDCODED_MSG" | sed 's/^/    /'
    SCORE=$((SCORE - 3))
    ISSUES+=("하드코딩 에러 메시지 ${HARDCODED_COUNT}개 — TD-3 참조 (BP-5)")
fi

# ─────────────────────────────────────────────────────────────────
# 4. 와일드카드 import 탐지 (STYLE-1) — Checkstyle 전에 빠른 확인
# ─────────────────────────────────────────────────────────────────
echo ""
echo "[4] 와일드카드 import 탐지 (STYLE-1)"

WILDCARD=$(grep -rn "import .*\*;" "$ROOT/src/main/java/" 2>/dev/null || true)
WILDCARD_COUNT=$(echo "$WILDCARD" | grep -c "import" 2>/dev/null || echo 0)

if [ "$WILDCARD_COUNT" -eq 0 ]; then
    echo "  ✓ 와일드카드 import 없음"
else
    echo "  ✗ 와일드카드 import ${WILDCARD_COUNT}개 발견:"
    echo "$WILDCARD" | sed 's/^/    /'
    SCORE=$((SCORE - WILDCARD_COUNT * 5))
    ISSUES+=("와일드카드 import ${WILDCARD_COUNT}개 — Checkstyle STYLE-1 위반")
fi

# ─────────────────────────────────────────────────────────────────
# 5. 긴 메서드 탐지 (30줄 초과)
# 단순 휴리스틱: 메서드 내에서 30줄 이상 연속으로 코드가 있는 경우
# ─────────────────────────────────────────────────────────────────
echo ""
echo "[5] 긴 메서드 탐지 (30줄 초과)"

LONG_METHOD_FILES=()
for file in $(find "$ROOT/src/main/java" -name "*.java" 2>/dev/null); do
    # 각 파일의 최대 메서드 길이를 AWK로 추정
    MAX_LEN=$(awk '
        /\{/ { depth++ }
        /\}/ { depth--; if(depth==1) { if(len>max) max=len; len=0 } }
        depth>1 { len++ }
        END { print max+0 }
    ' "$file" 2>/dev/null || echo 0)

    if [ "$MAX_LEN" -gt 30 ] 2>/dev/null; then
        LONG_METHOD_FILES+=("$(basename "$file") (최대 약 ${MAX_LEN}줄)")
    fi
done

if [ ${#LONG_METHOD_FILES[@]} -eq 0 ]; then
    echo "  ✓ 30줄 초과 메서드 없음"
else
    echo "  ⚠ 긴 메서드 감지된 파일:"
    for f in "${LONG_METHOD_FILES[@]}"; do
        echo "    - $f"
    done
    SCORE=$((SCORE - ${#LONG_METHOD_FILES[@]} * 5))
    ISSUES+=("긴 메서드 감지 ${#LONG_METHOD_FILES[@]}개 파일")
fi

# ─────────────────────────────────────────────────────────────────
# 6. Checkstyle + ArchUnit + 커버리지 실행 (--quick 아닐 때)
# ─────────────────────────────────────────────────────────────────
if [ "$QUICK" = "false" ]; then
    echo ""
    echo "[6] Checkstyle 실행"
    cd "$ROOT"

    if ./gradlew checkstyleMain -q 2>&1; then
        echo "  ✓ Checkstyle 통과"
    else
        echo "  ✗ Checkstyle 실패 — build/reports/checkstyle/main.html 참조"
        SCORE=$((SCORE - 20))
        ISSUES+=("Checkstyle 실패")
    fi

    echo ""
    echo "[7] 품질 스멜 탐지 (CodeQualityTest)"

    if ./gradlew test --tests "com.sparta.oms.quality.CodeQualityTest" -q 2>&1; then
        echo "  ✓ 품질 스멜 없음"
    else
        echo "  ✗ 품질 스멜 탐지 — build/reports/tests/test/index.html 참조"
        SCORE=$((SCORE - 15))
        ISSUES+=("CodeQualityTest 실패 — bad-patterns.md 참조")
    fi

    echo ""
    echo "[8] 커버리지 검증 (JaCoCo)"

    if ./gradlew jacocoTestCoverageVerification -q 2>&1; then
        echo "  ✓ 커버리지 기준 충족"
    else
        echo "  ✗ 커버리지 미달 — build/reports/jacoco/test/html/index.html 참조"
        SCORE=$((SCORE - 20))
        ISSUES+=("JaCoCo 커버리지 미달 — 누락 테스트 추가 필요")
    fi
else
    echo ""
    echo "[6~8] --quick 모드: Gradle 실행 생략"
fi

# ─────────────────────────────────────────────────────────────────
# 결과 요약
# ─────────────────────────────────────────────────────────────────
echo ""
echo "============================================================"
echo " 품질 평가 결과"
echo "============================================================"
echo ""
echo "  품질 점수: ${SCORE}/100"
echo ""

if [ "$SCORE" -ge 90 ]; then
    GRADE="GOOD   ✓ PR 즉시 가능"
elif [ "$SCORE" -ge 70 ]; then
    GRADE="FAIR   ⚠ 기록 후 PR 가능, 다음 스프린트 개선"
elif [ "$SCORE" -ge 50 ]; then
    GRADE="POOR   ✗ 개선 계획 수립 후 PR"
else
    GRADE="BLOCK  ✗ 개선 후 재평가 필수"
fi

echo "  등급: $GRADE"
echo ""

if [ ${#ISSUES[@]} -gt 0 ]; then
    echo "  발견된 문제:"
    for issue in "${ISSUES[@]}"; do
        echo "    - $issue"
    done
    echo ""
    echo "  참조: docs/improvements/tech-debt-registry.md"
    echo "       docs/improvements/bad-patterns.md"
    echo "       agents/roles/refactor-agent.md"
fi

echo ""
echo "  리포트: build/reports/jacoco/test/html/index.html"
echo "          build/reports/tests/test/index.html"
echo "          build/reports/checkstyle/main.html"
echo "============================================================"

# 점수 50 미만이면 실패 코드 반환
if [ "$SCORE" -lt 50 ]; then
    exit 1
fi
