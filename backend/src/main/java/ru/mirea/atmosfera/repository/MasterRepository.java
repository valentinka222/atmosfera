package ru.mirea.atmosfera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mirea.atmosfera.entity.Master;

import java.util.List;

public interface MasterRepository extends JpaRepository<Master, Long> {
    @Query("""
            select m from Master m
            join m.services s
            where s.id = :serviceId and m.active = true
            order by m.fullName
            """)
    List<Master> findActiveByServiceId(@Param("serviceId") Long serviceId);
}