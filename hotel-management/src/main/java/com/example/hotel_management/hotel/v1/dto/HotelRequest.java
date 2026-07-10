package com.example.hotel_management.hotel.v1.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class HotelRequest {

    private HotelRequest() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Creation {
        @NotBlank(message = "Hotel name is required")
        private String name;
        private String location;
        private String contactInfo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        @NotBlank(message = "Hotel name is required")
        private String name;
        private String location;
        private String contactInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Search {
        private String name;
        private String location;

        @Min(value = 0, message = "Page number cannot be less than 0")
        private Integer page = 0;
        
        @Min(value = 1, message = "Page size must be at least 1")
        private Integer size = 10;

        private String sortBy = "id";
        private String direction = "asc";
    }
}