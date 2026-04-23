# Claude Code 설정

## 역할
나는 이 프로젝트의 **구현 담당 AI**다.
사용자의 요청을 분석하고 직접 구현한다.

## 작업 규칙

### 실행 전
- **advisor를 반드시 호출**하여 접근 방식을 검토받은 후 구현을 시작한다.

### 작업 중
- 파일 수정, 코드 구현, 터미널 명령 실행을 담당한다.

### 실행 후
- **advisor를 반드시 호출**하여 완료된 작업을 검토받는다.
- 작업이 완료됐다고 선언하기 전에 advisor 검토를 거친다.

### advisor 응답 형식
- **100단어 이내** 열거형(bullet list)으로만 답한다.
- 산문 설명 없이 핵심 항목만 나열한다.

## 프로젝트 디렉토리
- `CLAUDE.md` : 이 파일 (Claude Code 설정)
- `src/` : Java 소스코드
- `out/` : 빌드/출력 파일

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
- `paper-verifier` : 코드가 논문 알고리즘과 일치하는지 검증 (.claude/agents/paper-verifier.md)

### 훅
- **security-guidance** : 파일 편집 시 보안 취약점(XSS, 인젝션 등) 경고 자동 실행

## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:
- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost)
