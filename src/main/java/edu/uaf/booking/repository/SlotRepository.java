package edu.uaf.booking.repository;

import edu.uaf.booking.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByProviderIdAndDate(Long providerId, LocalDate date);
    List<Slot> findByProviderIdAndDateAndIsAvailableTrue(Long providerId, LocalDate date);
    @Query("SELECT COUNT(s) > 0 FROM Slot s WHERE s.provider.id = :providerId " +
            "AND s.date = :date " +
            "AND s.startTime < :endTime AND s.endTime > :startTime")
    boolean existsOverlappingSlot(@Param("providerId") Long providerId,
                                  @Param("date") LocalDate date,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime);
}
