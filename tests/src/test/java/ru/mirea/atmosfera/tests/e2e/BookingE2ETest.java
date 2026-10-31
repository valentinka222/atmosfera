package ru.mirea.atmosfera.tests.e2e;

import com.microsoft.playwright.*;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;
import ru.mirea.atmosfera.tests.TestConfig;

import java.io.ByteArrayInputStream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("E2E")
@Feature("Интерфейс записи")
class BookingE2ETest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        // Браузер без окна (headless) — в контейнере экрана нет
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void openSite() {
        // Новый «чистый» профиль браузера для каждого теста
        context = browser.newContext(new Browser.NewContextOptions()
                .setLocale("ru-RU")
                .setTimezoneId("Europe/Moscow"));
        page = context.newPage();
        page.navigate(TestConfig.BASE_URL);
    }

    @AfterEach
    void attachScreenshot() {
        // Снимок страницы в конце каждого теста — вложением в отчёт
        byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        Allure.addAttachment("Страница в конце теста", "image/png",
                new ByteArrayInputStream(screenshot), "png");
        context.close();
    }

    @Test
    @DisplayName("Запись через сайт и отмена по ссылке")
    void bookAndCancelThroughUi() {
        String day = TestConfig.nextMonday().toString();

        Allure.step("Выбрать услугу «Чистка лица»", () ->
                page.getByTestId("service-8").click());
        Allure.step("Выбрать мастера Марию Петрову", () ->
                page.getByTestId("master-4").click());
        Allure.step("Выбрать понедельник и время 10:00", () -> {
            page.getByTestId("day-" + day).click();
            page.getByTestId("slot-10:00").click();
        });
        Allure.step("Заполнить контакты и записаться", () -> {
            page.getByTestId("input-name").fill("Анастасия Тестова");
            page.getByTestId("input-phone").fill("+7 900 123-45-67");
            page.getByTestId("input-email").fill(TestConfig.uniqueEmail());
            page.getByTestId("submit").click();
        });
        Allure.step("Показан экран «Вы записаны»", () -> {
            assertThat(page.getByTestId("done")).isVisible();
            assertThat(page.getByTestId("done")).containsText("Чистка лица");
        });
        Allure.step("Перейти по ссылке отмены и отменить запись", () -> {
            page.getByTestId("cancel-link").click();
            page.getByTestId("cancel-button").click();
            assertThat(page.getByTestId("notice")).containsText("Запись отменена");
        });
    }

    @Test
    @DisplayName("Ошибка валидации показывается под формой")
    void validationErrorIsShown() {
        page.getByTestId("service-1").click();
        page.getByTestId("master-1").click();
        page.getByTestId("day-" + TestConfig.nextMonday()).click();
        page.getByTestId("slot-10:00").click();

        // Отправляем пустую форму
        page.getByTestId("submit").click();

        assertThat(page.getByTestId("form-error")).isVisible();
        assertThat(page.getByTestId("form-error")).containsText("Укажите имя");
    }

    @Test
    @DisplayName("Уволенный мастер не показывается в выборе")
    void inactiveMasterIsNotShown() {
        page.getByTestId("service-5").click();

        assertThat(page.getByTestId("masters")).containsText("Ольга Иванова");
        assertThat(page.getByTestId("masters")).not().containsText("Ирина Соколова");
    }
}