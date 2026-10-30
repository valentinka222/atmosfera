package ru.mirea.atmosfera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.atmosfera.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
}