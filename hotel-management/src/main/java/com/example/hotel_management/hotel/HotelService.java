package com.example.hotel_management.hotel;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.hotel_management.hotel.v1.dto.HotelRequest;
import com.example.hotel_management.hotel.v1.dto.HotelResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    public HotelResponse createHotel(HotelRequest.Creation request) {
        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .location(request.getLocation())
                .contactInfo(request.getContactInfo())
                .build();
                
        Hotel savedHotel = hotelRepository.save(hotel);
        return hotelMapper.toResponse(savedHotel);
    }

    public List<HotelResponse> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toResponse)
                .collect(Collectors.toList());
    }

    public HotelResponse getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + id));
        return hotelMapper.toResponse(hotel);
    }

    @Transactional
    public void deleteHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + id));

        if (hotel.getRooms() != null && !hotel.getRooms().isEmpty()) {
            throw new IllegalArgumentException("Bu otele ait odalar bulunmaktadır. Önce odaları silmelisiniz.");
        }

        hotelRepository.delete(hotel);
    }

    public HotelResponse updateHotel(Long id, HotelRequest.Update request) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + id));
                
        hotel.setName(request.getName());
        hotel.setLocation(request.getLocation());
        hotel.setContactInfo(request.getContactInfo());
        
        Hotel updatedHotel = hotelRepository.save(hotel);
        return hotelMapper.toResponse(updatedHotel);
    }
}