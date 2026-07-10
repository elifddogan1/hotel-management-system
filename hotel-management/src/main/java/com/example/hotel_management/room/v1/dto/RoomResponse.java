package com.example.hotel_management.room.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class RoomResponse {

    private RoomResponse() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Creation {
        private Long id;
        private String roomNumber;
        private String roomType;
        private Integer maxCapacity;
        private Long hotelId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryDetail {
        private Long id;
        private String roomNumber;
        private String roomType;
        private Integer maxCapacity;
        private Long hotelId;
        private String hotelName; 
    }
}