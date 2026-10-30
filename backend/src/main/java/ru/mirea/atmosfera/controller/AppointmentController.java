package ru.mirea.atmosfera.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.mirea.atmosfera.dto.AppointmentDto;
import ru.mirea.atmosfera.dto.CreateAppointmentRequest;
import ru.mirea.atmosfera.service.BookingService;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final BookingService bookingService;

    public AppointmentController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // POST /api/appointments — создать запись
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDto create(@Valid @RequestBody CreateAppointmentRequest request) {
        return bookingService.create(request);
    }

    // GET /api/appointments/{token} — посмотреть свою запись
    @GetMapping("/{token}")
    public AppointmentDto get(@PathVariable UUID token) {
        return bookingService.getByToken(token);
    }

    // POST /api/appointments/{token}/cancel — отменить запись
    @PostMapping("/{token}/cancel")
    public AppointmentDto cancel(@PathVariable UUID token) {
        return bookingService.cancel(token);
    }
}