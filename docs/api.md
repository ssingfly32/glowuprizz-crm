# API 문서

리드마그넷 CRM 운영 시스템의 API 문서다. 두 개의 독립 프로세스로 구성되어 있다
(`docs/adr/0011-jwt-auth-with-process-isolation.md` 참고).

| 서버 | 기본 URL | 대상 |
|---|---|---|
| admin-bootstrap | `http://localhost:8080` | 인증된 운영자 전용 (인증/템플릿/캠페인/배포 링크/성과 조회) |
| public-bootstrap | `http://localhost:8081` | 익명 방문자 전용 (배포 링크 리다이렉트/공개 폼/신청 제출) |

실행 방법은 저장소 루트 `README.md`를 참고한다.

## 공통 사항

### 인증
`admin-bootstrap`의 로그인 API를 제외한 모든 `/admin/**` 엔드포인트는 JWT Bearer 토큰이
필요하다.

```
Authorization: Bearer <accessToken>
```

토큰이 없거나 유효하지 않으면 `401 Unauthorized`와 함께 다음 형식으로 응답한다.

```json
{ "code": "AUTH_REQUIRED", "message": "인증이 필요합니다." }
```

### 에러 응답 형식
모든 에러는 아래 형식으로 응답한다 (`ErrorResponse`).

```json
{ "code": "CAMPAIGN_NOT_FOUND", "message": "캠페인을 찾을 수 없습니다." }
```

요청 바디 검증 실패(`admin` 서버)는 `code: "VALIDATION_FAILED"`, `400 Bad Request`로 응답한다.

주요 에러 코드:

| code | HTTP status | 의미 |
|---|---|---|
| `AUTH_REQUIRED` | 401 | 인증 토큰 없음/무효 |
| `AUTH_INVALID_CREDENTIALS` | 401 | 로그인 이메일/비밀번호 불일치 |
| `VALIDATION_FAILED` | 400 | 요청 바디 검증 실패 |
| `TEMPLATE_NOT_FOUND` | 404 | HTML 템플릿 없음 |
| `CAMPAIGN_NOT_FOUND` | 404 | 캠페인 없음 |
| `CAMPAIGN_NOT_PUBLISHED` | 404 | 공개되지 않은 캠페인 접근 (공개 폼 서버) |
| `LINK_NOT_FOUND` | 404 | 배포 링크 없음, 또는 링크가 해당 캠페인 소속이 아님 |
| `INVALID_PUBLIC_SLUG` / `INVALID_CAMPAIGN_NAME` 등 | 400 | 도메인 엔티티 생성자 검증 실패 |

---

## 1. 인증 API (admin, 8080)

### `POST /admin/auth/login`
운영자 로그인. 인증 불필요.

요청
```json
{ "email": "operator@glowuprizz.com", "password": "glowup1234!" }
```

응답 `200 OK`
```json
{ "accessToken": "eyJhbGciOi..." }
```

실패: 이메일/비밀번호 불일치 시 `401` `AUTH_INVALID_CREDENTIALS`.

> 운영자 계정은 회원가입 API 없이 시드 데이터로만 생성된다 (`docs/adr/0012` 참고).

---

## 2. HTML 템플릿 관리 API (admin, 8080)

운영자가 AI로 만든 단일 `.html` 파일을 등록한다. 이후 캠페인 생성 시 참조한다.

### `POST /admin/html-templates`
요청
```json
{ "name": "가을 웨비나 신청폼", "content": "<html>...</html>" }
```
응답 `201 Created`, `Location: /admin/html-templates/{id}`
```json
{ "id": 1, "name": "가을 웨비나 신청폼", "createdAt": "2026-09-10T12:00:00Z" }
```
실패: `content`/`name`이 비어있으면 `400` `VALIDATION_FAILED`.

### `GET /admin/html-templates`
등록된 템플릿 목록 조회 (content 제외 요약 정보).
```json
[{ "id": 1, "name": "가을 웨비나 신청폼", "createdAt": "2026-09-10T12:00:00Z" }]
```

### `GET /admin/html-templates/{id}`
템플릿 상세(HTML 원문 포함) 조회.
```json
{ "id": 1, "name": "가을 웨비나 신청폼", "content": "<html>...</html>", "createdAt": "..." }
```
실패: 존재하지 않으면 `404` `TEMPLATE_NOT_FOUND`.

