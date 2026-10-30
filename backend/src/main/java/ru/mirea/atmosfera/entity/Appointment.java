package ru.mirea.atmosfera.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_id")
    private Master master;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id")
    private SalonService service;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    // В базе хранится текстом: 'BOOKED', 'CANCELLED', 'COMPLETED'
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Appointment() {
    }

    public Appointment(Client client, Master master, SalonService service,
                       LocalDateTime startAt, LocalDateTime endAt) {
        this.client = client;
        this.master = master;
        this.service = service;
        this.startAt = startAt;
        this.endAt = endAt;
    }


    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }

    public Master getMaster() {
        return master;
    }
    public void setMaster(Master master) {
        this.master = master;
    }

    public SalonService getService() {
        return service;
    }
    public void setService(SalonService service) {
        this.service = service;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }
    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }
    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public AppointmentStatus getStatus() {
        return status;
    }
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}