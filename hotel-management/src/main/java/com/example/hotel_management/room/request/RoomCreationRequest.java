package com.example.hotel_management.room.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreationRequest {
    String roomNumber;
    String roomType;
    int maxCapacity;
    Long hotelId;

}
