package ru.mirea.atmosfera.tests.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mirea.atmosfera.tests.TestConfig;

import java.util.HashSet;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("API")
@Feature("Каталог и расписание")
class CatalogApiTest extends ApiTestBase {

    @Test
    @DisplayName("Список услуг: 10 услуг в 4 категориях")
    void servicesAreListed() {
        List<String> categories = given()
                .when().get("/api/services")
                .then()
                .statusCode(200)
                .body("size()", equalTo(10))
                .extract().jsonPath().getList("category");

        assertThat(new HashSet<>(categories))
                .containsExactlyInAnyOrder("Брови", "Волосы", "Ногти", "Уход за лицом");
    }

    @Test
    @DisplayName("Окрашивание выполняет только мастер, у которого есть эта услуга")
    void onlyQualifiedMastersAreOffered() {
        given()
                .when().get("/api/services/3/masters")
                .then()
                .statusCode(200)
                .body("fullName", contains("Анна Смирнова"));
    }

    @Test
    @DisplayName("Уволенный мастер не предлагается клиентам")
    void inactiveMasterIsHidden() {
        given()
                .when().get("/api/services/5/masters")
                .then()
                .statusCode(200)
                .body("fullName", contains("Ольга Иванова"))
                .body("fullName", not(hasItem("Ирина Соколова")));
    }

    @Test
    @DisplayName("Свободные слоты не пересекаются с существующей записью")
    void slotsSkipBookedTime() {
        List<String> starts = given()
                .queryParam("serviceId", 1)
                .queryParam("date", TestConfig.nextMonday().toString())
                .when().get("/api/masters/1/slots")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("start");

        // У Анны тестовая запись 12:00–13:00, стрижка длится 60 минут
        assertThat(starts)
                .contains("10:00:00", "11:00:00", "13:00:00")
                .doesNotContain("11:30:00", "12:00:00", "12:30:00");
        // Рабочий день до 19:00 — последняя стрижка в 18:00
        assertThat(starts.getLast()).isEqualTo("18:00:00");
    }

    @Test
    @DisplayName("В выходной день свободного времени нет")
    void noSlotsOnDayOff() {
        given()
                .queryParam("serviceId", 1)
                .queryParam("date", TestConfig.nextSunday().toString())
                .when().get("/api/masters/1/slots")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @DisplayName("Мастер не оказывает услугу — 400")
    void masterWithoutServiceIsRejected() {
        given()
                .queryParam("serviceId", 8)
                .queryParam("date", TestConfig.nextMonday().toString())
                .when().get("/api/masters/1/slots")
                .then()
                .statusCode(400)
                .body("message", containsString("не оказывает"));
    }

    @Test
    @DisplayName("Уволенный мастер — 404")
    void inactiveMasterNotFound() {
        given()
                .queryParam("serviceId", 5)
                .queryParam("date", TestConfig.nextMonday().toString())
                .when().get("/api/masters/5/slots")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Прошедшая дата — 400")
    void pastDateIsRejected() {
        given()
                .queryParam("serviceId", 1)
                .queryParam("date", "2020-01-01")
                .when().get("/api/masters/1/slots")
                .then()
                .statusCode(400);
    }
}