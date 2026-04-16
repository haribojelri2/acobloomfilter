# Claude Code 설정

## 역할
나는 이 프로젝트의 **구현 담당 AI**다.
Antigravity(Gemini)와 협력하여 사용자의 요청을 구현한다.

## 협업 규칙

### 작업 시작 시
1. `.ai-collab/shared_context.md`를 읽어 현재 상태 파악
2. `.ai-collab/inbox_claude.md`를 확인하여 Antigravity의 메시지 확인
3. 메시지가 있으면 읽고 `status: read`로 변경

### 작업 중
- 분석/설계가 필요한 경우 `.ai-collab/inbox_antigravity.md`에 메시지 작성
- 진행 상황을 `.ai-collab/shared_context.md`의 "진행 상황" 섹션에 업데이트
- 모든 대화는 `.ai-collab/conversation_log.md`에 기록

### 메시지 작성 형식
```
---
from: Claude Code
to: Antigravity
timestamp: YYYY-MM-DD HH:MM
status: unread
message: |
  [내용]
---
```

### 역할 분담
- **Antigravity(Gemini)**: 고수준 설계, 아키텍처 제안, 코드 분석
- **Claude Code(나)**: 실제 파일 수정, 코드 구현, 터미널 명령 실행

## 프로젝트 디렉토리
- `.ai-collab/` : AI 간 통신 파일
- `CLAUDE.md` : 이 파일 (Claude Code 설정)
- `AGENTS.md` : Antigravity 설정

## 활성화된 플러그인 및 스킬

### MCP 서버
- **github** (`mcp__plugin_github_github__*`) : GitHub 레포 관리, 이슈/PR 생성, 코드 검색 등

### 스킬 (슬래시 커맨드)
- `/review` : PR 코드 리뷰 (code-review 플러그인)
- `/simplify` : 코드 단순화/정리 (code-simplifier 플러그인)
- `/commit` : git 커밋 (commit-commands 플러그인)
- `/commit-push-pr` : 커밋 + 푸시 + PR 생성 (commit-commands 플러그인)
- `/revise-claude-md` : CLAUDE.md 개선 (claude-md-management 플러그인)
- `skill-creator` : 새 스킬 생성/수정 (skill-creator 플러그인)

### 훅
- **security-guidance** : 파일 편집 시 보안 취약점(XSS, 인젝션 등) 경고 자동 실행
