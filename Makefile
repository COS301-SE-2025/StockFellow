# Makefile for easier deployment
.PHONY: dev-up dev-down dev-build dev-logs dev-health prod-up prod-down prod-build prod-logs prod-health clean

# Development commands
dev-up:
	@./deploy.sh dev up

dev-down:
	@./deploy.sh dev down

dev-build:
	@./deploy.sh dev build

dev-logs:
	@./deploy.sh dev logs

dev-health:
	@./deploy.sh dev health

# Production commands
prod-up:
	@./deploy.sh prod up

prod-down:
	@./deploy.sh prod down

prod-build:
	@./deploy.sh prod build

prod-logs:
	@./deploy.sh prod logs

prod-health:
	@./deploy.sh prod health

# Utility commands
clean-dev:
	@./deploy.sh dev clean

clean-prod:
	@./deploy.sh prod clean

help:
	@echo "Available commands:"
	@echo "Development:"
	@echo "  make dev-up      - Start development environment"
	@echo "  make dev-down    - Stop development environment"
	@echo "  make dev-build   - Build development images"
	@echo "  make dev-logs    - Show development logs"
	@echo "  make dev-health  - Check development health"
	@echo ""
	@echo "Production:"
	@echo "  make prod-up     - Start production environment"
	@echo "  make prod-down   - Stop production environment"
	@echo "  make prod-build  - Build production images"
	@echo "  make prod-logs   - Show production logs"
	@echo "  make prod-health - Check production health"
	@echo ""
	@echo "Cleanup:"
	@echo "  make clean-dev   - Clean development environment"
	@echo "  make clean-prod  - Clean production environment"