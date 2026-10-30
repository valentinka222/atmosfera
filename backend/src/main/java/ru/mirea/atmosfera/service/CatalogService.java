package ru.mirea.atmosfera.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.atmosfera.dto.MasterDto;
import ru.mirea.atmosfera.dto.ServiceDto;
import ru.mirea.atmosfera.exception.NotFoundException;
import ru.mirea.atmosfera.repository.MasterRepository;
import ru.mirea.atmosfera.repository.SalonServiceRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final SalonServiceRepository serviceRepository;
    private final MasterRepository masterRepository;

    public CatalogService(SalonServiceRepository serviceRepository,
                          MasterRepository masterRepository) {
        this.serviceRepository = serviceRepository;
        this.masterRepository = masterRepository;
    }

    public List<ServiceDto> getAllServices() {
        return serviceRepository.findAllByOrderByCategoryAscNameAsc().stream()
                .map(ServiceDto::from)
                .toList();
    }

    public List<MasterDto> getMastersForService(Long serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            throw new NotFoundException("Услуга с id=" + serviceId + " не найдена");
        }
        return masterRepository.findActiveByServiceId(serviceId).stream()
                .map(MasterDto::from)
                .toList();
    }
}