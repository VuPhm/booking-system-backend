package edu.uaf.booking.repository;

import edu.uaf.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByProviderId(Long providerId);
    List<Booking> findByCustomerId(Long customerId);
}
