package com.example.hotel_management.reservation;

import java.util.UUID;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.example.hotel_management.guest.GuestService;
import com.example.hotel_management.room.RoomService;
import com.example.hotel_management.room.Room;
import com.example.hotel_management.guest.v1.dto.GuestRequest;
import com.example.hotel_management.guest.v1.dto.GuestResponse;

@Service
@RequiredArgsConstructor
public class ReservationFacade {

    private final GuestService guestService;
    private final RoomService roomService;
    private final VoucherGenerator voucherGenerator;

    @Transactional
    public GuestResponse.Creation createReservation(GuestRequest.Creation request) {
        if (!request.getCheckInDate().isBefore(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-out tarihi check-in tarihinden sonra olmalıdır.");
        }

        // 1. Odayı RoomService üzerinden getir
        Room room = roomService.getRoomEntityById(request.getRoomId());

        // 2. Oda müsaitliğini RoomService üzerinden kontrol et
        Boolean isAvailable = roomService.isRoomAvailable(room, request.getGuests().size(),
                request.getCheckInDate(), request.getCheckOutDate(), null);

        if (!isAvailable) {
            throw new IllegalArgumentException("Seçilen oda bu tarihler arasında müsait değil.");
        }

        // 3. Voucher oluştur ve GuestService'e kaydetmesi için gönder
        String generatedVoucher = voucherGenerator.generateVoucherCode();
        
        return guestService.createGuestsForReservation(room, generatedVoucher, request);
    }

    @Transactional
    public GuestResponse.Creation updateReservation(String voucherNumber, GuestRequest.Creation request) {
        if (!request.getCheckInDate().isBefore(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-out tarihi check-in tarihinden sonra olmalıdır.");
        }

        Room newRoom = roomService.getRoomEntityById(request.getRoomId());

        Boolean isAvailable = roomService.isRoomAvailable(newRoom, request.getGuests().size(),
                request.getCheckInDate(), request.getCheckOutDate(), voucherNumber);

        if (!isAvailable) {
            throw new IllegalArgumentException("Oda bu tarihler arasında müsait değil.");
        }

        // İşlemin geri kalanını GuestService'e devret
        return guestService.updateGuestsForReservation(voucherNumber, newRoom, request);
    }
}
