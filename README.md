# Атмосфера — тестовая среда веб-приложения
![CI/CD](https://github.com/valentinka222/atmosfera/actions/workflows/ci.yml/badge.svg)

Курсовая работа по дисциплине «Настройка и администрирование сервисного программного обеспечения».
Тема: настройка и администрирование управления тестовой средой для разработки и тестирования веб-приложений.

Тестируемое приложение — онлайн-запись в салон красоты «Атмосфера».

## Состав

| Сервис | Назначение |
|---|---|
| proxy | nginx, единая точка входа: `/` -> frontend, `/api/` -> backend |
| frontend | nginx со статическими страницами (HTML, CSS, JS) |
| backend | Spring Boot 4, Java 21: REST API записи |
| db | PostgreSQL 16, миграции Flyway |
| mailpit | фейковый SMTP-сервер для проверки писем |
| adminer | веб-интерфейс к базе данных |

## Окружения

| | dev | test |
|---|---|---|
| Сайт | http://localhost | http://localhost:8090 |
| MailPit | http://localhost:8025 | http://localhost:8026 |
| Adminer | http://localhost:8081 | http://localhost:8082 |
| База данных | том, открыта на порту 5433 | tmpfs, одноразовая, закрыта |
| Отчёт Allure | — | http://localhost:8083/allure-docker-service/latest-report |

## Управление

```bash
bash scripts/env.sh <dev|test> <up|down|reset|status|logs>
bash scripts/env.sh test tests   # сбросить test и прогнать автотесты
```

Примеры:

```bash
bash scripts/env.sh test up      # поднять тестовую среду
bash scripts/env.sh test reset   # сбросить к эталонному состоянию
bash scripts/env.sh dev down     # остановить среду разработки
```

## Кластер (stage)

Окружение stage разворачивается в Docker Swarm из образов локального registry.

```bash
bash scripts/swarm.sh init          # включить Swarm и поднять registry (localhost:5000)
bash scripts/swarm.sh push 1.0      # собрать образы и отправить в registry
bash scripts/swarm.sh deploy 1.0    # развернуть стек atmosfera-stage
bash scripts/swarm.sh status        # сервисы и реплики
bash scripts/swarm.sh rm            # удалить стек
```

Сайт: http://localhost:8100, MailPit: http://localhost:8027.
Бэкенд и фронтенд работают в двух репликах; обновление — плавающее,
с автоматическим откатом при неудачной проверке здоровья.

## Администрирование (Portainer)

```bash
docker compose -f docker-compose.portainer.yml up -d
```

Веб-панель: https://localhost:9443 (самоподписанный сертификат).
Показывает контейнеры, сервисы и стеки всех окружений, тома, сети и образы;
позволяет просматривать логи, статистику, открывать консоль контейнера
и масштабировать сервисы кластера.

Portainer получает полный доступ к Docker через сокет — только для локального
администрирования.

## Требования

Docker Desktop (Docker Engine, Docker Compose v2), Git Bash (на Windows).