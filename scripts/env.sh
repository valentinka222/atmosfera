#!/usr/bin/env bash
# =========================================================
# Управление окружениями салона «Атмосфера»
#
#   bash scripts/env.sh <окружение> <действие>
#
#   окружение: dev | test
#   действие:  up      — собрать образы и поднять среду
#              down    — остановить среду (данные dev сохраняются)
#              reset   — удалить среду вместе с данными и поднять заново
#              status  — состояние контейнеров
#              logs    — логи бэкенда (или указанного сервиса)
# =========================================================

# Остановиться при первой же ошибке
set -euo pipefail

# Работаем из корня репозитория, откуда бы ни запустили скрипт
cd "$(dirname "$0")/.."

ENV_NAME="${1:-}"
ACTION="${2:-}"

usage() {
    echo "Использование: bash scripts/env.sh <dev|test> <up|down|reset|status|logs> [сервис]"
    exit 1
}

case "$ENV_NAME" in
    dev|test) ;;
    *) usage ;;
esac

ENV_FILE="env/$ENV_NAME.env"
PROJECT="atmosfera-$ENV_NAME"

# Загружаем переменные среды, чтобы показать адреса после запуска
set -a
source "$ENV_FILE"
set +a

# Общая часть команды: имя проекта, файл переменных, базовый файл + надстройка
COMPOSE=(docker compose
         -p "$PROJECT"
         --env-file "$ENV_FILE"
         -f docker-compose.yml
         -f "docker-compose.$ENV_NAME.yml")

print_urls() {
    echo
    echo "Окружение $ENV_NAME готово:"
    echo "  Сайт:     http://localhost:${APP_PORT}"
    echo "  MailPit:  http://localhost:${MAILPIT_PORT}"
    echo "  Adminer:  http://localhost:${ADMINER_PORT}  (сервер db, база ${POSTGRES_DB})"
    if [ "$ENV_NAME" = "dev" ]; then
        echo "  База для IntelliJ: localhost:${DB_PUBLIC_PORT}"
    fi
}

case "$ACTION" in
    up)
        # --wait — дождаться, пока все healthcheck станут успешными
        "${COMPOSE[@]}" up -d --build --wait
        print_urls
        ;;
    down)
        "${COMPOSE[@]}" down
        ;;
    reset)
        echo "Сброс окружения $ENV_NAME: удаляем контейнеры и данные…"
        "${COMPOSE[@]}" down -v
        "${COMPOSE[@]}" up -d --build --wait
        print_urls
        ;;
    status)
        "${COMPOSE[@]}" ps
        ;;
    logs)
        "${COMPOSE[@]}" logs -f "${3:-backend}"
        ;;
    *)
        usage
        ;;
esac