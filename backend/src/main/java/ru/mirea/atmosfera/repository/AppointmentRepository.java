package ru.mirea.atmosfera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.atmosfera.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}