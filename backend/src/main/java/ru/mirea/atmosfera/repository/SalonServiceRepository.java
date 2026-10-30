package ru.mirea.atmosfera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.atmosfera.entity.SalonService;

public interface SalonServiceRepository extends JpaRepository<SalonService, Long> {
}