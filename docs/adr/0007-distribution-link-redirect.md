# ADR-0007: 배포 링크는 리다이렉트 링크로 구현하고, 캠페인에 공개 여부 플래그를 둔다

## 상태
Accepted

## 맥락
"커스텀 신청 폼은 배포된 채널의 데이터를 수집합니다"라는 요구사항을 만족하려면, 방문자가
어느 채널의 링크를 타고 들어왔는지를 정확히 기록해야 한다. 또한 AI가 만든 HTML은 우리
백엔드 API를 알지 못하므로, 신청 데이터를 어떻게 우리 서버로 전달할지 별도 설계가 필요하다.

## 결정
1. 배포 링크는 실제 폼 URL과 분리된 리다이렉트 링크(`GET /r/{linkToken}`)로 만든다. 이
   엔드포인트가 Visit을 기록(campaignId, distributionLinkId, channel, visitorToken)한 뒤
   실제 폼(`/f/{campaignSlug}?channel=...&link=...`)으로 302 리다이렉트한다.
2. `/f/{campaignSlug}`는 등록된 HTML을 그대로 서빙하되, `</body>` 직전에 폼 제출을
   가로채 우리 제출 API로 POST하는 JS 스니펫을 서버가 주입한다. 이 스니펫이 URL
   쿼리파라미터의 channel/link 값을 함께 전송한다.
3. Campaign에 `published`(boolean) 플래그를 추가한다. 미공개 캠페인의 `/r/{token}`,
   `/f/{slug}`는 404를 반환한다.

## 근거
- 리다이렉트 링크와 실제 폼 URL을 분리하면, 방문 로그를 "링크 클릭" 시점에 정확히 남길 수
  있고, bit.ly류의 트래킹 링크와 동일한 검증된 패턴이다.
- JS 스니핏 주입 방식은 운영자가 만든 HTML이 우리 백엔드 연동 방법을 전혀 몰라도 되게 한다
  ("개발자의 도움 없이 AI로 폼을 만든다"는 전제와 부합).
- published 플래그는 "폼을 만들었지만 아직 배포 전" 상태를 표현할 수 있게 하고, 미공개
  캠페인 접근 시도라는 자연스러운 실패 케이스 테스트를 만들 수 있게 한다.

## 결과
- 장점: HTML 등록 단계에서 운영자가 아무 것도 신경 쓸 필요가 없다. 방문/제출 모두 채널
  귀속이 명확하다.
- 단점: `/f/{slug}`에 직접(리다이렉트 링크 없이) 접근하면 channel/link 정보가 없다 — 이
  경우 Visit.distributionLinkId/channel을 null로 기록한다 (Visit/Submission 스키마가
  이미 nullable로 설계됨).
