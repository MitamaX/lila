# Project: Lila Analysis API

Lichess(`lila`)의 분석 엔진을 100% 동일하게 활용하여 PGN 게임 분석 결과를 제공하는 이식성 높은 API 서버를 구축합니다.

## Core Value
사용자에게 Lichess와 동일한 기준의 정확도(Accuracy), 실수(Blunder), 묘수(Brilliant) 판정 및 분석 리포트를 제공합니다.

## Objectives
- **Lichess 코어 통합**: `modules/analyse`, `modules/game` 등 기존 Scala 코드를 직접 참조하여 분석 로직의 동일성 보장.
- **Stockfish 연동**: Stockfish CLI 또는 라이브러리를 통해 수순별 평가값(CP/Mate) 및 최선의 수 추출.
- **분석 리포트 API**: 정확도 계산, 오차 요약, 엔진 주석이 달린 PGN을 반환하는 RESTful/RPC API 제공.
- **이식성**: 다른 시스템에서 쉽게 호출하고 활용할 수 있는 독립적인 서비스 형태.

## Tech Stack
- **Language**: Scala (Lichess core compatibility)
- **Framework**: Play Framework or Akka-HTTP (Lichess standard)
- **Engine**: Stockfish 16+
- **Analysis Core**: `lila.analyse` (Advice.scala, Annotator.scala)

## Requirements

### Active
- [ ] **CORE-01**: Lichess `analyse` 모듈 의존성 설정 및 코드 참조 환경 구축
- [ ] **ENGINE-01**: Stockfish 연동 및 수순별 평가 데이터 수집 로직 구현
- [ ] **REPORT-01**: Lichess 기준의 정확도(Accuracy) 산출 로직 구현
- [ ] **REPORT-02**: Blunder, Mistake, Inaccuracy, Brilliant 판정 및 요약 기능
- [ ] **API-01**: PGN 입력을 받아 분석 결과를 JSON/PGN으로 반환하는 API 엔드포인트 구현
- [ ] **ANNOT-01**: 기호 및 주석이 포함된 Lichess 스타일 PGN 생성

### Out of Scope
- 체스 대국 UI (분석 결과 데이터 제공에 집중)
- 사용자 계정 및 데이터베이스 저장 (Stateless API 지향)

## Key Decisions
| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Lichess 모듈 직접 참조 | 분석 로직의 100% 일치성을 위해 필수적임 | Pending |
| API 형태 제공 | 다른 서비스와의 통합 및 이식성을 위함 | Pending |

---
*Last updated: 2026-03-01 after initialization*
