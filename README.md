# Атмосфера — тестовая среда веб-приложения

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

## Требования

Docker Desktop (Docker Engine, Docker Compose v2), Git Bash (на Windows).