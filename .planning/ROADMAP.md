# Roadmap: Lila Analysis API

이 로드맵은 Lichess의 분석 엔진을 활용한 PGN 분석 API 서버 구축 과정을 단계별로 정의합니다.

## Phases

- [x] **Phase 1: Foundation** - 프로젝트 빌드 설정 및 엔진 통신 기반 마련 (completed 2026-03-02)
- [x] **Phase 2: Engine Bridge** - Stockfish UCI 오케스트레이션 및 데이터 매핑 (completed 2026-03-02)
- [x] **Phase 3: Lichess Logic Integration** - 분석 정확도 및 오차 판정 로직 통합 (completed 2026-03-02)
- [x] **Phase 4: API & Reporting** - 외부 호출 인터페이스 및 상세 리포트 제공 (completed 2026-03-02)

## Phase Details

### Phase 1: Foundation
**Goal**: 프로젝트 빌드 환경을 구축하고 Stockfish 엔진과의 기본적인 통신을 확인합니다.
**Depends on**: Nothing
**Requirements**: CORE-01, ENG-01
**Success Criteria**:
  1. `lila.analyse` 및 `lila.tree` 모듈이 프로젝트 빌드 설정에 정상적으로 포함됨.
  2. 로컬 Stockfish 바이너리와 UCI 통신을 통해 "isready" 응답을 수신함.
  3. 기본 분석 환경 설정(JVM, Scala version)이 완료됨.
**Plans**:
- [x] 01-01-PLAN.md — Setup build settings and verify Stockfish communication.

### Phase 2: Engine Bridge
**Goal**: Stockfish를 제어하여 게임의 모든 수에 대한 평가값을 추출하고 Lila 모델로 변환합니다.
**Depends on**: Phase 1
**Requirements**: ENG-02, ENG-03
**Success Criteria**:
  1. 주어진 PGN의 모든 수에 대해 순차적 또는 병렬로 엔진 평가(CP/Mate)를 수행함.
  2. 추출된 원시 평가값이 `lila.tree.Eval` 모델로 정확히 매핑됨.
  3. 대량의 수(100수 이상) 분석 시 메모리 및 프로세스 관리가 안정적으로 유지됨.
**Plans**:
- [x] 02-01-PLAN.md — Implementation of the bridge between Stockfish UCI and Lila Eval models.

### Phase 3: Lichess Logic Integration
**Goal**: Lichess의 핵심 분석 로직을 적용하여 수별 등급 및 종합 정확도를 산출합니다.
**Depends on**: Phase 2
**Requirements**: CORE-02, CORE-03, REP-01
**Success Criteria**:
  1. `lila.tree.Advice`를 통해 각 수에 대해 Blunder, Mistake, Inaccuracy 판정이 수행됨.
  2. Lichess 웹사이트와 동일한 기준의 종합 정확도(Accuracy %)가 계산됨.
  3. Lichess 스타일의 "Brilliant" 및 "Great Move" 판정 로직이 동작함.
**Plans**:
- [x] 03-01-PLAN.md — Implementation of Lichess-style move judgements and accuracy logic.

### Phase 4: API & Reporting
**Goal**: 분석 결과를 외부에서 호출할 수 있는 API를 제공하고 리포트 형식을 완성합니다.
**Depends on**: Phase 3
**Requirements**: API-01, API-02, API-03, REP-02
**Success Criteria**:
  1. POST 요청으로 PGN을 입력받아 분석을 시작하는 엔드포인트가 동작함.
  2. 분석 완료 시 정확도, 오차 요약이 포함된 JSON 결과를 반환함.
  3. Lichess 스타일의 주석(Glyphs, Variations)이 포함된 분석된 PGN 텍스트를 반환함.
  4. 게임 단계별(Opening/Mid/End) 통계 및 ECO 정보가 리포트에 포함됨.
**Plans**:
- [ ] 04-01-PLAN.md — Implementation of the analysis API and advanced reporting.

## Progress Table

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Foundation | 1/1 | Complete    | 2026-03-02 |
| 2. Engine Bridge | 1/1 | Complete | 2026-03-02 |
| 3. Lichess Logic Integration | 1/1 | Complete | 2026-03-02 |
| 4. API & Reporting | 0/1 | Complete    | 2026-03-02 |

---
*Last updated: 2026-03-02*
