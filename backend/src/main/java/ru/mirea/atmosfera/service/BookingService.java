package ru.mirea.atmosfera.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.atmosfera.dto.AppointmentDto;
import ru.mirea.atmosfera.dto.CreateAppointmentRequest;
import ru.mirea.atmosfera.dto.TimeSlotDto;
import ru.mirea.atmosfera.entity.*;
import ru.mirea.atmosfera.exception.ConflictException;
import ru.mirea.atmosfera.exception.NotFoundException;
import ru.mirea.atmosfera.repository.AppointmentRepository;
import ru.mirea.atmosfera.repository.ClientRepository;
import ru.mirea.atmosfera.repository.MasterRepository;
import ru.mirea.atmosfera.repository.SalonServiceRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    // Отменить запись можно не позднее чем за 2 часа до начала
    public static final Duration CANCEL_DEADLINE = Duration.ofHours(2);

    private final MasterRepository masterRepository;
    private final SalonServiceRepository serviceRepository;
    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final SlotService slotService;
    private final NotificationService notificationService;
    private final Clock clock;

    public BookingService(MasterRepository masterRepository,
                          SalonServiceRepository serviceRepository,
                          ClientRepository clientRepository,
                          AppointmentRepository appointmentRepository,
                          SlotService slotService,
                          NotificationService notificationService,
                          Clock clock) {
        this.masterRepository = masterRepository;
        this.serviceRepository = serviceRepository;
        this.clientRepository = clientRepository;
        this.appointmentRepository = appointmentRepository;
        this.slotService = slotService;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Transactional
    public AppointmentDto create(CreateAppointmentRequest request) {
        Master master = masterRepository.findById(request.masterId())
                .filter(Master::isActive)
                .orElseThrow(() -> new NotFoundException("Мастер с id=" + request.masterId() + " не найден"));

        SalonService service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new NotFoundException("Услуга с id=" + request.serviceId() + " не найдена"));

        LocalDateTime startAt = request.startAt().truncatedTo(ChronoUnit.MINUTES);
        LocalTime startTime = startAt.toLocalTime();

        // Используем уже готовый расчёт слотов: он проверяет, что мастер делает
        // эту услугу, дата не в прошлом, время в рабочих часах и не занято
        List<TimeSlotDto> freeSlots = slotService.getFreeSlots(
                master.getId(), service.getId(), startAt.toLocalDate());

        boolean available = freeSlots.stream()
                .anyMatch(slot -> slot.start().equals(startTime));
        if (!available) {
            throw new ConflictException("Время " + startTime + " недоступно для записи. Выберите другое время");
        }

        // Клиент: если уже записывался с этим email — используем его, иначе создаём
        String email = request.clientEmail().trim().toLowerCase();
        Client client = clientRepository.findFirstByEmailIgnoreCase(email)
                .orElseGet(() -> clientRepository.save(new Client(
                        request.clientName().trim(),
                        request.clientPhone().trim(),
                        email)));

        Appointment appointment = new Appointment(client, master, service,
                startAt, startAt.plusMinutes(service.getDurationMin()));

        try {
            // saveAndFlush — сохранить и сразу отправить в базу, чтобы
            // ограничение appointment_no_overlap сработало прямо здесь
            appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException e) {
            // Кто-то занял это время между нашей проверкой и сохранением
            throw new ConflictException("Это время только что заняли. Выберите другое время");
        }

        notificationService.sendBookingConfirmation(appointment);
        return AppointmentDto.from(appointment);
    }

    @Transactional(readOnly = true)
    public AppointmentDto getByToken(UUID token) {
        return AppointmentDto.from(findByToken(token));
    }

    @Transactional
    public AppointmentDto cancel(UUID token) {
        Appointment appointment = findByToken(token);

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new ConflictException("Запись уже отменена или завершена");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (now.plus(CANCEL_DEADLINE).isAfter(appointment.getStartAt())) {
            throw new ConflictException("Отменить запись можно не позднее чем за 2 часа до начала");
        }

        // Явный save не нужен: Hibernate сам заметит изменение объекта
        // и обновит строку в базе при завершении транзакции
        appointment.setStatus(AppointmentStatus.CANCELLED);

        notificationService.sendCancellation(appointment);
        return AppointmentDto.from(appointment);
    }

    private Appointment findByToken(UUID token) {
        return appointmentRepository.findByCancelToken(token)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
    }
}