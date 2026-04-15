#!/bin/bash
# verify-gates.sh
# L1~L3 게이트를 순서대로 실행한다. 하나라도 실패하면 즉시 중단.
# Principle 4 (피드백 루프) + Principle 6 (강제 제약) 구현체

set -e  # 에러 발생 시 즉시 종료

echo "=============================="
echo " OMS Gate Verification"
echo "=============================="

echo ""
echo "[L1] Compile..."
./gradlew compileJava -q
echo "  ✓ L1 PASSED"

echo ""
echo "[L2] Test (Unit + Architecture)..."
./gradlew test -q
echo "  ✓ L2 PASSED"

echo ""
echo "[L3] Build..."
./gradlew build -q
echo "  ✓ L3 PASSED"

echo ""
echo "=============================="
echo " All gates PASSED"
echo "=============================="