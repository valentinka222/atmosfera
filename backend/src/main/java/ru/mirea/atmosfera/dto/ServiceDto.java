package ru.mirea.atmosfera.dto;

import ru.mirea.atmosfera.entity.SalonService;
import java.math.BigDecimal;

public record ServiceDto(Long id, String name, String category,
                         Integer durationMin, BigDecimal price) {

    public static ServiceDto from(SalonService s) {
        return new ServiceDto(s.getId(), s.getName(), s.getCategory(),
                s.getDurationMin(), s.getPrice());
    }
}