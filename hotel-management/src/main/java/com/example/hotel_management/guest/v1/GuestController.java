package com.example.hotel_management.guest.v1;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.hotel_management.common.PagedResponse;
import com.example.hotel_management.guest.GuestService;
import com.example.hotel_management.guest.v1.dto.GuestRequest;
import com.example.hotel_management.guest.v1.dto.GuestResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @GetMapping
public PagedResponse<GuestResponse.QueryDetail> getGuests(
        @Valid  @ParameterObject @ModelAttribute GuestRequest.Search search) {
        

        return guestService.searchAndSortGuests(search);
    }

    @GetMapping("/hotel/{hotelId}")
    public List<GuestResponse.QueryDetail> getGuestsByHotelId(@PathVariable Long hotelId) {
        return guestService.getGuestsByHotelId(hotelId);
    }

    @GetMapping("/room/{roomId}")
    public List<GuestResponse.QueryDetail> getGuestsByRoomId(@PathVariable Long roomId) {
        return guestService.getGuestsByRoomId(roomId);
    }

    @DeleteMapping("/{guestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuest(@PathVariable Long guestId) {
        guestService.deleteGuest(guestId);
    }

    @DeleteMapping("/cancel-reservation/{voucherNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelReservation(@PathVariable String voucherNumber) {
        guestService.cancelReservation(voucherNumber);
    }
}