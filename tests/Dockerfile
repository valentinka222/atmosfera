# Официальный образ Playwright: Java, Maven и уже установленные браузеры.
# Версия образа должна совпадать с версией Playwright в pom.xml
FROM mcr.microsoft.com/playwright/java:v1.49.0-noble

WORKDIR /tests

# Сначала зависимости — этот слой кэшируется, пока не меняется pom.xml
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Затем код тестов — и предварительная компиляция
COPY src ./src
RUN mvn -B -q test-compile

# Браузеры уже есть в образе — не скачивать их заново
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

# При запуске: очистить прошлые результаты, записать сведения об окружении
# для отчёта и прогнать тесты, сохраняя результаты в /results
ENTRYPOINT ["sh", "-c", "rm -rf /results/* && printf 'Environment=test\\nBase.URL=%s\\n' \"$BASE_URL\" > /results/environment.properties && mvn -B test -Dallure.results.directory=/results"]