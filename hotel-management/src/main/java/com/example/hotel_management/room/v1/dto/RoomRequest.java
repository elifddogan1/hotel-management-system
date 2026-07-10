package com.example.hotel_management.room.v1.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class RoomRequest {

    private RoomRequest() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Creation {
        @NotNull(message = "Hotel ID cannot be null")
        private Long hotelId;

        @NotBlank(message = "Room number cannot be blank")
        private String roomNumber;

        @NotBlank(message = "Room type cannot be blank")
        private String roomType;

        @NotNull(message = "Max capacity cannot be null")
        @Min(value = 1, message = "Max capacity must be at least 1")
        private Integer maxCapacity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Search {
        private String roomNumber;
        private String roomType;
        private Long hotelId;

        @Min(value = 0, message = "Page number cannot be less than 0")
        private Integer page = 0;
        
        @Min(value = 1, message = "Page size must be at least 1")
        private Integer size = 10;

        private String sortBy = "id";
        private String direction = "asc";
    }
}