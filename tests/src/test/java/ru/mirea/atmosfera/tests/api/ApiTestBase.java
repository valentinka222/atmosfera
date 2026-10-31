package ru.mirea.atmosfera.tests.api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import io.restassured.config.EncoderConfig;
import org.junit.jupiter.api.BeforeAll;
import ru.mirea.atmosfera.tests.TestConfig;

import java.time.LocalDateTime;
import java.util.Map;

public abstract class ApiTestBase {

    @BeforeAll
    static void setUpRestAssured() {
        RestAssured.baseURI = TestConfig.BASE_URL;

        // Русский текст в запросах и ответах — в UTF-8
        RestAssured.config = RestAssured.config()
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"))
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"));

        // Каждый запрос и ответ — вложением в отчёт Allure
        RestAssured.replaceFiltersWith(new AllureRestAssured());
    }

    /** Тело запроса на запись */
    protected static Map<String, Object> booking(long masterId, long serviceId,
                                                 LocalDateTime startAt, String email) {
        return Map.of(
                "masterId", masterId,
                "serviceId", serviceId,
                "startAt", startAt.toString(),
                "clientName", "Test Client",
                "clientPhone", "+7 900 000-00-00",
                "clientEmail", email);
    }
}