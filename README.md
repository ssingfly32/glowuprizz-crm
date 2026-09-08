# glowuprizz-crm

리드마그넷 CRM 운영 시스템

## 실행 방법

1. `.env.example`을 복사해 `.env`를 만들고 값을 채운다.
   ```
   cp .env.example .env
   ```
2. PostgreSQL을 띄운다.
   ```
   docker compose --env-file .env up -d
   ```
3. 애플리케이션을 실행한다 (Flyway 마이그레이션이 기동 시 자동 적용된다).
   ```
   ./gradlew :bootstrap:bootRun
   ```
4. 기본 포트는 `8080`이다.

## 테스트 방법

```
./gradlew test
```

- `core` 모듈의 JPA 리포지토리 테스트는 Testcontainers로 실제 PostgreSQL 컨테이너를
  띄워 검증한다 (별도 DB 실행 불필요, Docker만 있으면 됨).
- 전체 모듈 테스트 리포트: `./gradlew test jacocoTestReport` 실행 후
  `*/build/customJacocoReportDir/jacocoHtml/index.html` 확인.
