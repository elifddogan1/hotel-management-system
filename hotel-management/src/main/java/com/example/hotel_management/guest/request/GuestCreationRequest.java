package com.example.hotel_management.guest.request;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class GuestCreationRequest {
    String voucherNumber;
    Long roomId;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    List<GuestDto> guests;
}
