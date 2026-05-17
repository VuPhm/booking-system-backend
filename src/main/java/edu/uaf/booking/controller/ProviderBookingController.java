package edu.uaf.booking.controller;

import edu.uaf.booking.dto.BookingDto.BookingResponse;
import edu.uaf.booking.dto.BookingDto.ProviderDashboardStats;
import edu.uaf.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/provider/bookings")
@PreAuthorize("hasRole('PROVIDER_ADMIN')")
public class ProviderBookingController {

    private final BookingService bookingService;

    public ProviderBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getProviderBookings(Principal principal) {
        return ResponseEntity.ok(bookingService.getBookingsByProvider(principal.getName()));
    }

    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<BookingResponse> updateStatus(
            Principal principal,
            @PathVariable Long bookingId,
            @RequestParam String status) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(principal.getName(), bookingId, status));
    }
    // Bổ sung Endpoint thống kê số liệu Dashboard
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ProviderDashboardStats> getDashboardStats(Principal principal) {
        return ResponseEntity.ok(bookingService.getProviderDashboardStats(principal.getName()));
    }

}