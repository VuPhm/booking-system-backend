package edu.uaf.booking.service;

import edu.uaf.booking.dto.ProviderBusinessDto.SlotCreateRequest;
import edu.uaf.booking.entity.Provider;
import edu.uaf.booking.entity.Slot;
import edu.uaf.booking.repository.ProviderRepository;
import edu.uaf.booking.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final ProviderRepository providerRepository;

    public SlotService(SlotRepository slotRepository, ProviderRepository providerRepository) {
        this.slotRepository = slotRepository;
        this.providerRepository = providerRepository;
    }

    @Transactional
    public void createSlot(Long providerId, SlotCreateRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        // Chốt chặn kiểm tra chồng chéo lịch
        boolean isOverlapped = slotRepository.existsOverlappingSlot(
                providerId, request.date(), request.startTime(), request.endTime());

        if (isOverlapped) {
            throw new IllegalArgumentException("Khung giờ khởi tạo bị trùng lịch với cấu hình đã tồn tại");
        }

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Doanh nghiệp không tồn tại"));

        Slot slot = new Slot();
        slot.setProvider(provider);
        slot.setDate(request.date());
        slot.setStartTime(request.startTime());
        slot.setEndTime(request.endTime());
        slot.setAvailable(true);

        slotRepository.save(slot);
    }

    @Transactional(readOnly = true)
    public List<Slot> getSlotsByDate(Long providerId, LocalDate date) {
        return slotRepository.findByProviderIdAndDate(providerId, date);
    }
}