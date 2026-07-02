package com.example.hotel_management.hotel.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class HotelCreationRequest {
    String name;
    String location;
    String contactInfo;
}
