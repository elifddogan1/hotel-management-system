package com.example.hotel_management.guest.v1.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class GuestRequest {

    private GuestRequest() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Creation {
        private Long roomId;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private List<GuestDto> guests;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Search {
        private String firstname;
        private String lastname;
        private String voucherNumber;
        private String roomNumber;
        private String hotelName;

        @Min(value = 0, message = "Page number cannot be less than 0")
        private Integer page = 0;
        
        @Min(value = 1, message = "Page size must be at least 1")
        private Integer size = 10;

        private String sortBy = "id";
        private String direction = "asc";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestDto {
        private String firstname;
        private String lastname;
    }
}