package com.example.hotel_management.reservation.v1;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.hotel_management.guest.v1.dto.GuestRequest;
import com.example.hotel_management.guest.v1.dto.GuestResponse;
import com.example.hotel_management.reservation.ReservationFacade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationFacade reservationFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse.Creation createReservation(@Valid @RequestBody GuestRequest.Creation request) {
        return reservationFacade.createReservation(request);
    }

    @PutMapping("/{voucherNumber}")
    public GuestResponse.Creation updateReservation(
            @PathVariable String voucherNumber,
            @Valid @RequestBody GuestRequest.Creation request) {
        return reservationFacade.updateReservation(voucherNumber, request);
    }
}
