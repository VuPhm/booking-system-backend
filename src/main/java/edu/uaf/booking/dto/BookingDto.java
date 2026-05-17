package edu.uaf.booking.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class BookingDto {
    public record BookingRequest(
        @NotNull Long serviceId,
        @NotNull Long slotId,
        String customerNote
    ) {}

    public record BookingResponse(
        Long bookingId,
        String serviceName,
        String date,         // Trả về chuỗi yyyy-MM-dd từ LocalDate
        String timeWindow,
        String status,
        BigDecimal totalPrice
    ) {}
}