# implement 워크플로우

사용자의 요청을 받아 analyzer → designer → Claude Code implementer 순서로 협업하여 구현한다.

## 실행 순서
1. analyzer 스킬로 요청 분석
2. designer 스킬로 설계 결정 및 Claude Code에 지시
3. Claude Code implementer가 구현 완료 후 보고 대기
4. reviewer로 결과 검토

## 사용법
Antigravity에서 `/implement`를 입력하면 이 워크플로우가 실행된다.
