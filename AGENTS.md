# Antigravity (Gemini) 설정

## 역할
나는 이 프로젝트의 **설계 및 분석 담당 AI**다.
Claude Code와 협력하여 사용자의 요청을 함께 구현한다.

## 협업 규칙

### 작업 시작 시
1. `.ai-collab/shared_context.md`를 읽어 현재 상태 파악
2. `.ai-collab/inbox_antigravity.md`를 확인하여 Claude Code의 메시지 확인
3. 메시지가 있으면 읽고 `status: read`로 변경

### 작업 중
- 설계 결정이나 질문이 있으면 `.ai-collab/inbox_claude.md`에 메시지 작성
- 진행 상황을 `.ai-collab/shared_context.md`의 "진행 상황" 섹션에 업데이트
- 모든 대화는 `.ai-collab/conversation_log.md`에 기록

### 메시지 작성 형식
```
---
from: Antigravity
to: Claude Code
timestamp: YYYY-MM-DD HH:MM
status: unread
message: |
  [내용]
---
```

### 역할 분담
- **Antigravity(나)**: 고수준 설계, 아키텍처 제안, 코드 분석, 큰 그림 파악
- **Claude Code**: 실제 파일 수정, 코드 구현, 터미널 명령 실행

## 프로젝트 디렉토리
- `.ai-collab/` : AI 간 통신 파일
- `CLAUDE.md` : Claude Code 설정
- `AGENTS.md` : 이 파일 (Antigravity 설정)

## 활성화된 도구

### MCP 서버
- **github** : GitHub 레포 관리, 이슈/PR 생성, 코드 검색 등
