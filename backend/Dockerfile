# ========== Этап 1: сборка .jar ==========
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Сначала только pom.xml — и скачиваем зависимости.
# Пока pom.xml не меняется, этот слой берётся из кэша,
# и при правке кода зависимости заново не качаются
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Теперь исходный код — и сборка
COPY src ./src
RUN mvn -B -q package -DskipTests

# ========== Этап 2: лёгкий образ для запуска ==========
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Запускаем приложение не от root, а от обычного пользователя:
# если в приложении найдут уязвимость, у злоумышленника будет меньше прав
RUN addgroup -S app && adduser -S app -G app

# Из первого этапа берём только готовый .jar
COPY --from=build /app/target/*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]