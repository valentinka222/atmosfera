package ru.mirea.atmosfera.dto;

import ru.mirea.atmosfera.entity.Appointment;
import ru.mirea.atmosfera.entity.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentDto(Long id,
                             String masterName,
                             String serviceName,
                             BigDecimal price,
                             LocalDateTime startAt,
                             LocalDateTime endAt,
                             AppointmentStatus status,
                             UUID cancelToken) {

    public static AppointmentDto from(Appointment a) {
        return new AppointmentDto(
                a.getId(),
                a.getMaster().getFullName(),
                a.getService().getName(),
                a.getService().getPrice(),
                a.getStartAt(),
                a.getEndAt(),
                a.getStatus(),
                a.getCancelToken());
    }
}