package com.example.hotel_management.guest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.hotel_management.guest.request.GuestCreationRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor

public class GuestController {

    private final GuestService guestService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Guest> createGuest(@RequestBody GuestCreationRequest request) {
        return guestService.createGuest(request);
    }

    @GetMapping
    public List<Guest> getGuests(@RequestParam(required = false) String lastName,
            @RequestParam(required = false) String voucherNumber,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return guestService.searchAndSortGuests(lastName, voucherNumber, sortBy, direction);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuest(@PathVariable Long guestId) {
        guestService.deleteGuest(guestId);
    }

    @DeleteMapping("/cancel-reservation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelReservation(@PathVariable Long voucherNumber) {
        guestService.cancelReservation(voucherNumber);
    }

}