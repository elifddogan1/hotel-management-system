package com.example.hotel_management.guest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.hotel_management.guest.v1.dto.GuestRequest;
import com.example.hotel_management.guest.v1.dto.GuestResponse;
import com.example.hotel_management.hotel.Hotel;
import com.example.hotel_management.hotel.HotelRepository;
import com.example.hotel_management.room.Room;
import com.example.hotel_management.room.RoomRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class) 
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository; 

    @Mock
    private RoomRepository roomRepository; 

    @Mock
    private HotelRepository hotelRepository;

    @org.mockito.Spy
    private GuestMapper guestMapper = new GuestMapper();

    @InjectMocks
    private GuestService guestService; 

    @Test
    void searchAndSortGuests_WithEmptyRequest_ShouldCallRepositoryWithNonNullSpec() {
        GuestRequest.Search request = new GuestRequest.Search();
        request.setFirstname("");
        request.setLastname("");
        request.setVoucherNumber("");
        request.setRoomNumber("");
        request.setHotelName("");
        request.setPage(0);
        request.setSize(1);
        request.setSortBy("");
        request.setDirection("");

        org.springframework.data.domain.Page<Guest> mockPage = org.mockito.Mockito.mock(org.springframework.data.domain.Page.class);
        when(guestRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.jpa.domain.Specification.class), org.mockito.ArgumentMatchers.any(org.springframework.data.domain.PageRequest.class))).thenReturn(mockPage);

        Guest mockGuest = Guest.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .build();
        when(mockPage.getContent()).thenReturn(List.of(mockGuest));
        when(mockPage.getNumber()).thenReturn(0);
        when(mockPage.getSize()).thenReturn(1);
        when(mockPage.getTotalElements()).thenReturn(1L);
        when(mockPage.getTotalPages()).thenReturn(1);
        when(mockPage.isLast()).thenReturn(true);

        var response = guestService.searchAndSortGuests(request);

        assertNotNull(response);
    }
    
    @Test
    void getGuestsByRoomId_WhenGuestsExist_ShouldReturnQueryDetailList() {
        
        Long roomId = 1L;

        
        Room mockRoom = new Room();
       
        Guest mockGuest = Guest.builder()
                .id(100L)
                .firstname("Ahmet")
                .lastname("Yılmaz")
                .voucherNumber("VCH-12345678")
                .room(mockRoom)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(3))
                .build();

        when(guestRepository.findByRoom_Id(roomId)).thenReturn(List.of(mockGuest));

        List<GuestResponse.QueryDetail> resultList = guestService.getGuestsByRoomId(roomId);

        assertNotNull(resultList); 
        assertEquals(1, resultList.size()); 
        assertEquals("Ahmet", resultList.get(0).getFirstname()); 
        assertEquals("VCH-12345678", resultList.get(0).getVoucherNumber()); 

        verify(guestRepository, times(1)).findByRoom_Id(roomId);
    }

    @Test
    void getGuestsByRoomId_WhenNoGuestsOrRoomDoesNotExist_ShouldReturnEmptyList() {
        Long roomId = 99L;

        when(guestRepository.findByRoom_Id(roomId)).thenReturn(new ArrayList<>());

        List<GuestResponse.QueryDetail> resultList = guestService.getGuestsByRoomId(roomId);

        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
        verify(guestRepository, times(1)).findByRoom_Id(roomId);
    }

    @Test
    void getGuestsByHotelId_WhenGuestsExist_ShouldReturnQueryDetailList() {
        
        Long hotelId = 1L;
        Hotel mockHotel = new Hotel();
        mockHotel.setId(hotelId);
        mockHotel.setName("Gözde Hotel");

        Long roomId = 2L;
        Room mockRoom = new Room();
        mockRoom.setHotel(mockHotel);

        Guest mockGuest = Guest.builder()
                .id(100L)
                .firstname("Ahmet")
                .lastname("Yılmaz")
                .voucherNumber("VCH-12345678")
                .room(mockRoom)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(3))
                .build();

        when(guestRepository.findByRoom_Hotel_Id(hotelId)).thenReturn(List.of(mockGuest));

        List<GuestResponse.QueryDetail> resultList = guestService.getGuestsByHotelId(hotelId);

        assertNotNull(resultList); 
        assertEquals(1, resultList.size()); 
        assertEquals("Ahmet", resultList.get(0).getFirstname()); 
        assertEquals("VCH-12345678", resultList.get(0).getVoucherNumber()); 

        verify(guestRepository, times(1)).findByRoom_Hotel_Id(hotelId);
    }

    @Test
    void getGuestsByHotelId_WhenNoGuestsOrHotelDoesNotExist_ShouldReturnEmptyList() {
        Long hotelId = 99L;

        when(guestRepository.findByRoom_Hotel_Id(hotelId)).thenReturn(new ArrayList<>());

        List<GuestResponse.QueryDetail> resultList = guestService.getGuestsByHotelId(hotelId);

        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
        verify(guestRepository, times(1)).findByRoom_Hotel_Id(hotelId);
    }


    @Test
    void deleteGuest_WhenGuestDoesNotExist_ShouldThrowEntityNotFoundException() {
        Long nonExistingGuestId = 99L;

        when(guestRepository.existsById(nonExistingGuestId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            guestService.deleteGuest(nonExistingGuestId);
        });

        assertEquals("Reservation with ID 99 could not be found.", exception.getMessage());

       
        verify(guestRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteGuest_WhenGuestExists_ShouldDeleteGuest() {
        Long existingGuestId = 1L;

        when(guestRepository.existsById(existingGuestId)).thenReturn(true);

        guestService.deleteGuest(existingGuestId);

        verify(guestRepository, times(1)).deleteById(existingGuestId);
    }

    @Test
    void cancelReservation_WhenVoucherDoesNotExist_ShouldThrowEntityNotFoundException() {
        String fakeVoucher = "VCH-INVALID";
        
        when(guestRepository.findByVoucherNumber(fakeVoucher)).thenReturn(new ArrayList<>());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            guestService.cancelReservation(fakeVoucher);
        });

        assertEquals("Reservation with ID VCH-INVALID could not be found.", exception.getMessage());
        verify(guestRepository, never()).deleteAll(anyList());
    }

    @Test
    void cancelReservation_WhenVoucherExists_ShouldDeleteReservation() {
        String validVoucher = "VCH-12345678";
        Guest mockGuest = Guest.builder().voucherNumber(validVoucher).build();
        
        when(guestRepository.findByVoucherNumber(validVoucher)).thenReturn(List.of(mockGuest));

        guestService.cancelReservation(validVoucher);

        verify(guestRepository, times(1)).deleteAll(anyList());
    }

    @Test
    void updateGuestsForReservation_WhenGuestCountIncreases_ShouldAddNewGuestAndSave() {
        String voucher = "VCH-UPDATE";
        Long roomId = 1L;
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Room mockRoom = new Room();
        mockRoom.setId(roomId);
        mockRoom.setMaxCapacity(4); 

        Guest existingGuest = Guest.builder()
                .id(100L)
                .firstname("Elif")
                .lastname("Dogan") 
                .voucherNumber(voucher)
                .room(mockRoom)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .build();

        mockRoom.setGuests(new ArrayList<>(List.of(existingGuest)));

        GuestRequest.GuestDto updatedGuest1 = GuestRequest.GuestDto.builder()
                .firstname("Elif")
                .lastname("Yılmaz") 
                .build();
                
        GuestRequest.GuestDto newGuest2 = GuestRequest.GuestDto.builder()
                .firstname("Ahmet")
                .lastname("Yılmaz") 
                .build();

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(roomId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guests(List.of(updatedGuest1, newGuest2)) 
                .build();

        when(guestRepository.findByVoucherNumber(voucher)).thenReturn(new ArrayList<>(List.of(existingGuest)));

        GuestResponse.Creation response = guestService.updateGuestsForReservation(voucher, mockRoom, request);

        assertNotNull(response);
        assertEquals(2, response.getGuests().size()); 
        assertEquals("Yılmaz", response.getGuests().get(0).getLastname()); 
        assertEquals("Ahmet", response.getGuests().get(1).getFirstname()); 

        verify(guestRepository, times(1)).saveAll(anyList());
        verify(guestRepository, never()).deleteAll(anyList());
    }

    @Test
    void updateGuestsForReservation_WhenGuestCountDecreases_ShouldRemoveExcessGuestsAndSave() {
        String voucher = "VCH-UPDATE";
        Long roomId = 1L;
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Room mockRoom = new Room();
        mockRoom.setId(roomId);
        mockRoom.setMaxCapacity(4);

        Guest existingGuest1 = Guest.builder().id(101L).firstname("Elif").lastname("Dogan").voucherNumber(voucher).room(mockRoom).checkInDate(checkIn).checkOutDate(checkOut).build();
        Guest existingGuest2 = Guest.builder().id(102L).firstname("Ahmet").lastname("Yilmaz").voucherNumber(voucher).room(mockRoom).checkInDate(checkIn).checkOutDate(checkOut).build();
        Guest existingGuest3 = Guest.builder().id(103L).firstname("Zeynep").lastname("Kaya").voucherNumber(voucher).room(mockRoom).checkInDate(checkIn).checkOutDate(checkOut).build();

        mockRoom.setGuests(new ArrayList<>(List.of(existingGuest1, existingGuest2, existingGuest3)));

        GuestRequest.GuestDto updatedGuest1 = GuestRequest.GuestDto.builder().firstname("Elif").lastname("Dogan").build();
        GuestRequest.GuestDto updatedGuest2 = GuestRequest.GuestDto.builder().firstname("Ahmet").lastname("Yilmaz").build();

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(roomId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guests(List.of(updatedGuest1, updatedGuest2))
                .build();

        when(guestRepository.findByVoucherNumber(voucher)).thenReturn(new ArrayList<>(List.of(existingGuest1, existingGuest2, existingGuest3)));

        GuestResponse.Creation response = guestService.updateGuestsForReservation(voucher, mockRoom, request);

        assertNotNull(response);
        assertEquals(2, response.getGuests().size());

        verify(guestRepository, times(1)).deleteAll(anyList());
        verify(guestRepository, times(1)).saveAll(anyList());
    }

    @Test
    void updateGuestsForReservation_WhenVoucherDoesNotExist_ShouldThrowEntityNotFoundException() {
        String invalidVoucher = "VCH-INVALID";
        GuestRequest.Creation request = GuestRequest.Creation.builder().build();

        when(guestRepository.findByVoucherNumber(invalidVoucher)).thenReturn(new ArrayList<>());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            guestService.updateGuestsForReservation(invalidVoucher, new Room(), request);
        });

        assertEquals("Reservation with voucher VCH-INVALID could not be found.", exception.getMessage());
    }

    @Test
    void updateGuestsForReservation_WhenUpdateIsSuccessfulWithSameGuestCount_ShouldUpdateAndSave() {
        String voucher = "VCH-UPDATE";
        Long roomId = 1L;
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Room mockRoom = new Room();
        mockRoom.setId(roomId);
        mockRoom.setMaxCapacity(4);

        Guest existingGuest1 = Guest.builder().id(101L).firstname("EskiAd1").lastname("EskiSoyad1").voucherNumber(voucher).room(mockRoom).checkInDate(checkIn).checkOutDate(checkOut).build();
        Guest existingGuest2 = Guest.builder().id(102L).firstname("EskiAd2").lastname("EskiSoyad2").voucherNumber(voucher).room(mockRoom).checkInDate(checkIn).checkOutDate(checkOut).build();

        mockRoom.setGuests(new ArrayList<>(List.of(existingGuest1, existingGuest2)));

        GuestRequest.GuestDto updatedGuest1 = GuestRequest.GuestDto.builder().firstname("YeniAd1").lastname("YeniSoyad1").build();
        GuestRequest.GuestDto updatedGuest2 = GuestRequest.GuestDto.builder().firstname("YeniAd2").lastname("YeniSoyad2").build();

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(roomId)
                .checkInDate(checkIn.plusDays(1)) 
                .checkOutDate(checkOut.plusDays(1))
                .guests(List.of(updatedGuest1, updatedGuest2))
                .build();

        when(guestRepository.findByVoucherNumber(voucher)).thenReturn(new ArrayList<>(List.of(existingGuest1, existingGuest2)));

        GuestResponse.Creation response = guestService.updateGuestsForReservation(voucher, mockRoom, request);

        assertNotNull(response);
        assertEquals(2, response.getGuests().size());
        assertEquals("YeniAd1", response.getGuests().get(0).getFirstname());
        assertEquals("YeniAd2", response.getGuests().get(1).getFirstname());

        verify(guestRepository, times(1)).saveAll(anyList());
        verify(guestRepository, never()).deleteAll(anyList());
    }

    @Test 
    void createGuestsForReservation_WhenCreationIsSuccessful_ShouldReturnResponse(){
        Room mockRoom = new Room();
        Long roomId = 99L;
        mockRoom.setMaxCapacity(4);
        mockRoom.setId(roomId);
        mockRoom.setGuests(new ArrayList<>());

        GuestRequest.GuestDto guest1 = GuestRequest.GuestDto.builder()
                                                            .firstname("Elif")
                                                            .lastname("Dogan")
                                                            .build();
        GuestRequest.GuestDto guest2 = GuestRequest.GuestDto.builder()
                                                            .firstname("Ahmet")
                                                            .lastname("Yilmaz")
                                                            .build();
        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(roomId)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(2))
                .guests(List.of(guest1, guest2)) 
                .build();

        GuestResponse.Creation response = guestService.createGuestsForReservation(mockRoom, "VCH-TEST", request);

        assertNotNull(response);
        assertEquals("VCH-TEST", response.getVoucherNumber());
        assertEquals(2, response.getGuests().size());
        verify(guestRepository, times(1)).saveAll(anyList());
    }
}