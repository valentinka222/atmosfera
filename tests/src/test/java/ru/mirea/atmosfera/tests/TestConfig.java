package ru.mirea.atmosfera.tests;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

public final class TestConfig {

    // Адреса берутся из переменных окружения. Значения по умолчанию —
    // для запуска с компьютера против поднятой test-среды
    public static final String BASE_URL = env("BASE_URL", "http://localhost:8090");
    public static final String MAILPIT_URL = env("MAILPIT_URL", "http://localhost:8026");

    private TestConfig() {
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /** Ближайший понедельник — на него в тестовых данных стоят записи (V2__test_data.sql) */
    public static LocalDate nextMonday() {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    /** Воскресенье после него — выходной у всех мастеров */
    public static LocalDate nextSunday() {
        return nextMonday().plusDays(6);
    }

    /** Уникальный email, чтобы найти именно письма этого теста */
    public static String uniqueEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }
}