package edu.uaf.booking.controller;

import edu.uaf.booking.dto.BookingDto.BookingRequest;
import edu.uaf.booking.dto.BookingDto.BookingResponse;
import edu.uaf.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/customer/bookings")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerBookingController {

    private final BookingService bookingService;

    public CustomerBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> placeBooking(Principal principal, @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getCustomerBookings(Principal principal) {
        return ResponseEntity.ok(bookingService.getBookingsByCustomer(principal.getName()));
    }
}