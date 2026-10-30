package ru.mirea.atmosfera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.atmosfera.entity.Master;

public interface MasterRepository extends JpaRepository<Master, Long> {
}