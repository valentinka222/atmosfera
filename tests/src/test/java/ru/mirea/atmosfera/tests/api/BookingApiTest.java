package ru.mirea.atmosfera.tests.api;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mirea.atmosfera.tests.MailpitClient;
import ru.mirea.atmosfera.tests.TestConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("API")
@Feature("Запись и отмена")
class BookingApiTest extends ApiTestBase {

    private final MailpitClient mailpit = new MailpitClient(TestConfig.MAILPIT_URL);

    // Каждый тест записывается на своё время или к своему мастеру,
    // чтобы тесты не мешали друг другу

    @Test
    @DisplayName("Запись создаётся, клиенту приходит письмо-подтверждение")
    void bookingIsCreatedAndConfirmed() throws Exception {
        String email = TestConfig.uniqueEmail();
        LocalDateTime startAt = TestConfig.nextMonday().atTime(15, 0);

        String token = Allure.step("Записаться к Анне на мужскую стрижку в 15:00", () ->
                given()
                        .contentType(ContentType.JSON)
                        .body(booking(1, 2, startAt, email))
                        .when().post("/api/appointments")
                        .then()
                        .statusCode(201)
                        .body("status", equalTo("BOOKED"))
                        .body("masterName", equalTo("Анна Смирнова"))
                        .body("cancelToken", notNullValue())
                        .extract().path("cancelToken"));

        Allure.step("Запись доступна по токену", () ->
                given()
                        .when().get("/api/appointments/{token}", token)
                        .then()
                        .statusCode(200)
                        .body("status", equalTo("BOOKED")));

        Allure.step("Письмо-подтверждение пришло в MailPit", () ->
                assertThat(mailpit.waitForMessagesTo(email, 1)).isEqualTo(1));
    }

    @Test
    @DisplayName("Повторная запись на занятое время — 409")
    void doubleBookingIsRejected() {
        Map<String, Object> body = booking(1, 2,
                TestConfig.nextMonday().atTime(16, 0), TestConfig.uniqueEmail());

        Allure.step("Первая запись успешна", () ->
                given().contentType(ContentType.JSON).body(body)
                        .when().post("/api/appointments")
                        .then().statusCode(201));

        Allure.step("Вторая запись на то же время отклонена", () ->
                given().contentType(ContentType.JSON).body(body)
                        .when().post("/api/appointments")
                        .then()
                        .statusCode(409)
                        .body("message", containsString("недоступно")));
    }

    @Test
    @DisplayName("Невалидные данные клиента — 400 с понятными сообщениями")
    void invalidClientDataIsRejected() {
        Map<String, Object> body = Map.of(
                "masterId", 1,
                "serviceId", 1,
                "startAt", TestConfig.nextMonday().atTime(17, 0).toString(),
                "clientName", "",
                "clientPhone", "+7 900 000-00-00",
                "clientEmail", "not-an-email");

        given()
                .contentType(ContentType.JSON).body(body)
                .when().post("/api/appointments")
                .then()
                .statusCode(400)
                .body("message", allOf(
                        containsString("Укажите имя"),
                        containsString("Неверный формат email")));
    }

    @Test
    @DisplayName("Отмена освобождает время, повторная отмена — 409")
    void cancellationFreesSlot() throws Exception {
        String email = TestConfig.uniqueEmail();

        String token = Allure.step("Записаться к Ольге на маникюр в 10:00", () ->
                given()
                        .contentType(ContentType.JSON)
                        .body(booking(3, 5, TestConfig.nextMonday().atTime(10, 0), email))
                        .when().post("/api/appointments")
                        .then().statusCode(201)
                        .extract().path("cancelToken"));

        Allure.step("Отменить запись", () ->
                given()
                        .when().post("/api/appointments/{token}/cancel", token)
                        .then()
                        .statusCode(200)
                        .body("status", equalTo("CANCELLED")));

        Allure.step("Время 10:00 снова свободно", () -> {
            List<String> starts = given()
                    .queryParam("serviceId", 5)
                    .queryParam("date", TestConfig.nextMonday().toString())
                    .when().get("/api/masters/3/slots")
                    .then().statusCode(200)
                    .extract().jsonPath().getList("start");
            assertThat(starts).contains("10:00:00");
        });

        Allure.step("Повторная отмена отклонена", () ->
                given()
                        .when().post("/api/appointments/{token}/cancel", token)
                        .then().statusCode(409));

        Allure.step("Пришло два письма: подтверждение и отмена", () ->
                assertThat(mailpit.waitForMessagesTo(email, 2)).isEqualTo(2));
    }

    @Test
    @DisplayName("Отмена методом GET запрещена — 405")
    void cancelByGetIsNotAllowed() {
        given()
                .when().get("/api/appointments/{token}/cancel", UUID.randomUUID())
                .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("Одновременная запись на одно время: успешна ровно одна")
    void concurrentBookingAllowsOnlyOne() throws Exception {
        LocalDateTime startAt = TestConfig.nextMonday().atTime(13, 0);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        // Оба потока ждут общий сигнал и отправляют запросы одновременно
        Callable<Integer> attempt = () -> {
            startSignal.await();
            return given()
                    .noFilters()
                    .contentType(ContentType.JSON)
                    .body(booking(4, 9, startAt, TestConfig.uniqueEmail()))
                    .when().post("/api/appointments")
                    .statusCode();
        };

        Future<Integer> first = pool.submit(attempt);
        Future<Integer> second = pool.submit(attempt);
        startSignal.countDown();

        List<Integer> statuses = List.of(
                first.get(10, TimeUnit.SECONDS),
                second.get(10, TimeUnit.SECONDS));
        pool.shutdown();

        Allure.addAttachment("Коды ответов", statuses.toString());
        assertThat(statuses).containsExactlyInAnyOrder(201, 409);
    }
}