package com.example.hotel_management.hotel;

import com.example.hotel_management.hotel.request.HotelCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Frontend (Angular) bağlantısı için cors güvenliği
public class HotelController {

    private final HotelService hotelService;

    // POST /api/hotels -> Yeni otel oluşturur (201 Created)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel createHotel(@RequestBody HotelCreationRequest request) {
        return hotelService.createHotel(request);
    }

    // GET /api/hotels -> Tüm otelleri listeler (200 OK)
    @GetMapping
    public List<Hotel> getAllHotels() {
        return hotelService.getAllHotels();
    }

    // GET /api/hotels/{id} -> Belirli bir oteli getirir (200 OK)
    @GetMapping("/{id}")
    public Hotel getHotelById(@PathVariable Long id) {
        return hotelService.getHotelById(id);
    }

    // DELETE /api/hotels/{id} -> Oteli ve bağlı tüm odaları/misafirleri siler (204
    // No Content)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
    }
}