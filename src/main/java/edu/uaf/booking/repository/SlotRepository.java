package edu.uaf.booking.repository;

import edu.uaf.booking.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByProviderIdAndDate(Long providerId, LocalDate date);
    List<Slot> findByProviderIdAndDateAndIsAvailableTrue(Long providerId, LocalDate date);
}
