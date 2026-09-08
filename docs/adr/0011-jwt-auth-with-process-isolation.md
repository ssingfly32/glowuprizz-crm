# ADR-0011: 관리자 인증은 JWT + admin/public 프로세스(포트) 분리로 구현한다

## 상태
Accepted

## 맥락
비기능요구사항("등록한 HTML이 관리자 인증 정보나 관리자 API에 접근하지 못하게 해야 합니다")의
현실적인 공격 경로는 "publicform 코드가 admin 코드를 import하는 것"(ADR-0008이 이미 막음)이
아니라, **운영자가 자신이 등록한 HTML을 미리보기 하다가 그 안의 악성/미검증 JS가 운영자의
관리자 인증 정보에 편승해 관리자 API를 호출하는 것**이다.

이 위협을 분석하면서 다음을 확인했다:
- admin과 publicform이 같은 호스트:포트(같은 origin)에서 뜨면, 쿠키의 Domain/SameSite
  속성도, localStorage의 origin 격리도, CORS도 전부 무력화된다 (애초에 "같은 origin에서
  같은 origin으로의 요청"이라 브라우저가 구분할 방법이 없음).
- origin이 실제로 분리돼야(다른 호스트 또는 다른 포트) 위 방어 수단들이 의미를 가진다.
  localhost에서는 포트만 달라도 서로 다른 origin으로 취급된다.

## 결정
1. `admin-bootstrap`(8080 포트, admin+core만 의존)과 `public-bootstrap`(8081 포트,
   publicform+core만 의존)을 별도 실행 앱으로 분리한다. 기존 단일 `bootstrap` 모듈은
   제거한다.
2. 관리자 인증은 세션 쿠키가 아니라 **JWT**로 구현한다 (`Authorization: Bearer` 헤더).
   무상태(stateless)이므로 CSRF 보호가 필요 없고, 토큰은 admin origin(8080)에서만
   발급·보관된다.

## 근거
- 프로세스/포트 분리는 ADR-0008이 "한계"로 남겨뒀던 부분(컴파일 타임 격리일 뿐 런타임엔
  같은 프로세스)을 실제로 해소한다. 8081(public)에서 실행되는 어떤 JS도 8080(admin) origin의
  토큰 저장소(메모리/localStorage)에 접근할 방법이 원천적으로 없다.
- JWT는 REST API로 문서화하기 명확하고(Authorization 헤더 스킴), 서버 세션 저장소가
  필요 없어 배포가 단순하다.
- 두 프로세스 모두 같은 PostgreSQL을 바라보므로 데이터는 공유되지만, 마이그레이션은
  `admin-bootstrap`만 실행한다(동시 실행 경합 방지, `public-bootstrap`은
  `spring.flyway.enabled=false`).

## 결과 및 한계
- 장점: 컴파일 타임(모듈 의존성) + 런타임(별도 프로세스/origin) 이중 격리가 실제로 성립한다.
- 단점: 로컬 개발 시 두 프로세스를 각각 띄워야 한다 (README에 안내). 배포 시에도 두 개의
  배포 단위가 생긴다.
- 여전히 완전한 것은 아니다: 만약 운영자가 관리자 페이지와 공개 폼 미리보기를 **같은
  브라우저 탭 세션에서 습관적으로 뒤섞어 쓰다가** 다른 방식(예: 브라우저 확장, 클립보드
  공유)으로 토큰이 유출되는 시나리오까지 막지는 못한다. 이런 잔여 위험은 CSP(`connect-src`
  제한)로 한 겹 더 방어할 수 있으며, 공개 폼 API 구현 시 추가한다.
