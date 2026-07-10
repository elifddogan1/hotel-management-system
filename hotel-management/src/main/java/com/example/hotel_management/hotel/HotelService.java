package com.example.hotel_management.hotel;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.hotel_management.hotel.dto.HotelRequest;
import com.example.hotel_management.hotel.dto.HotelResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelResponse createHotel(HotelRequest.Creation request) {
        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .location(request.getLocation())
                .contactInfo(request.getContactInfo())
                .build();
                
        Hotel savedHotel = hotelRepository.save(hotel);
        return mapToResponse(savedHotel);
    }

    public List<HotelResponse> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public HotelResponse getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + id));
        return mapToResponse(hotel);
    }

    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new EntityNotFoundException("Hotel not found with ID: " + id);
        }
        // @SQLDelete anotasyonu sayesinde bu işlem otomatik olarak update'e dönüşecek.
        hotelRepository.deleteById(id);
    }

    public HotelResponse updateHotel(Long id, HotelRequest.Update request) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + id));
                
        hotel.setName(request.getName());
        hotel.setLocation(request.getLocation());
        hotel.setContactInfo(request.getContactInfo());
        
        Hotel updatedHotel = hotelRepository.save(hotel);
        return mapToResponse(updatedHotel);
    }

    // Helper method for mapping Entity to Response DTO
    private HotelResponse mapToResponse(Hotel hotel) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .location(hotel.getLocation())
                .contactInfo(hotel.getContactInfo())
                .build();
    }
}