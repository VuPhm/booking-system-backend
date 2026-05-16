package edu.uaf.booking.controller;

import edu.uaf.booking.dto.ProviderBusinessDto.*;
import edu.uaf.booking.service.ServiceEntityService;
import edu.uaf.booking.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/provider")
@PreAuthorize("hasRole('PROVIDER_ADMIN')") // Chốt chặn bảo vệ cấp lớp
public class ProviderManagementController {

    private final ServiceEntityService serviceEntityService;
    private final SlotService slotService;

    public ProviderManagementController(ServiceEntityService serviceEntityService, SlotService slotService) {
        this.serviceEntityService = serviceEntityService;
        this.slotService = slotService;
    }

    // 1. ENDPOINTS QUẢN LÝ DỊCH VỤ
    @PostMapping("/services")
    public ResponseEntity<String> addService(
            @RequestAttribute("X-Provider-Id") Long providerId,
            @Valid @RequestBody ServiceRequest request) { // Thêm @Valid ở đây
        serviceEntityService.createService(providerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Thêm dịch vụ thành công");
    }

    @GetMapping("/services")
    public ResponseEntity<?> getServices(@RequestAttribute("X-Provider-Id") Long providerId) {
        return ResponseEntity.ok(serviceEntityService.getAllServicesByProvider(providerId));
    }

    // 2. ENDPOINTS QUẢN LÝ KHUNG GIỜ
    @PostMapping("/slots")
    public ResponseEntity<String> addSlot(
            @RequestAttribute("X-Provider-Id") Long providerId,
            @Valid @RequestBody SlotCreateRequest request) { // Thêm @Valid ở đây
        slotService.createSlot(providerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Mở khung giờ thành công");
    }

    @GetMapping("/slots")
    public ResponseEntity<?> getSlots(
            @RequestAttribute("X-Provider-Id") Long providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.getSlotsByDate(providerId, date));
    }
}