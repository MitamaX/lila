# Requirements: Lila Analysis API

이 프로젝트는 Lichess의 분석 엔진을 100% 동일하게 활용하여 PGN 분석 결과를 제공하는 API를 목표로 합니다.

## v1 Requirements

### CORE: Lichess Logic Integration
- [ ] **CORE-01**: `modules/analyse` 및 `modules/tree` 모듈 의존성 설정
- [ ] **CORE-02**: `lila.tree.Advice`를 활용한 수별 오차(Blunder, Mistake, Inaccuracy) 판정 로직 통합
- [ ] **CORE-03**: `lila.analyse.AccuracyPercent`를 활용한 Lichess 스타일의 정확도(Accuracy %) 산출

### ENGINE: Stockfish Orchestration
- [ ] **ENG-01**: Stockfish 16+ NNUE 연동 및 UCI 통신 구현
- [ ] **ENG-02**: 게임 전체 수에 대한 순차적/병렬 엔진 분석 수행 및 평가값(CP/Mate) 추출
- [ ] **ENG-03**: 엔진 분석 데이터를 Lichess `Eval` 모델로 매핑

### API: Analysis Service
- [ ] **API-01**: PGN 문자열을 입력받아 분석 작업을 생성하는 엔드포인트
- [ ] **API-02**: 분석 완료 후 정확도, 오차 요약, 엔진 주석이 포함된 JSON 결과 반환
- [ ] **API-03**: 분석 주석(Glyphs)과 베스트 무브가 포함된 분석된 PGN 텍스트 반환

### REPORT: Advanced Metrics
- [ ] **REP-01**: Brilliant/Great Move 판정 로직 구현 (Lichess 코어 외 추가 차별화 기능)
- [ ] **REP-02**: 오프닝 정보(ECO) 및 게임 단계별(Opening/Middlegame/Endgame) 통계 리포트

## v2 / Deferred Requirements
- [ ] **DIST-01**: Fishnet(분산 분석) 연동 지원
- [ ] **AUTH-01**: API 호출 제한(Rate Limiting) 및 사용자 인증
- [ ] **WEB-01**: 분석 결과를 시각화하는 간단한 웹 대시보드

## Out of Scope
- 체스 대국 엔진 개발 (기존 Stockfish 활용)
- 영구적인 게임 데이터 저장 및 관리 (Stateless API)

---
*Last updated: 2026-03-01*
