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

    // 1. Tüm misafirleri getirmek için parametresiz arama metodunu tetikliyoruz
    @GetMapping
    public List<Guest> getAllGuests() {
        return guestService.searchAndSortGuests(null, null, "id", "asc");
    }

    // 2. Dinamik Filtreleme ve Arama (Çakışmayı önlemek için /search yapıldı)
    @GetMapping("/search")
    public List<Guest> getGuests(@RequestParam(required = false) String lastName,
            @RequestParam(required = false) String voucherNumber,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return guestService.searchAndSortGuests(lastName, voucherNumber, sortBy, direction);
    }

    // 3. Otele Göre Rezervasyonları Çekme (Yeni mimarimizin kalbi)
    @GetMapping("/hotel/{hotelId}")
    public List<Guest> getGuestsByHotelId(@PathVariable Long hotelId) {
        return guestService.getGuestsByHotelId(hotelId);
    }

    // 4. Odaya Göre Misafirleri Çekme
    @GetMapping("/room/{roomId}")
    public List<Guest> getGuestsByRoomId(@PathVariable Long roomId) {
        return guestService.getGuestsByRoomId(roomId);
    }

    // 5. Rezervasyon / Misafir Kaydı Oluşturma
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Guest> createGuest(@RequestBody GuestCreationRequest request) {
        return guestService.createGuest(request);
    }

    // 6. Tek Bir Misafiri Silme
    @DeleteMapping("/{guestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuest(@PathVariable Long guestId) {
        guestService.deleteGuest(guestId);
    }

    // 7. Voucher Numarasına Göre Toplu Rezervasyon İptali
    @DeleteMapping("/cancel-reservation/{voucherNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelReservation(@PathVariable Long voucherNumber) {
        guestService.cancelReservation(voucherNumber);
    }
}