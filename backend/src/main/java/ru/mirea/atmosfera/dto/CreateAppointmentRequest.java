package ru.mirea.atmosfera.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CreateAppointmentRequest(

        @NotNull(message = "Не указан мастер")
        Long masterId,

        @NotNull(message = "Не указана услуга")
        Long serviceId,

        @NotNull(message = "Не указано время записи")
        LocalDateTime startAt,

        @NotBlank(message = "Укажите имя")
        @Size(max = 100, message = "Имя слишком длинное")
        String clientName,

        @NotBlank(message = "Укажите телефон")
        @Pattern(regexp = "^\\+?[0-9 ()-]{7,20}$", message = "Неверный формат телефона")
        String clientPhone,

        @NotBlank(message = "Укажите email")
        @Email(message = "Неверный формат email")
        @Size(max = 100, message = "Email слишком длинный")
        String clientEmail
) {
}