---

## 3. 캠페인(커스텀 신청 폼) 관리 API (admin, 8080)

캠페인 = 등록된 HTML 템플릿 + 공개 슬러그로 만든 커스텀 신청 폼 인스턴스다
(`docs/adr/0001` 참고).

### `POST /admin/campaigns`
요청
```json
{ "htmlTemplateId": 1, "name": "가을 웨비나", "publicSlug": "autumn-webinar" }
```
- `publicSlug`는 소문자/숫자/하이픈만 허용 (`^[a-z0-9]+(-[a-z0-9]+)*$`).

응답 `201 Created`, `Location: /admin/campaigns/{id}`
```json
{
  "id": 1, "htmlTemplateId": 1, "name": "가을 웨비나",
  "publicSlug": "autumn-webinar", "published": false, "createdAt": "..."
}
```
실패: 템플릿 없음 `404` `TEMPLATE_NOT_FOUND`, 슬러그 형식 오류 `400` `INVALID_PUBLIC_SLUG`.

### `GET /admin/campaigns`
캠페인 목록 조회.

### `GET /admin/campaigns/{id}`
캠페인 단건 조회. 실패: `404` `CAMPAIGN_NOT_FOUND`.

### `POST /admin/campaigns/{id}/publish`
캠페인을 공개 상태로 전환한다. 공개된 캠페인만 공개 폼 서버(`/f/{slug}`)에서 접근 가능하다.

### `POST /admin/campaigns/{id}/unpublish`
캠페인을 비공개로 전환한다.

---

## 4. 배포 링크 API (admin, 8080)

인스타그램/X/유튜브/스레드용 배포 링크를 캠페인 하위 리소스로 생성한다. 채널당 링크 개수
제한은 없다 (`docs/adr/0005` 참고).

### `POST /admin/campaigns/{campaignId}/links`
요청
```json
{ "channel": "INSTAGRAM" }
```
`channel`은 `INSTAGRAM` / `X` / `YOUTUBE` / `THREADS` 중 하나.

응답 `201 Created`, `Location: /admin/campaigns/{campaignId}/links/{id}`
```json
{
  "id": 10, "campaignId": 1, "channel": "INSTAGRAM",
  "linkToken": "b6b5...uuid", "createdAt": "..."
}
```
실패: 캠페인 없음 `404` `CAMPAIGN_NOT_FOUND`.

실제로 배포할 URL은 공개 폼 서버의 `GET /r/{linkToken}`이다 (아래 6번 참고).

### `GET /admin/campaigns/{campaignId}/links`
해당 캠페인의 배포 링크 목록 조회.

### `GET /admin/campaigns/{campaignId}/links/{id}`
배포 링크 단건 조회. 실패: `404` `LINK_NOT_FOUND`.

---

## 5. 성과 조회 API (admin, 8080)

캠페인별 방문/방문자/신청/전환율과 채널별 성과를 한 번에 조회한다. 채널별 성과는 캠페인
하위 스코프로 제공한다 (`docs/adr/0013` 참고). 전환율 = 신청 수 / 순 방문자(고유
`visitorToken`) 수, 소수 넷째 자리 반올림 (`docs/adr/0006`). 순 방문자가 0명이면
`conversionRate`는 `0`이다. 방문/신청이 1건도 없었던 채널은 `channels`에 나타나지 않는다.

### `GET /admin/campaigns/{campaignId}/stats`
응답 `200 OK`
```json
{
  "campaignId": 1,
  "visitCount": 4,
  "visitorCount": 3,
  "submissionCount": 2,
  "conversionRate": 0.6667,
  "channels": [
    {
      "channel": "INSTAGRAM",
      "visitCount": 2,
      "visitorCount": 1,
      "submissionCount": 1,
      "conversionRate": 1.0
    },
    {
      "channel": "X",
      "visitCount": 1,
      "visitorCount": 1,
      "submissionCount": 1,
      "conversionRate": 1.0
    }
  ]
}
```
실패: 캠페인 없음 `404` `CAMPAIGN_NOT_FOUND`.

---

## 6. 공개 폼 흐름 API (public, 8081, 인증 불필요)

