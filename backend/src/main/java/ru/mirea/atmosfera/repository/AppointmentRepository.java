package ru.mirea.atmosfera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mirea.atmosfera.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("""
            select a from Appointment a
            where a.master.id = :masterId
              and a.status = ru.mirea.atmosfera.entity.AppointmentStatus.BOOKED
              and a.startAt < :to
              and a.endAt > :from
            order by a.startAt
            """)
    List<Appointment> findBookedInPeriod(@Param("masterId") Long masterId,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);

    Optional<Appointment> findByCancelToken(UUID cancelToken);
}