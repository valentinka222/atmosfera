package ru.mirea.atmosfera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.atmosfera.entity.WorkSchedule;

import java.util.Optional;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    Optional<WorkSchedule> findByMasterIdAndDayOfWeek(Long masterId, Integer dayOfWeek);
}