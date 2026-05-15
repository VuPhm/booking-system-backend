package edu.uaf.booking.service;

import edu.uaf.booking.dto.AuthDto.*;
import edu.uaf.booking.entity.Provider;
import edu.uaf.booking.entity.User;
import edu.uaf.booking.enums.Role;
import edu.uaf.booking.repository.ProviderRepository;
import edu.uaf.booking.repository.UserRepository;
import edu.uaf.booking.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, 
                       ProviderRepository providerRepository,
                       PasswordEncoder passwordEncoder, 
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại trên hệ thống");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password())); // Băm mật khẩu BCrypt
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRole(request.role());

        // Nếu là Quản trị viên cửa hàng, bắt buộc phải kiểm tra và gắn ProviderId
        if (request.role() == Role.PROVIDER_ADMIN) {
            if (request.providerId() == null) {
                throw new IllegalArgumentException("Tài khoản doanh nghiệp phải đi kèm mã Provider ID");
            }
            Provider provider = providerRepository.findById(request.providerId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin đối tác hợp lệ"));
            user.setProvider(provider);
        }

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Tài khoản hoặc mật khẩu không chính xác");
        }

        Long providerId = user.getProvider() != null ? user.getProvider().getId() : null;
        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name(), providerId);

        return new AuthResponse(token, user.getEmail(), user.getRole().name(), providerId);
    }
}