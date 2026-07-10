package com.example.hotel_management.guest;

import java.util.List;
import org.springframework.stereotype.Component;
import com.example.hotel_management.guest.v1.dto.GuestResponse;

@Component
public class GuestMapper {

    public GuestResponse.QueryDetail toQueryDetail(Guest guest) {
        if (guest == null) {
            return null;
        }
        return GuestResponse.QueryDetail.builder()
                .id(guest.getId())
                .firstname(guest.getFirstname())
                .lastname(guest.getLastname())
                .voucherNumber(guest.getVoucherNumber())
                .roomNumber(guest.getRoom() != null ? guest.getRoom().getRoomNumber() : null)
                .hotelName((guest.getRoom() != null && guest.getRoom().getHotel() != null)
                                ? guest.getRoom().getHotel().getName()
                                : null)
                .checkInDate(guest.getCheckInDate())
                .checkOutDate(guest.getCheckOutDate())
                .build();
    }

    public GuestResponse.GuestDetail toGuestDetail(Guest guest) {
        if (guest == null) {
            return null;
        }
        return GuestResponse.GuestDetail.builder()
                .id(guest.getId())
                .firstname(guest.getFirstname())
                .lastname(guest.getLastname())
                .build();
    }

    public GuestResponse.Creation toCreationResponse(List<Guest> guests) {
        if (guests == null || guests.isEmpty()) {
            return null;
        }

        Guest firstGuest = guests.get(0);

        List<GuestResponse.GuestDetail> guestDetails = guests.stream()
                .map(this::toGuestDetail)
                .toList();

        return GuestResponse.Creation.builder()
                .voucherNumber(firstGuest.getVoucherNumber())
                .checkInDate(firstGuest.getCheckInDate())
                .checkOutDate(firstGuest.getCheckOutDate())
                .roomId(firstGuest.getRoom() != null ? firstGuest.getRoom().getId() : null)
                .guests(guestDetails)
                .build();
    }
}
