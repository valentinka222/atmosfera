package ru.mirea.atmosfera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.atmosfera.entity.SalonService;

import java.util.List;

public interface SalonServiceRepository extends JpaRepository<SalonService, Long> {
    List<SalonService> findAllByOrderByCategoryAscNameAsc();
}