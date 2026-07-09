package com.example.hotel_management.guest.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

import com.example.hotel_management.guest.dto.GuestRequest.GuestDto;

public final class GuestResponse {

    private GuestResponse() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Creation {
        private String voucherNumber;
        private Long roomId;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private List<GuestDetail> guests;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryDetail {
        private Long id;
        private String firstname;
        private String lastname;
        private String voucherNumber;
        private String roomNumber;
        private String hotelName;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestDetail {
        private Long id;
        private String firstname;
        private String lastname;
    }
}