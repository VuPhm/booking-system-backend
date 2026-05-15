package edu.uaf.booking.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ProviderBusinessDto {
    
    public record ServiceRequest(
        @NotBlank(message = "Tên dịch vụ không được để trống")
        @Size(max = 150, message = "Tên dịch vụ không vượt quá 150 ký tự")
        String name,

        String description,

        @NotNull(message = "Giá dịch vụ không được để trống")
        @PositiveOrZero(message = "Giá dịch vụ không được là số âm")
        BigDecimal price,

        @NotNull(message = "Thời lượng không được để trống")
        @Min(value = 1, message = "Thời lượng dịch vụ tối thiểu là 1 phút")
        Integer durationMinutes
    ) {}

    public record SlotCreateRequest(
        @NotNull(message = "Ngày cấu hình không được để trống")
        LocalDate date,

        @NotNull(message = "Giờ bắt đầu không được để trống")
        LocalTime startTime,

        @NotNull(message = "Giờ kết thúc không được để trống")
        LocalTime endTime
    ) {}
}