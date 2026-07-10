package com.example.hotel_management.hotel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.hotel_management.hotel.v1.dto.HotelRequest;
import com.example.hotel_management.hotel.v1.dto.HotelResponse;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @org.mockito.Spy
    private HotelMapper hotelMapper = new HotelMapper();

    @InjectMocks
    private HotelService hotelService;

    @Test
    void getAllHotels_WhenHotelsExist_ShouldReturnHotelResponseList() {
        Hotel hotel1 = Hotel.builder()
                .id(1L)
                .name("Büyük Otel")
                .location("Antalya")
                .contactInfo("0242 111 2233")
                .build();
                
        Hotel hotel2 = Hotel.builder()
                .id(2L)
                .name("Şehir Oteli")
                .location("Ankara")
                .contactInfo("0312 444 5566")
                .build();
                
        List<Hotel> mockHotelList = List.of(hotel1, hotel2);
        
        when(hotelRepository.findAll()).thenReturn(mockHotelList);

        List<HotelResponse> result = hotelService.getAllHotels();

        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals(1L, result.get(0).getId());
        assertEquals("Büyük Otel", result.get(0).getName());
        assertEquals("Antalya", result.get(0).getLocation());
        
        assertEquals(2L, result.get(1).getId());
        assertEquals("Şehir Oteli", result.get(1).getName());
        
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void getAllHotels_WhenNoHotelsExist_ShouldReturnEmptyList() {
        when(hotelRepository.findAll()).thenReturn(new ArrayList<>());

        List<HotelResponse> result = hotelService.getAllHotels();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void getHotelById_WhenHotelExists_ShouldReturnHotelResponse() {
        Long hotelId = 1L;
        Hotel hotel = Hotel.builder()
                .id(hotelId)
                .name("Akdeniz Resort")
                .location("Antalya")
                .contactInfo("0242 123 4567")
                .build();

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        HotelResponse result = hotelService.getHotelById(hotelId);

        assertNotNull(result);
        assertEquals(hotelId, result.getId());
        assertEquals("Akdeniz Resort", result.getName());
        assertEquals("Antalya", result.getLocation());
        assertEquals("0242 123 4567", result.getContactInfo());

        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void getHotelById_WhenHotelDoesNotExist_ShouldThrowEntityNotFoundException() {
        Long hotelId = 99L;
        
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            hotelService.getHotelById(hotelId);
        });

        assertEquals("Hotel not found with ID: " + hotelId, exception.getMessage());

        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void deleteHotel_WhenHotelExistsAndHasNoRooms_ShouldDeleteHotel() {
        Long hotelId = 1L;
        Hotel hotel = Hotel.builder()
                .id(hotelId)
                .name("Akdeniz Resort")
                .location("Antalya")
                .rooms(new ArrayList<>())
                .build();
        
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        hotelService.deleteHotel(hotelId);

        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelRepository, times(1)).delete(hotel);
    }

    @Test
    void deleteHotel_WhenHotelExistsAndHasRooms_ShouldThrowIllegalArgumentException() {
        Long hotelId = 1L;
        com.example.hotel_management.room.Room room = com.example.hotel_management.room.Room.builder().id(100L).build();
        Hotel hotel = Hotel.builder()
                .id(hotelId)
                .name("Akdeniz Resort")
                .location("Antalya")
                .rooms(List.of(room))
                .build();

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hotelService.deleteHotel(hotelId);
        });

        assertEquals("Bu otele ait odalar bulunmaktadır. Önce odaları silmelisiniz.", exception.getMessage());
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelRepository, never()).delete(any(Hotel.class));
    }

    @Test
    void deleteHotel_WhenHotelDoesNotExist_ShouldThrowEntityNotFoundException() {
        Long hotelId = 99L;
        
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            hotelService.deleteHotel(hotelId);
        });

        assertEquals("Hotel not found with ID: " + hotelId, exception.getMessage());

        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelRepository, never()).delete(any(Hotel.class));
    }

    @Test
    void updateHotel_WhenHotelExists_ShouldUpdateAndSave() {
        Long hotelId = 1L;
        
        Hotel existingHotel = Hotel.builder()
                .id(hotelId)
                .name("Eski İsim")
                .location("Eski Lokasyon")
                .contactInfo("Eski İletişim")
                .build();

        HotelRequest.Update request = HotelRequest.Update.builder()
                .name("Yeni İsim")
                .location("Yeni Lokasyon")
                .contactInfo("Yeni İletişim")
                .build();

        Hotel updatedHotel = Hotel.builder()
                .id(hotelId)
                .name(request.getName())
                .location(request.getLocation())
                .contactInfo(request.getContactInfo())
                .build();

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(existingHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(updatedHotel);

        HotelResponse result = hotelService.updateHotel(hotelId, request);
        assertNotNull(result);
        assertEquals(hotelId, result.getId());
        assertEquals("Yeni İsim", result.getName());
        assertEquals("Yeni Lokasyon", result.getLocation());
        assertEquals("Yeni İletişim", result.getContactInfo());

        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelRepository, times(1)).save(existingHotel); 
    }

    @Test
    void updateHotel_WhenHotelDoesNotExist_ShouldThrowEntityNotFoundException() {
        Long hotelId = 99L; 
        HotelRequest.Update request = HotelRequest.Update.builder()
                .name("Yeni İsim")
                .build();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            hotelService.updateHotel(hotelId, request);
        });

        assertEquals("Hotel not found with ID: " + hotelId, exception.getMessage());

        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelRepository, never()).save(any(Hotel.class));
    }

    @Test
    void createHotel_WhenCreationIsSuccessful_ShouldReturnHotelResponse() {
        HotelRequest.Creation request = HotelRequest.Creation.builder()
                .name("Yeni Otel")
                .location("Sahil Yolu")
                .contactInfo("iletisim@yeniotel.com")
                .build();

        Hotel savedHotel = Hotel.builder()
                .id(1L)
                .name(request.getName())
                .location(request.getLocation())
                .contactInfo(request.getContactInfo())
                .build();

        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);

        HotelResponse result = hotelService.createHotel(request);

        assertNotNull(result);
        assertEquals(1L, result.getId()); 
        assertEquals("Yeni Otel", result.getName());
        assertEquals("Sahil Yolu", result.getLocation());
        assertEquals("iletisim@yeniotel.com", result.getContactInfo());

        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }
    
}