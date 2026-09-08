# ADR-0010: 연관관계는 `@ManyToOne` 대신 평범한 FK id(Long) 컬럼으로 둔다

## 상태
Accepted

## 맥락
Campaign, DistributionLink, Visit, Submission은 서로 참조 관계(operatorId, htmlTemplateId,
campaignId, distributionLinkId)를 갖는다. JPA의 관용적인 방법은 `@ManyToOne` 객체 참조로
연관관계를 매핑하는 것이다.

## 결정
모든 참조를 `@ManyToOne` 객체 그래프가 아니라 평범한 `Long` FK 컬럼으로 둔다. FK 제약조건
자체는 Flyway 마이그레이션의 `REFERENCES`로 DB 레벨에서 강제한다.

## 근거
- Visit/Submission은 전형적인 조회·집계 위주 테이블이다 (캠페인별/채널별 카운트). 객체
  그래프를 따라갈 일이 거의 없고, `@ManyToOne`을 쓰면 지연 로딩 프록시, N+1 쿼리,
  직렬화 시 무한 순환 참조 같은 문제를 신경 써야 한다.
- Campaign 하나에 Visit/Submission이 대량으로 쌓이는 구조라, 양방향 연관관계
  (`@OneToMany`)를 열어두면 컬렉션 전체를 불필요하게 로딩할 위험이 생긴다.
- 성과 조회는 어차피 리포지토리 쿼리 메서드(또는 JPQL 집계)로 직접 짜야 하므로, 객체
  그래프 탐색이 주는 이점이 크지 않다.

## 결과
- 장점: 엔티티가 단순해지고, 의도치 않은 지연 로딩/N+1 위험이 없다.
- 단점: 참조 무결성이 애플리케이션 레벨에서는 컴파일 타임에 보장되지 않는다 (예: 존재하지
  않는 campaignId로 Visit을 만드는 실수는 DB FK 제약 위반 시점에야 잡힌다). 서비스 레이어에서
  참조 대상 존재 여부를 먼저 조회해 확인하는 방식으로 보완한다.
