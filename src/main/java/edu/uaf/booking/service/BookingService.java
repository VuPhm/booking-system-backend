package edu.uaf.booking.service;

import edu.uaf.booking.dto.BookingDto.*;
import edu.uaf.booking.entity.*;
import edu.uaf.booking.enums.BookingStatus;
import edu.uaf.booking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
}