방문자가 배포 링크를 클릭해 폼을 보고 신청을 제출하는 흐름이다. 이 서버는 `admin` 서버와
완전히 다른 프로세스/포트/오리진이며, 관리자 JWT나 `/admin/**` API에 접근할 방법이 없다
(`docs/adr/0011`). 등록된 HTML은 신뢰하지 않는 콘텐츠로 취급해 `Content-Security-Policy:
connect-src 'self'; frame-ancestors 'none'` 헤더를 붙여 렌더링한다.

익명 방문자 식별용 쿠키(`visitorToken`)는 최초 요청 시 자동 발급되며, 아래 세 엔드포인트가
공통으로 사용한다.

### `GET /r/{linkToken}`
배포 링크 클릭 진입점. 방문을 채널 정보와 함께 기록한 뒤 실제 폼으로 `302 Found` 리다이렉트한다
(`docs/adr/0007`).

응답 헤더
```
302 Found
Location: /f/{publicSlug}?link={linkToken}
```
실패: 링크 없음 `404` `LINK_NOT_FOUND`, 캠페인이 비공개면 `404` `CAMPAIGN_NOT_PUBLISHED`.

### `GET /f/{slug}?link={linkToken}`
공개 폼 HTML을 렌더링한다. `link` 쿼리 파라미터가 있으면 이미 `/r/{token}`에서 방문이 기록된
것으로 보고 중복 기록하지 않는다. 없으면(북마크 등 직접 접근) 채널 없는 방문으로 기록한다.

응답: `Content-Type: text/html`, 등록된 HTML 원문(+ 제출 스크립트 삽입).
실패: 슬러그 없음/비공개 캠페인 `404` `CAMPAIGN_NOT_PUBLISHED`.

### `POST /f/{slug}/submissions?link={linkToken}`
신청 데이터를 제출한다. `link`가 주어지고 해당 캠페인 소속이 아니면 존재하지 않는 링크와
동일하게 취급해 거부한다 (크로스 캠페인 링크 오염 방지).

요청 (폼 필드는 등록된 HTML마다 달라질 수 있어 스키마리스 JSON으로 받는다,
`docs/adr/0003`)
```json
{ "name": "홍길동", "phone": "010-1234-5678", "email": "hong@example.com" }
```
응답: `201 Created` (본문 없음)
실패: 캠페인 없음/비공개 `404` `CAMPAIGN_NOT_PUBLISHED`, `link`가 다른 캠페인 소속 `404`
`LINK_NOT_FOUND`.

---

## 시나리오 예시 (curl)

```bash
# 1) 로그인
TOKEN=$(curl -s -X POST http://localhost:8080/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"operator@glowuprizz.com","password":"glowup1234!"}' | jq -r .accessToken)

# 2) HTML 템플릿 등록
TEMPLATE_ID=$(curl -s -X POST http://localhost:8080/admin/html-templates \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"가을 웨비나","content":"<html><body><form>...</form></body></html>"}' | jq -r .id)

# 3) 캠페인 생성 + 공개
CAMPAIGN_ID=$(curl -s -X POST http://localhost:8080/admin/campaigns \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"htmlTemplateId\":$TEMPLATE_ID,\"name\":\"가을 웨비나\",\"publicSlug\":\"autumn-webinar\"}" | jq -r .id)
curl -s -X POST http://localhost:8080/admin/campaigns/$CAMPAIGN_ID/publish -H "Authorization: Bearer $TOKEN"

# 4) 인스타그램 배포 링크 생성
LINK_TOKEN=$(curl -s -X POST http://localhost:8080/admin/campaigns/$CAMPAIGN_ID/links \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"channel":"INSTAGRAM"}' | jq -r .linkToken)

# 5) 방문자가 배포 링크를 클릭 -> 폼 진입 -> 신청 (쿠키 유지를 위해 -c/-b 사용)
curl -sD - -o /dev/null -b cookies.txt -c cookies.txt http://localhost:8081/r/$LINK_TOKEN
curl -s -b cookies.txt -c cookies.txt \
  "http://localhost:8081/f/autumn-webinar/submissions?link=$LINK_TOKEN" \
  -H "Content-Type: application/json" -d '{"name":"홍길동"}'

# 6) 성과 확인
curl -s http://localhost:8080/admin/campaigns/$CAMPAIGN_ID/stats -H "Authorization: Bearer $TOKEN"
```
