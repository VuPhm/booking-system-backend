package edu.uaf.booking.repository;

import edu.uaf.booking.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByProviderIdAndIsActiveTrue(Long providerId);
}
