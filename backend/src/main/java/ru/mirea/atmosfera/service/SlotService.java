package ru.mirea.atmosfera.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.atmosfera.dto.TimeSlotDto;
import ru.mirea.atmosfera.entity.Appointment;
import ru.mirea.atmosfera.entity.Master;
import ru.mirea.atmosfera.entity.SalonService;
import ru.mirea.atmosfera.entity.WorkSchedule;
import ru.mirea.atmosfera.exception.BadRequestException;
import ru.mirea.atmosfera.exception.NotFoundException;
import ru.mirea.atmosfera.repository.AppointmentRepository;
import ru.mirea.atmosfera.repository.MasterRepository;
import ru.mirea.atmosfera.repository.SalonServiceRepository;
import ru.mirea.atmosfera.repository.WorkScheduleRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SlotService {

    // Шаг сетки: слоты начинаются каждые 30 минут
    public static final int SLOT_STEP_MINUTES = 30;

    private final MasterRepository masterRepository;
    private final SalonServiceRepository serviceRepository;
    private final WorkScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    public SlotService(MasterRepository masterRepository,
                       SalonServiceRepository serviceRepository,
                       WorkScheduleRepository scheduleRepository,
                       AppointmentRepository appointmentRepository,
                       Clock clock) {
        this.masterRepository = masterRepository;
        this.serviceRepository = serviceRepository;
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    public List<TimeSlotDto> getFreeSlots(Long masterId, Long serviceId, LocalDate date) {
        // 1. Мастер существует и работает
        Master master = masterRepository.findById(masterId)
                .filter(Master::isActive)
                .orElseThrow(() -> new NotFoundException("Мастер с id=" + masterId + " не найден"));

        // 2. Услуга существует
        SalonService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга с id=" + serviceId + " не найдена"));

        // 3. Мастер оказывает эту услугу
        boolean offers = master.getServices().stream()
                .anyMatch(s -> s.getId().equals(serviceId));
        if (!offers) {
            throw new BadRequestException("Мастер " + master.getFullName()
                    + " не оказывает услугу «" + service.getName() + "»");
        }

        // 4. Дата не в прошлом
        LocalDateTime now = LocalDateTime.now(clock);
        if (date.isBefore(now.toLocalDate())) {
            throw new BadRequestException("Нельзя записаться на прошедшую дату");
        }

        // 5. График на этот день недели. Нет строки — выходной, слотов нет
        Optional<WorkSchedule> schedule = scheduleRepository
                .findByMasterIdAndDayOfWeek(masterId, date.getDayOfWeek().getValue());
        if (schedule.isEmpty()) {
            return List.of();
        }

        LocalDateTime workStart = date.atTime(schedule.get().getStartTime());
        LocalDateTime workEnd = date.atTime(schedule.get().getEndTime());

        // 6. Уже занятые промежутки
        List<Appointment> booked = appointmentRepository
                .findBookedInPeriod(masterId, workStart, workEnd);

        // 7. Перебираем начала с шагом 30 минут, пока услуга помещается до конца дня
        Duration duration = Duration.ofMinutes(service.getDurationMin());
        List<TimeSlotDto> result = new ArrayList<>();

        for (LocalDateTime start = workStart;
             !start.plus(duration).isAfter(workEnd);
             start = start.plusMinutes(SLOT_STEP_MINUTES)) {

            LocalDateTime end = start.plus(duration);

            if (!start.isAfter(now)) {
                continue;               // это время сегодня уже прошло
            }
            if (overlapsAny(start, end, booked)) {
                continue;               // пересекается с чужой записью
            }
            result.add(new TimeSlotDto(start.toLocalTime(), end.toLocalTime()));
        }
        return result;
    }

    private boolean overlapsAny(LocalDateTime start, LocalDateTime end, List<Appointment> booked) {
        return booked.stream().anyMatch(a ->
                a.getStartAt().isBefore(end) && a.getEndAt().isAfter(start));
    }
}