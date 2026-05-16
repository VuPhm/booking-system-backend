package edu.uaf.booking.service;

import edu.uaf.booking.dto.ProviderBusinessDto.ServiceRequest;
import edu.uaf.booking.entity.Provider;
import edu.uaf.booking.entity.ServiceEntity;
import edu.uaf.booking.repository.ProviderRepository;
import edu.uaf.booking.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ServiceEntityService {

    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;

    public ServiceEntityService(ServiceRepository serviceRepository, ProviderRepository providerRepository) {
        this.serviceRepository = serviceRepository;
        this.providerRepository = providerRepository;
    }

    @Transactional
    public void createService(Long providerId, ServiceRequest request) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Doanh nghiệp không tồn tại"));

        ServiceEntity service = new ServiceEntity();
        service.setProvider(provider);
        service.setName(request.name());
        service.setDescription(request.description());
        service.setPrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
        service.setActive(true);

        serviceRepository.save(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceEntity> getAllServicesByProvider(Long providerId) {
        return serviceRepository.findByProviderIdAndIsActiveTrue(providerId);
    }
}