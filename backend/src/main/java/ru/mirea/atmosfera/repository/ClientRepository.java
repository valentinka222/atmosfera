package ru.mirea.atmosfera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.atmosfera.entity.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findFirstByEmailIgnoreCase(String email);
}