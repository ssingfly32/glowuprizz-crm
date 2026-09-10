# ADR-0014: 예상 못한 예외를 위한 fallback 예외 핸들러를 둔다

## 상태
Accepted

## 맥락
`AdminExceptionHandler`/`PublicExceptionHandler`는 `BusinessException`(+ admin은
`MethodArgumentNotValidException`)만 처리했다. 그 외 예외(예: 요청 바디가 깨진 JSON일 때
발생하는 `HttpMessageNotReadableException`)는 잡히지 않고 Spring 기본 예외 처리로 빠졌다.

실제로 확인해보니 문제가 예상보다 컸다: 이 경우 Spring이 `400`을 반환하긴 하지만 **바디가
완전히 비어 있었다** (`{code,message}` 계약이 깨지는 정도가 아니라 아무 정보도 없음).
이전에 고친 publicSlug 중복 생성 버그(`DataIntegrityViolationException` 미처리 → 500)도
같은 클래스의 문제였다 — "예상한 예외만 잡고 나머지는 프레임워크 기본값에 맡긴다"는 구조라,
발견되지 않은 케이스마다 개별로 패치해야 하는 상황이었다.

## 결정
두 `RestControllerAdvice`(`AdminExceptionHandler`, `PublicExceptionHandler`) 모두에
`@ExceptionHandler(Exception.class)` catch-all을 추가한다. 여기 걸리는 모든 예외는
`ErrorCode.INTERNAL_ERROR`로 뭉뚱그려 `500` + `{code:"INTERNAL_ERROR", message:"..."}`로
응답하고, 서버 로그에 스택트레이스를 남긴다(`log.error`).

## 근거
- 목표는 "모든 예외를 정확한 HTTP 상태로 분류"가 아니라 "이 API가 절대 프레임워크 기본
  포맷이나 빈 바디를 클라이언트에 노출하지 않는다"는 계약을 보장하는 것이다. 세분화된 분류는
  필요해질 때 `BusinessException`에 새 `ErrorCode`를 추가하는 방식으로 점진적으로 좁히면 된다.
- 이전에 다른 프로젝트(architecture-study)에서 동일한 패턴(`GlobalExceptionHandler`의
  `Exception.class` catch-all)을 이미 써봤고, 거기서도 "ErrorCode는 순수 비즈니스 의미만
  담고 HTTP 상태 매핑은 presentation 계층 책임"이라는 원칙과 함께 잘 동작했다.

## 결과
- 장점: 개별 미처리 예외를 하나씩 찾아 패치하는 대신, 아직 발견되지 않은 예외까지 한 번에
  방어한다. 클라이언트는 항상 `{code, message}` 형식의 응답을 받는다는 계약이 보장된다.
- 단점: 진짜 클라이언트 오류(예: 깨진 JSON)도 `500`으로 응답하게 되어 의미상 부정확할 수
  있다. 특정 케이스가 자주 발생해 `400`으로 세분화할 가치가 생기면, 그때 해당 예외 타입을
  전용 `@ExceptionHandler`로 승격하면 된다.
