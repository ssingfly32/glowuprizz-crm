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
3. 관리자 서버를 실행한다 (Flyway 마이그레이션이 기동 시 자동 적용된다. 데모용 운영자
   계정도 이때 함께 시드된다).
   ```
   ./gradlew :admin-bootstrap:bootRun
   ```
   포트 `8080`. 로그인: `POST /admin/auth/login`
   ```json
   { "email": "operator@glowuprizz.com", "password": "glowup1234!" }
   ```
   응답의 `accessToken`을 이후 요청에 `Authorization: Bearer <token>` 헤더로 사용한다.

   매 요청을 직접 만들기 번거로우면 `docs/glowuprizz-crm.postman_collection.json`을
   Postman에 import한다. 로그인 요청을 실행하면 `accessToken`이 컬렉션 변수에 자동
   저장되어 이후 admin 요청에 자동 적용된다.
4. 공개 폼 서버를 별도 프로세스로 실행한다 (관리자 서버와 다른 포트 = 다른 origin으로
   띄우는 것이 의도적인 설계다. `docs/adr/0011` 참고).
   ```
   ./gradlew :public-bootstrap:bootRun
   ```
   포트 `8081`.

## 테스트 방법

```
./gradlew test
```

- `core` 모듈은 Spring/DB 없이 도메인 엔티티의 생성자 검증 로직만 검증하는 순수
  단위 테스트다 (밀리초 단위로 빠르게 끝남).
- `admin`/`publicform` 모듈에도 Spring 컨텍스트 없이 순수 로직/Mockito만으로 검증하는
  단위 테스트가 있다 (전환율 계산, 제출 스크립트 삽입, 리포지토리를 목으로 만든 동시성
  예외 변환 로직 등).
- `admin-bootstrap`/`public-bootstrap` 모듈은 Testcontainers로 실제 PostgreSQL
  컨테이너를 띄워, Security 필터 체인/Flyway 마이그레이션/JPA까지 전부 실제로
  동작하는 상태에서 검증하는 통합 테스트다 (별도 DB 실행 불필요, Docker만 있으면 됨).
- 전체 모듈 테스트 리포트: `./gradlew test jacocoTestReport` 실행 후
  `*/build/customJacocoReportDir/jacocoHtml/index.html` 확인.
