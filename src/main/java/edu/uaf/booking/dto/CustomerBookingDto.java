package edu.uaf.booking.dto;

import jakarta.validation.constraints.NotNull;

public final class CustomerBookingDto {
    public record BookingRequest(
        @NotNull(message = "Mã dịch vụ không được để trống")
        Long serviceId,

        @NotNull(message = "Mã khung giờ không được để trống")
        Long slotId,

        String notes
    ) {}
}