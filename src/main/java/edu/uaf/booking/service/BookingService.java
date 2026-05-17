package edu.uaf.booking.service;

import edu.uaf.booking.dto.BookingDto.BookingRequest;
import edu.uaf.booking.dto.BookingDto.BookingResponse;
import edu.uaf.booking.entity.*;
import edu.uaf.booking.enums.BookingStatus;
import edu.uaf.booking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;

    public BookingService(BookingRepository bookingRepository, ServiceRepository serviceRepository,
                          SlotRepository slotRepository, UserRepository userRepository,
                          ProviderRepository providerRepository) {
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
    }

    // =========================================================================
    // TASK 4.4: LOGIC ĐẶT LỊCH (CUSTOMER)
    // =========================================================================
    @Transactional
    public BookingResponse createBooking(String customerEmail, BookingRequest request) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new IllegalArgumentException("Dịch vụ không tồn tại"));

        Slot slot = slotRepository.findById(request.slotId())
                .orElseThrow(() -> new IllegalArgumentException("Khung giờ không tồn tại"));

        if (!slot.isAvailable()) {
            throw new IllegalArgumentException("Khung giờ này đã bị đóng hoặc đã có người đặt");
        }

        slot.setAvailable(false);
        slotRepository.save(slot);

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setProvider(service.getProvider());
        booking.setService(service);
        booking.setSlot(slot);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(service.getPrice());
        booking.setCustomerNote(request.customerNote());
        booking.setCreatedAt(ZonedDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        String timeWindow = slot.getStartTime().toString() + " - " + slot.getEndTime().toString();

        return new BookingResponse(
                savedBooking.getId(),
                service.getName(),
                slot.getDate().toString(),
                timeWindow,
                savedBooking.getStatus().name(),
                savedBooking.getTotalPrice()
        );
    }

    // =========================================================================
    // TASK 4.5: LOGIC DUYỆT LỊCH ĐẶT (PROVIDER_ADMIN)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByProvider(String providerEmail) {
        User user = userRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản đối tác không tồn tại"));

        Provider provider = providerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản này chưa được cấu hình hồ sơ Đối tác"));

        return bookingRepository.findByProviderId(provider.getId()).stream()
                .map(b -> new BookingResponse(
                        b.getId(),
                        b.getService().getName(),
                        b.getSlot().getDate().toString(),
                        b.getSlot().getStartTime().toString() + " - " + b.getSlot().getEndTime().toString(),
                        b.getStatus().name(),
                        b.getTotalPrice()
                )).toList();
    }

    @Transactional
    public BookingResponse updateBookingStatus(String providerEmail, Long bookingId, String statusStr) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi đặt lịch"));

        if (booking.getProvider() == null || booking.getProvider().getUser() == null) {
            throw new IllegalStateException("Hóa đơn đặt lịch bị khuyết thiếu thông tin tài khoản đối tác hệ thống");
        }

        String attachedProviderEmail = booking.getProvider().getUser().getEmail();

        if (!attachedProviderEmail.equals(providerEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền xử lý hóa đơn này");
        }

        BookingStatus newStatus = BookingStatus.valueOf(statusStr.toUpperCase());
        booking.setStatus(newStatus);

        if (newStatus == BookingStatus.REJECTED) {
            Slot slot = booking.getSlot();
            slot.setAvailable(true);
            slotRepository.save(slot);
        }

        Booking updatedBooking = bookingRepository.save(booking);

        return new BookingResponse(
                updatedBooking.getId(),
                updatedBooking.getService().getName(),
                updatedBooking.getSlot().getDate().toString(),
                updatedBooking.getSlot().getStartTime().toString() + " - " + updatedBooking.getSlot().getEndTime().toString(),
                updatedBooking.getStatus().name(),
                updatedBooking.getTotalPrice()
        );
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByCustomer(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản khách hàng không tồn tại"));

        return bookingRepository.findByCustomerId(customer.getId()).stream()
                .map(b -> new BookingResponse(
                        b.getId(),
                        b.getService().getName(),
                        b.getSlot().getDate().toString(),
                        b.getSlot().getStartTime().toString() + " - " + b.getSlot().getEndTime().toString(),
                        b.getStatus().name(),
                        b.getTotalPrice()
                )).toList();
    }

    @Transactional
    public BookingResponse cancelBooking(String customerEmail, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi đặt lịch"));

        // Chốt chặn 1: Xác thực quyền sở hữu khách hàng
        if (!booking.getCustomer().getEmail().equals(customerEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền thao tác trên hóa đơn này");
        }

        // Chốt chặn 2: Kiểm tra điều kiện trạng thái ban đầu (Phải là PENDING)
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Không thể hủy lịch trình đã được xử lý hoặc đã hủy trước đó");
        }

        // Chốt chặn 3: Ràng buộc cửa sổ thời gian (Tối thiểu 2 tiếng trước giờ hẹn)
        Slot slot = booking.getSlot();
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getDate(), slot.getStartTime());
        // Đồng bộ về múi giờ hệ thống (Khuyên dùng Asia/Ho_Chi_Minh cho môi trường Việt Nam)
        ZonedDateTime slotTargetTime = slotDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime currentTime = ZonedDateTime.now();

        if (currentTime.plusHours(2).isAfter(slotTargetTime)) {
            throw new IllegalArgumentException("Vi phạm quy định vận hành: Chỉ được phép hủy lịch hẹn trước giờ bắt đầu tối thiểu 2 tiếng");
        }

        // Đủ điều kiện -> Tiến hành cập nhật
        booking.setStatus(BookingStatus.CANCELLED);

        slot.setAvailable(true); // Nhả ca trống
        slotRepository.save(slot);

        Booking updatedBooking = bookingRepository.save(booking);

        return new BookingResponse(
                updatedBooking.getId(),
                updatedBooking.getService().getName(),
                updatedBooking.getSlot().getDate().toString(),
                updatedBooking.getSlot().getStartTime().toString() + " - " + updatedBooking.getSlot().getEndTime().toString(),
                updatedBooking.getStatus().name(),
                updatedBooking.getTotalPrice()
        );
    }
}