package edu.uaf.booking.dto;

import edu.uaf.booking.enums.Role;

public final class AuthDto {
    public record LoginRequest(String email, String password) {}
    
    public record RegisterRequest(
        String email, 
        String password, 
        String fullName, 
        String phone, 
        Role role, 
        Long providerId // Bắt buộc truyền nếu role là PROVIDER_ADMIN
    ) {}
    
    public record AuthResponse(String accessToken, String email, String role, Long providerId) {}
}