---
title: "코드 스타일 규칙"
type: constraint
domain: cross-cutting
load_level: 2
verified_at: "2026-04-15"
references_code:
  - config/checkstyle/checkstyle.xml
related:
  - system.md
supersedes: null
superseded_by: null
---

# 코드 스타일 규칙 (Checkstyle)

**강제 수단:** `./gradlew checkstyleMain` — 위반 시 빌드 실패
**규칙 파일:** `config/checkstyle/checkstyle.xml`

---

## 규칙 목록

| 규칙 ID | 내용 | 위반 예시 | 수정 방법 |
|---|---|---|---|
| STYLE-1 | 와일드카드 임포트 금지 | `import java.util.*` | `import java.util.List` |
| STYLE-2 | 클래스명 PascalCase | `productService` (클래스) | `ProductService` |
| STYLE-2 | 메서드명 camelCase | `GetProduct()` | `getProduct()` |
| STYLE-2 | 상수명 UPPER_SNAKE_CASE | `maxRetry` (상수) | `MAX_RETRY` |
| STYLE-3 | 빈 catch 블록 금지 | `catch (Exception e) {}` | 최소한 로그라도 기록 |
| STYLE-4 | 문자열 == 비교 금지 | `name == "상품"` | `"상품".equals(name)` |
| STYLE-5 | 매직 넘버 금지 | `if (stock < 10)` | `private static final int MIN_STOCK = 10` |
| STYLE-6 | 미사용 임포트 금지 | 사용하지 않는 import | 삭제 |
| STYLE-7 | sun.* 임포트 금지 | `import sun.misc.Unsafe` | JDK 공개 API 사용 |

---

## 위반 탐지 및 수정

```bash
# 위반 탐지
./gradlew checkstyleMain

# 출력 예시:
# [ant:checkstyle] [ERROR] src/main/.../ProductService.java:3:
#   Using the '.*' form of import should be avoided - java.util.*.
#   [AvoidStarImport]

# 수정 후 재확인
./gradlew checkstyleMain
```

## 예외 처리

불가피하게 규칙을 위반해야 하는 경우 (예: Lombok 생성 코드):
```java
// @SuppressWarnings("checkstyle:MagicNumber")
// 단, 억제 사유를 주석으로 반드시 명시
```

억제는 예외적 상황에서만 허용한다. 모든 억제는 Review Agent 체크리스트에서 확인된다.