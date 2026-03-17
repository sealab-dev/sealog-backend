# ── 변수 선언 ─────────────────────────────────────────────────────────────────
DEV_COMPOSE_FILE := docker/docker-compose.yml
PERF_COMPOSE_FILE := docker/docker-compose.test.server.yml
DOCKERFILE := docker/Dockerfile

# ── 기본 타겟 ─────────────────────────────────────────────────────────────────
.DEFAULT_GOAL := help
.PHONY: help \
        dev-up dev-down dev-reset \
        perf-up perf-down perf-reset \

help:
	@echo ""
	@echo "  Sealog — 사용 가능한 명령어"
	@echo "  ─────────────────────────────────────────────────────"
	@echo "  [Docker]"
	@echo "    make dev-up      개발환경 시작"
	@echo "    make dev-down    개발환경 중지"
	@echo "    make dev-reset   개발환경 재시작(데이터 초기화)"
	@echo ""
	@echo "    make perf-up     테스트 환경 시작"
	@echo "    make perf-down   테스트 환경 중지(데이터 초기화)"
	@echo "    make perf-reset  테스트 환경 재시작(데이터 초기화)"
	@echo ""
	@echo "  ─────────────────────────────────────────────────────"
	@echo ""

# ── Docker ────────────────────────────────────────────────────────────────────────
dev-up:
	docker compose -f $(DEV_COMPOSE_FILE) up -d

dev-down:
	docker compose -f $(DEV_COMPOSE_FILE) down

dev-reset:
	docker compose -f $(DEV_COMPOSE_FILE) down -v
	docker compose -f $(DEV_COMPOSE_FILE) up -d

perf-up:
	docker compose -f $(PERF_COMPOSE_FILE) up -d --build

perf-down:
	docker compose -f $(PERF_COMPOSE_FILE) down -v

perf-reset:
	docker compose -f $(PERF_COMPOSE_FILE) down -v
	docker compose -f $(PERF_COMPOSE_FILE) up -d --build