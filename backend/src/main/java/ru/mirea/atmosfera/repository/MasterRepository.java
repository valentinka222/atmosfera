package ru.mirea.atmosfera.repository;
import ru.mirea.atmosfera.entity.Master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface MasterRepository extends JpaRepository<Master, Long> {
    @Query("""
            select m from Master m
            join m.services s
            where s.id = :serviceId and m.active = true
            order by m.fullName
            """)
    List<Master> findActiveByServiceId(@Param("serviceId") Long serviceId);
    // Найти мастера и заблокировать его строку до конца транзакции:
    // параллельная запись к тому же мастеру дождётся окончания текущей
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Master m where m.id = :id")
    Optional<Master> findByIdForUpdate(@Param("id") Long id);
}