# Phase Context: Dockerization & Deployment

## Phase Goal
독립적인 실행 환경을 구축하기 위해 프로젝트를 Docker 이미지로 패키징하고, 통합 실행을 위한 배포 설정을 완료합니다.

## Requirements
- **DEPLOY-01**: `sbt-native-packager`를 활용한 멀티 스테이지 `Dockerfile` 작성.
- **DEPLOY-02**: Stockfish 엔진 바이너리 포함 및 컨테이너 내 실행 경로 설정.
- **DEPLOY-03**: `docker-compose.yml`을 통한 애플리케이션 및 필수 인프라(Redis/MongoDB) 오케스트레이션.
- **DEPLOY-04**: 컨테이너 헬스체크 및 환경 변수를 통한 설정 자동화.

## Success Criteria
1. `docker build`를 통해 생성된 이미지가 정상적으로 실행됨.
2. 컨테이너 내부에서 외부 PGN 분석 요청을 처리하고 Stockfish 결과를 반환함.
3. `docker-compose up` 명령만으로 전체 분석 서비스 환경이 구동됨.

## Reference Files
- `build.sbt`: 프로젝트 빌드 및 의존성 설정.
- `.planning/research/STACK.md`: 기술 스택 및 버전 정보.
- `.planning/research/ARCHITECTURE.md`: 컴포넌트 구조 및 패키징 전략.
