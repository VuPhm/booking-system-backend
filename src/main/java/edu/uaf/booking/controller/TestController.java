package edu.uaf.booking.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/provider-dashboard")
    @PreAuthorize("hasRole('PROVIDER_ADMIN')") // Chỉ cho phép chủ cửa hàng truy cập
    public ResponseEntity<?> getProviderDashboardData(HttpServletRequest request) {
        // Lấy mã Tenant đã được bộ lọc JwtAuthenticationFilter giải mã ngầm trước đó
        Object providerId = request.getAttribute("X-Provider-Id");
        
        return ResponseEntity.ok(Map.of(
            "message", "Truy cập vùng quản trị an toàn",
            "extractedProviderId", providerId != null ? providerId : "N/A"
        ));
    }
}