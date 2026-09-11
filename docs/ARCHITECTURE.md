# 아키텍처 개요

5개 Gradle 모듈의 의존 방향과, 이 프로젝트의 핵심 설계 포인트인 "운영자(admin)와
방문자(publicform)는 서로를 전혀 모른다"는 신뢰 경계를 함께 표시했다. 근거는
`docs/adr/0008-module-structure-trust-boundary.md`,
`docs/adr/0011-jwt-auth-with-process-isolation.md` 참고.

```mermaid
graph TD
    subgraph ABS["admin-bootstrap (8080, 실행 가능)"]
        ABApp["AdminBootstrapApplication"]
    end

    subgraph PBS["public-bootstrap (8081, 실행 가능)"]
        PBApp["PublicBootstrapApplication"]
    end

    subgraph AD["admin"]
        AAuth["auth<br/>(로그인, JWT 발급/검증, SecurityConfig)"]
        ACore["template / campaign / link / stats / submission"]
    end

    subgraph PF["publicform"]
        PForm["form / redirect / submission"]
        PVisit["visit / visitor<br/>(익명 쿠키 토큰)"]
    end

    subgraph CO["core"]
        CEntity["Operator / HtmlTemplate / Campaign<br/>DistributionLink / Visit / Submission"]
        CMig["Flyway 마이그레이션<br/>(V1__init, V2__seed_operator)"]
    end

    ABApp --> AD
    ABApp --> CO
    PBApp --> PF
    PBApp --> CO
    AD --> CO
    PF --> CO

    style ABS fill:#0d172a,color:#ffffff,stroke:#0d172a
    style PBS fill:#0d172a,color:#ffffff,stroke:#0d172a
    style AD fill:#eef2ff,stroke:#6366f1
    style PF fill:#e8f9f0,stroke:#22c55e
    style CO fill:#fff7e6,stroke:#f59e0b
```

## 읽는 법

- **실선 화살표**: `build.gradle`의 `implementation project(':X')`로 명시된, 컴파일 타임에
  강제되는 모듈 의존. `core`는 모든 모듈이 의존할 수 있는 공유 도메인(shared kernel)이다.
- **`admin`과 `publicform` 사이엔 화살표가 없다** — 이게 이 프로젝트에서 가장 중요한 경계다.
  어느 쪽 코드에서도 반대쪽 클래스를 import할 방법이 없어, "등록된 HTML이 관리자 인증정보나
  관리자 API에 접근하지 못하게 해야 한다"는 비기능 요구사항을 빌드 단계에서부터 물리적으로
  강제한다.
- `admin-bootstrap`(8080)과 `public-bootstrap`(8081)은 같은 저장소의 서로 다른 실행
  가능 산출물이다. 실제 운영에서도 별도 프로세스/포트로 뜨며, 방문자의 브라우저는 8081하고만
  통신하므로 admin의 JWT를 획득할 방법이 없다.

## 요청 흐름

```mermaid
sequenceDiagram
    actor Operator as 운영자
    actor Visitor as 방문자
    participant Admin as admin-bootstrap :8080
    participant Public as public-bootstrap :8081
    participant DB as PostgreSQL

    Operator->>Admin: POST /admin/auth/login
    Admin-->>Operator: JWT
    Operator->>Admin: POST /admin/html-templates
    Operator->>Admin: POST /admin/campaigns (+publish)
    Operator->>Admin: POST /admin/campaigns/{id}/links (채널별)
    Admin->>DB: 템플릿/캠페인/링크 저장

    Visitor->>Public: GET /r/{linkToken}
    Public->>DB: Visit 기록 (channel 포함)
    Public-->>Visitor: 302 -> /f/{slug}?link=...
    Visitor->>Public: GET /f/{slug}
    Public-->>Visitor: 등록된 HTML (+CSP 헤더)
    Visitor->>Public: POST /f/{slug}/submissions
    Public->>DB: Submission 저장

    Operator->>Admin: GET /admin/campaigns/{id}/stats
    Admin->>DB: 방문/방문자/신청 집계 (캠페인 스코프)
    Admin-->>Operator: 캠페인 전체 + 캠페인 하위 채널별 성과

    Operator->>Admin: GET /admin/channels/stats
    Admin->>DB: 방문/방문자/신청 집계 (전체 캠페인, 채널 기준)
    Admin-->>Operator: 전역 채널별 성과

    Operator->>Admin: GET /admin/campaigns/{id}/submissions
    Admin->>DB: 신청 원본 데이터 조회
    Admin-->>Operator: 신청자 명단(CRM)
```

세부 API 스펙은 `docs/api.md`, 각 설계 결정의 근거는 `docs/adr/`를 참고한다.
