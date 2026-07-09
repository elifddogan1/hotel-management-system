package com.example.hotel_management.room;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.hotel_management.guest.Guest;
import com.example.hotel_management.hotel.Hotel;
import com.example.hotel_management.hotel.HotelRepository;
import com.example.hotel_management.room.dto.RoomRequest;
import com.example.hotel_management.room.dto.RoomResponse;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private RoomService roomService;

    private Hotel sampleHotel;
    private RoomRequest.Creation validRequest;
    private Room savedRoom;

    @BeforeEach
    void setUp() {
        sampleHotel = Hotel.builder()
                .id(1L)
                .name("Akdeniz Grand Hotel")
                .build();

        validRequest = RoomRequest.Creation.builder()
                .hotelId(1L)
                .roomNumber("101")
                .roomType("Deluxe Suite")
                .maxCapacity(3)
                .build();

        savedRoom = Room.builder()
                .id(100L)
                .roomNumber("101")
                .roomType("Deluxe Suite")
                .maxCapacity(3)
                .hotel(sampleHotel)
                .build();
    }

    @Test
    @DisplayName("createRoom - Başarılı Senaryo: Oda başarıyla oluşturulmalı ve Creation DTO dönmeli")
    void createRoom_WhenRequestIsValid_ShouldReturnRoomCreationResponse() {
        // Given
        when(roomRepository.existsByRoomNumberAndHotelId(validRequest.getRoomNumber(), validRequest.getHotelId()))
                .thenReturn(false);
        when(hotelRepository.findById(validRequest.getHotelId()))
                .thenReturn(Optional.of(sampleHotel));
        when(roomRepository.save(any(Room.class)))
                .thenReturn(savedRoom);

        // When
        RoomResponse.Creation result = roomService.createRoom(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(savedRoom.getId(), result.getId());
        assertEquals(savedRoom.getRoomNumber(), result.getRoomNumber());
        assertEquals(savedRoom.getRoomType(), result.getRoomType());
        assertEquals(savedRoom.getMaxCapacity(), result.getMaxCapacity());
        assertEquals(sampleHotel.getId(), result.getHotelId());

        verify(roomRepository, times(1)).existsByRoomNumberAndHotelId(validRequest.getRoomNumber(), validRequest.getHotelId());
        verify(hotelRepository, times(1)).findById(validRequest.getHotelId());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("createRoom - Hata Senaryosu: Aynı oda numarası varsa IllegalArgumentException fırlatmalı")
    void createRoom_WhenRoomNumberAlreadyExists_ShouldThrowIllegalArgumentException() {
        // Given
        when(roomRepository.existsByRoomNumberAndHotelId(validRequest.getRoomNumber(), validRequest.getHotelId()))
                .thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roomService.createRoom(validRequest);
        });

        assertEquals("This room already exists in this hotel.", exception.getMessage());
        
        // Veritabanı kaydı veya otel sorgusu tetiklenmemeli
        verify(hotelRepository, never()).findById(anyLong());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("createRoom - Hata Senaryosu: Otel bulunamazsa EntityNotFoundException fırlatmalı")
    void createRoom_WhenHotelNotFound_ShouldThrowEntityNotFoundException() {
        // Given
        when(roomRepository.existsByRoomNumberAndHotelId(validRequest.getRoomNumber(), validRequest.getHotelId()))
                .thenReturn(false);
        when(hotelRepository.findById(validRequest.getHotelId()))
                .thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            roomService.createRoom(validRequest);
        });

        assertEquals("Hotel with ID 1 could not be found.", exception.getMessage());
        
        // Otel yoksa save metodu asla çağrılmamalı
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("updateRoom - Başarılı Senaryo: Bilgiler geçerliyse oda güncellenmeli")
    void updateRoom_WhenRequestIsValid_ShouldUpdateAndReturnResponse() {
        // Given
        Long roomId = 100L;
        // Oda numarası aynı kalıyor ("101"), kapasite 3'ten 4'e çıkıyor
        RoomRequest.Creation updateRequest = RoomRequest.Creation.builder()
                .hotelId(1L)
                .roomNumber("101")
                .roomType("Updated Suite")
                .maxCapacity(4)
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        // When
        RoomResponse.Creation result = roomService.updateRoom(roomId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("101", savedRoom.getRoomNumber());
        assertEquals("Updated Suite", savedRoom.getRoomType());
        assertEquals(4, savedRoom.getMaxCapacity());
        
        verify(roomRepository, times(1)).findById(roomId);
        verify(roomRepository, never()).existsByRoomNumberAndHotelId(anyString(), anyLong());
        verify(roomRepository, times(1)).save(savedRoom);
    }

    @Test
    @DisplayName("updateRoom - Başarılı Senaryo: Otel değiştirilmek istendiğinde yeni otel atanmalı")
    void updateRoom_WhenHotelChanges_ShouldFetchNewHotelAndSubstitue() {
        // Given
        Long roomId = 100L;
        Hotel newHotel = Hotel.builder().id(2L).name("Antalya Beach Resort").build();
        RoomRequest.Creation updateRequest = RoomRequest.Creation.builder()
                .hotelId(2L) // Otel ID değişti
                .roomNumber("101")
                .roomType("Deluxe Suite")
                .maxCapacity(3)
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));
        when(hotelRepository.findById(2L)).thenReturn(Optional.of(newHotel));
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        // When
        roomService.updateRoom(roomId, updateRequest);

        // Then
        assertEquals(2L, savedRoom.getHotel().getId());
        verify(hotelRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("updateRoom - Hata Senaryosu: Oda bulunamazsa EntityNotFoundException fırlatmalı")
    void updateRoom_WhenRoomNotFound_ShouldThrowEntityNotFoundException() {
        // Given
        Long roomId = 999L;
        RoomRequest.Creation updateRequest = RoomRequest.Creation.builder().hotelId(1L).build();
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            roomService.updateRoom(roomId, updateRequest);
        });

        assertEquals("Room with ID 999 could not be found.", exception.getMessage());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("updateRoom - Hata Senaryosu: Yeni oda numarası aynı otelde zaten varsa IllegalArgumentException fırlatmalı")
    void updateRoom_WhenRoomNumberConflictInSameHotel_ShouldThrowIllegalArgumentException() {
        // Given
        Long roomId = 100L;
        // Oda numarası "101"'den "102"'ye değiştirilmek isteniyor
        RoomRequest.Creation updateRequest = RoomRequest.Creation.builder()
                .hotelId(1L)
                .roomNumber("102")
                .roomType("Deluxe Suite")
                .maxCapacity(3)
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));
        when(roomRepository.existsByRoomNumberAndHotelId("102", 1L)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roomService.updateRoom(roomId, updateRequest);
        });

        assertEquals("This room already exists in this hotel.", exception.getMessage());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("updateRoom - Hata Senaryosu: Kapasite, mevcut rezervasyondaki konuk sayısının altına düşürülürse hata fırlatmalı")
    void updateRoom_WhenCapacityIsLessThanExistingGuests_ShouldThrowIllegalArgumentException() {
        // Given
        Long roomId = 100L;
        // Odaya kayıtlı aynı voucher'a (VCH-123) sahip 3 adet guest mock'lıyoruz
        Guest guest1 = Guest.builder().voucherNumber("VCH-123").build();
        Guest guest2 = Guest.builder().voucherNumber("VCH-123").build();
        Guest guest3 = Guest.builder().voucherNumber("VCH-123").build();
        savedRoom.setGuests(List.of(guest1, guest2, guest3)); // Odada şu an 3 kişi kalıyor

        // Kapasiteyi 3'ten 2'ye düşürmeye çalışıyoruz
        RoomRequest.Creation updateRequest = RoomRequest.Creation.builder()
                .hotelId(1L)
                .roomNumber("101")
                .roomType("Deluxe Suite")
                .maxCapacity(2) 
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roomService.updateRoom(roomId, updateRequest);
        });

        assertTrue(exception.getMessage().contains("Cannot reduce capacity below the number of guests in an existing booking"));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("deleteRoom - Başarılı Senaryo: Oda mevcutsa silme işlemi tetiklenmeli")
    void deleteRoom_WhenRoomExists_ShouldDeleteSuccessfully() {
        // Given
        Long roomId = 100L;
        when(roomRepository.existsById(roomId)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(roomId);

        // When
        assertDoesNotThrow(() -> roomService.deleteRoom(roomId));

        // Then
        verify(roomRepository, times(1)).existsById(roomId);
        verify(roomRepository, times(1)).deleteById(roomId);
    }

    @Test
    @DisplayName("deleteRoom - Hata Senaryosu: Oda bulunamazsa EntityNotFoundException fırlatmalı")
    void deleteRoom_WhenRoomDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Given
        Long roomId = 999L;
        when(roomRepository.existsById(roomId)).thenReturn(false);

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            roomService.deleteRoom(roomId);
        });

        assertEquals("Room with ID 999 could not be found.", exception.getMessage());
        verify(roomRepository, times(1)).existsById(roomId);
        verify(roomRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("getRoomById - Başarılı Senaryo: Oda mevcutsa QueryDetail DTO dönmeli")
    void getRoomById_WhenRoomExists_ShouldReturnQueryDetail() {
        // Given
        Long roomId = 100L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));

        // When
        RoomResponse.QueryDetail result = roomService.getRoomById(roomId);

        // Then
        assertNotNull(result);
        assertEquals(savedRoom.getId(), result.getId());
        assertEquals(savedRoom.getRoomNumber(), result.getRoomNumber());
        assertEquals(sampleHotel.getName(), result.getHotelName());
        verify(roomRepository, times(1)).findById(roomId);
    }

    @Test
    @DisplayName("getAllRooms - Başarılı Senaryo: Tüm odaları liste halinde QueryDetail DTO olarak dönmeli")
    void getAllRooms_ShouldReturnQueryDetailList() {
        // Given
        when(roomRepository.findAll()).thenReturn(java.util.List.of(savedRoom));

        // When
        java.util.List<RoomResponse.QueryDetail> result = roomService.getAllRooms();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(savedRoom.getRoomNumber(), result.get(0).getRoomNumber());
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getRoomsByHotelId - Başarılı Senaryo: Belirli otele ait odaları dönmeli")
    void getRoomsByHotelId_ShouldReturnRoomsForThatHotel() {
        // Given
        Long hotelId = 1L;
        when(roomRepository.findByHotelId(hotelId)).thenReturn(java.util.List.of(savedRoom));

        // When
        java.util.List<RoomResponse.QueryDetail> result = roomService.getRoomsByHotelId(hotelId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(hotelId, result.get(0).getHotelId());
        verify(roomRepository, times(1)).findByHotelId(hotelId);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("searchAndSortRooms - Başarılı Senaryo: Sayfalamalı ve filtreli arama sonucu PagedResponse dönmeli")
    void searchAndSortRooms_ShouldReturnPagedResponseOfRooms() {
        // Given
        RoomRequest.Search searchRequest = new RoomRequest.Search();
        searchRequest.setRoomNumber("101");
        searchRequest.setPage(0);
        searchRequest.setSize(10);

        org.springframework.data.domain.Page<Room> mockPage = mock(org.springframework.data.domain.Page.class);
        when(mockPage.getContent()).thenReturn(java.util.List.of(savedRoom));
        when(mockPage.getNumber()).thenReturn(0);
        when(mockPage.getSize()).thenReturn(10);
        when(mockPage.getTotalElements()).thenReturn(1L);
        when(mockPage.getTotalPages()).thenReturn(1);
        when(mockPage.isLast()).thenReturn(true);

        when(roomRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.PageRequest.class)))
                .thenReturn(mockPage);

        // When
        com.example.hotel_management.common.PagedResponse<RoomResponse.QueryDetail> result = roomService.searchAndSortRooms(searchRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPageNo());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLast());
        verify(roomRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.PageRequest.class));
    }

    @Test
    @DisplayName("findOptimalRoom - Başarılı Senaryo: Tarih çakışması olmayan ve kapasitesi yeten odalar dönmeli")
    void findOptimalRoom_WhenRoomIsAvailable_ShouldReturnAvailableRooms() {
        // Given
        Long hotelId = 1L;
        int numberOfPerson = 2;
        java.time.LocalDate checkIn = java.time.LocalDate.of(2026, 8, 1);
        java.time.LocalDate checkOut = java.time.LocalDate.of(2026, 8, 5);

        // Odanın geçmiş/farklı bir tarihte rezervasyonu var (çakışma yok)
        Guest pastGuest = Guest.builder()
                .checkInDate(java.time.LocalDate.of(2026, 7, 10))
                .checkOutDate(java.time.LocalDate.of(2026, 7, 15))
                .build();
        savedRoom.setGuests(java.util.List.of(pastGuest));

        when(roomRepository.findByHotelIdAndMaxCapacityGreaterThanEqual(hotelId, numberOfPerson))
                .thenReturn(java.util.List.of(savedRoom));

        // When
        java.util.List<RoomResponse.QueryDetail> result = roomService.findOptimalRoom(hotelId, numberOfPerson, checkIn, checkOut);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(savedRoom.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("findOptimalRoom - Filtreleme Senaryosu: Tarih çakışması olan oda elenmeli (boş liste dönmeli)")
    void findOptimalRoom_WhenRoomDatesConflict_ShouldExcludeThatRoom() {
        // Given
        Long hotelId = 1L;
        int numberOfPerson = 2;
        java.time.LocalDate checkIn = java.time.LocalDate.of(2026, 8, 1);
        java.time.LocalDate checkOut = java.time.LocalDate.of(2026, 8, 5);

        // İstenen tarihlerle çakışan bir rezervasyon ekliyoruz
        Guest conflictingGuest = Guest.builder()
                .checkInDate(java.time.LocalDate.of(2026, 8, 3)) // Giriş tarihinden sonra, çıkıştan önce
                .checkOutDate(java.time.LocalDate.of(2026, 8, 10))
                .build();
        savedRoom.setGuests(java.util.List.of(conflictingGuest));

        when(roomRepository.findByHotelIdAndMaxCapacityGreaterThanEqual(hotelId, numberOfPerson))
                .thenReturn(java.util.List.of(savedRoom));

        // When
        java.util.List<RoomResponse.QueryDetail> result = roomService.findOptimalRoom(hotelId, numberOfPerson, checkIn, checkOut);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Çakışmadan dolayı oda elendi, liste boş olmalı
    }

    @Test
    @DisplayName("findOptimalRoomWithoutHotelFilter - Başarılı Senaryo: Otel filtresiz uygun odalar listelenmeli")
    void findOptimalRoomWithoutHotelFilter_ShouldReturnAvailableRoomsGlobally() {
        // Given
        int numberOfPerson = 2;
        java.time.LocalDate checkIn = java.time.LocalDate.of(2026, 8, 1);
        java.time.LocalDate checkOut = java.time.LocalDate.of(2026, 8, 5);
        savedRoom.setGuests(new java.util.ArrayList<>()); // Rezervasyonu yok, müsait

        when(roomRepository.findByMaxCapacityGreaterThanEqual(numberOfPerson))
                .thenReturn(java.util.List.of(savedRoom));

        // When
        java.util.List<RoomResponse.QueryDetail> result = roomService.findOptimalRoomWithoutHotelFilter(numberOfPerson, checkIn, checkOut);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roomRepository, times(1)).findByMaxCapacityGreaterThanEqual(numberOfPerson);
    }

}