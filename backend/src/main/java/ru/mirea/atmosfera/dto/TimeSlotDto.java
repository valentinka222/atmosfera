package ru.mirea.atmosfera.dto;

import java.time.LocalTime;

public record TimeSlotDto(LocalTime start, LocalTime end) {
}