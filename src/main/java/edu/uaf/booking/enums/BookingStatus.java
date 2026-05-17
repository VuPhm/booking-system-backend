package edu.uaf.booking.enums;

public enum BookingStatus {
    PENDING,    // Chờ xử lý
    CONFIRMED,  // Đã xác nhận lịch
    COMPLETED,  // Đã hoàn thành liệu trình
    REJECTED,   // Đã từ chối lịch (không duyệt)
    CANCELLED   // Đã hủy lịch
}