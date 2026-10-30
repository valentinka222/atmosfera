package ru.mirea.atmosfera.dto;

import ru.mirea.atmosfera.entity.Master;

public record MasterDto(Long id, String fullName, String specialization) {

    public static MasterDto from(Master m) {
        return new MasterDto(m.getId(), m.getFullName(), m.getSpecialization());
    }
}