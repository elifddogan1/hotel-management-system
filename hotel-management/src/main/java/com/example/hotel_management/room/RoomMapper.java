package com.example.hotel_management.room;

import org.springframework.stereotype.Component;
import com.example.hotel_management.room.v1.dto.RoomResponse;

@Component
public class RoomMapper {

    public RoomResponse.QueryDetail toQueryDetail(Room room) {
        if (room == null) {
            return null;
        }
        return RoomResponse.QueryDetail.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .maxCapacity(room.getMaxCapacity())
                .hotelId(room.getHotel() != null ? room.getHotel().getId() : null)
                .hotelName(room.getHotel() != null ? room.getHotel().getName() : null)
                .build();
    }

    public RoomResponse.Creation toCreation(Room room) {
        if (room == null) {
            return null;
        }
        return RoomResponse.Creation.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .maxCapacity(room.getMaxCapacity())
                .hotelId(room.getHotel() != null ? room.getHotel().getId() : null)
                .build();
    }
}
