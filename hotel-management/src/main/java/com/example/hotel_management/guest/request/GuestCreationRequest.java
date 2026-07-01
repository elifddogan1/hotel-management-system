package com.example.hotel_management.guest.request;

import java.time.LocalDate;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class GuestCreationRequest {
    String firstname;
    String lastname;
    String voucherNumber;
    Long roomId;
    LocalDate checkInDate;
    LocalDate checkOutDate;
}