#!/usr/bin/env bash
# =========================================================
# Окружение stage в кластере Docker Swarm
#
#   bash scripts/swarm.sh <действие> [версия]
#
#   init            — включить Swarm и поднять локальный registry
#   push <версия>   — собрать образы и отправить их в registry
#   deploy <версия> — развернуть или обновить стек
#   status          — сервисы и реплики стека
#   rm              — удалить стек
# =========================================================

set -euo pipefail
cd "$(dirname "$0")/.."

ACTION="${1:-}"
VERSION="${2:-1.0}"
REGISTRY="localhost:5000"
STACK="atmosfera-stage"
IMAGES=(backend frontend proxy)

usage() {
    echo "Использование: bash scripts/swarm.sh <init|push|deploy|status|rm> [версия]"
    exit 1
}

case "$ACTION" in
    init)
        # Включить Swarm, если он ещё не включён
        if [ "$(docker info --format '{{.Swarm.LocalNodeState}}')" != "active" ]; then
            docker swarm init
        else
            echo "Swarm уже включён"
        fi
        # Локальный registry — тоже сервис кластера
        if ! docker service inspect registry > /dev/null 2>&1; then
            docker service create --name registry \
                --publish published=5000,target=5000 \
                registry:2
        else
            echo "Registry уже запущен"
        fi
        echo
        docker node ls
        ;;
    push)
        echo "Сборка образов версии $VERSION…"
        docker compose --env-file env/dev.env build backend frontend proxy
        for name in "${IMAGES[@]}"; do
            # Тот же образ получает второе имя — с адресом registry и версией
            docker tag "atmosfera-$name:latest" "$REGISTRY/atmosfera-$name:$VERSION"
            docker push "$REGISTRY/atmosfera-$name:$VERSION"
        done
        echo
        echo "Образы в registry:"
        curl -s "http://$REGISTRY/v2/_catalog"
        echo
        ;;
    deploy)
        echo "Развёртывание стека $STACK, версия $VERSION…"
        VERSION="$VERSION" docker stack deploy -c docker-stack.yml "$STACK"
        echo
        echo "Сайт:    http://localhost:8100"
        echo "MailPit: http://localhost:8027"
        echo "Состояние: bash scripts/swarm.sh status"
        ;;
    status)
        docker stack services "$STACK"
        echo
        docker stack ps "$STACK"
        ;;
    rm)
        docker stack rm "$STACK"
        ;;
    *)
        usage
        ;;
esac