# ADR-0008: 모듈을 계층이 아니라 신뢰 경계(trust boundary) 기준으로 나눈다

## 상태
Accepted

## 맥락
비기능요구사항에 "등록한 HTML이 관리자 인증 정보나 관리자 API에 접근하지 못하게 해야
합니다"가 명시되어 있다. 이전 프로젝트(architecture-study)는 domain/application/infra/
presentation/common/bootstrap 6개 모듈로 계층별 분리를 했는데, 그 근거는 "클린 아키텍처
원칙"이라는 일반론이었다.

## 결정
Gradle 모듈을 신뢰 경계 기준 3개(+bootstrap)로 나눈다.
- `core`: 도메인 엔티티, JPA 리포지토리. admin/publicform 둘 다 의존.
- `admin`: 인증, 템플릿/캠페인/링크 관리, 성과 조회 API. core만 의존.
- `publicform`: 리다이렉트, 공개 폼 렌더링, 제출 API. core만 의존. **admin을 의존하지
  않는다** — 이 제약이 이 ADR의 핵심이다.
- `bootstrap`: 위 세 모듈을 조립하는 실행 진입점.

## 근거
- 이전 6모듈 구조의 계층 분리는 이번 과제의 구체적 위협과 직접 연결되지 않는다.
- publicform이 admin에 대한 Gradle 의존성 자체가 없으므로, 공개 폼 처리 코드가 관리자
  인증/세션 관련 클래스를 import하면 컴파일 에러가 난다. "격리를 지켰다"는 주장을
  코드 리뷰가 아니라 빌드 실패로 증명할 수 있다.
- 6개 모듈보다 적은 3개 모듈은 모듈별 테스트 부트스트랩 등 설정 오버헤드가 작아 3일
  스코프에 적합하다.

## 결과 및 한계
- 장점: "public 모듈이 admin 코드를 참조할 수 없다"는 것을 빌드 시점에 강제한다.
- **한계 (정직하게 남겨야 함)**: 이 경계는 컴파일 타임 강제이지 런타임 프로세스/네트워크
  격리가 아니다. admin과 publicform은 같은 Spring Boot 프로세스(bootstrap)에서 함께
  뜬다. 이번 과제에서 실제로 더 중요한 공격 경로는 "등록된 HTML 안의 악성 JS가 방문자
  브라우저에서 실행되어 관리자 API를 직접 fetch로 호출하는 것"인데, 이는 Gradle 모듈
  분리로 막을 수 없고 HTTP 레벨 방어(별도 세션 쿠키 스코프, CORS, CSP)가 필요하다.
  이 부분은 admin 인증 설계 시 별도로 다룬다.
