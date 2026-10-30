package ru.mirea.atmosfera.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.mirea.atmosfera.dto.MasterDto;
import ru.mirea.atmosfera.dto.ServiceDto;
import ru.mirea.atmosfera.dto.TimeSlotDto;
import ru.mirea.atmosfera.service.CatalogService;
import ru.mirea.atmosfera.service.SlotService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;
    private final SlotService slotService;

    public CatalogController(CatalogService catalogService, SlotService slotService) {
        this.catalogService = catalogService;
        this.slotService = slotService;
    }

    // GET /api/services — все услуги
    @GetMapping("/services")
    public List<ServiceDto> getServices() {
        return catalogService.getAllServices();
    }

    // GET /api/services/3/masters — мастера, которые делают услугу 3
    @GetMapping("/services/{serviceId}/masters")
    public List<MasterDto> getMastersForService(@PathVariable Long serviceId) {
        return catalogService.getMastersForService(serviceId);
    }

    // GET /api/masters/1/slots?serviceId=1&date=2026-11-02 — свободное время
    @GetMapping("/masters/{masterId}/slots")
    public List<TimeSlotDto> getFreeSlots(
            @PathVariable Long masterId,
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return slotService.getFreeSlots(masterId, serviceId, date);
    }
}