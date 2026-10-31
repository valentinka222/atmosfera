package ru.mirea.atmosfera.tests;

import static io.restassured.RestAssured.given;

public class MailpitClient {

    private final String baseUrl;

    public MailpitClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** Сколько писем пришло на адрес */
    public int countMessagesTo(String email) {
        return given()
                .baseUri(baseUrl)
                .queryParam("query", "to:" + email)
                .when()
                .get("/api/v1/search")
                .then()
                .statusCode(200)
                .extract()
                .path("messages.size()");
    }

    /** Подождать до 10 секунд, пока придёт нужное число писем */
    public int waitForMessagesTo(String email, int expected) throws InterruptedException {
        int count = 0;
        for (int attempt = 0; attempt < 20; attempt++) {
            count = countMessagesTo(email);
            if (count >= expected) {
                return count;
            }
            Thread.sleep(500);
        }
        return count;
    }
}