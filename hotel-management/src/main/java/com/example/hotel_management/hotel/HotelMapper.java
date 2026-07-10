package com.example.hotel_management.hotel;

import org.springframework.stereotype.Component;
import com.example.hotel_management.hotel.v1.dto.HotelResponse;

@Component
public class HotelMapper {

    public HotelResponse toResponse(Hotel hotel) {
        if (hotel == null) {
            return null;
        }
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .location(hotel.getLocation())
                .contactInfo(hotel.getContactInfo())
                .build();
    }
}
