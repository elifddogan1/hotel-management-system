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

import com.example.hotel_management.guest.dto.GuestRequest;
import com.example.hotel_management.guest.dto.GuestResponse;
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
    void updateReservation_WhenGuestCountIncreases_ShouldAddNewGuestAndSave() {
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
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));

        GuestResponse.Creation response = guestService.updateReservation(voucher, request);

        assertNotNull(response);
        assertEquals(2, response.getGuests().size()); 
        assertEquals("Yılmaz", response.getGuests().get(0).getLastname()); 
        assertEquals("Ahmet", response.getGuests().get(1).getFirstname()); 

        verify(guestRepository, times(1)).saveAll(anyList());
        verify(guestRepository, never()).deleteAll(anyList());
    }

    @Test
    void updateReservation_WhenGuestCountDecreases_ShouldRemoveExcessGuestsAndSave() {
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
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));

        GuestResponse.Creation response = guestService.updateReservation(voucher, request);

        assertNotNull(response);
        assertEquals(2, response.getGuests().size());

        verify(guestRepository, times(1)).deleteAll(anyList());
        verify(guestRepository, times(1)).saveAll(anyList());
    }

    @Test
    void updateReservation_WhenVoucherDoesNotExist_ShouldThrowEntityNotFoundException() {
        String invalidVoucher = "VCH-INVALID";
        GuestRequest.Creation request = GuestRequest.Creation.builder().build();

        when(guestRepository.findByVoucherNumber(invalidVoucher)).thenReturn(new ArrayList<>());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            guestService.updateReservation(invalidVoucher, request);
        });

        assertEquals("Reservation with voucher VCH-INVALID could not be found.", exception.getMessage());
    }

    @Test
    void updateReservation_WhenCheckOutIsBeforeOrEqualCheckIn_ShouldThrowIllegalArgumentException() {
        String voucher = "VCH-UPDATE";
        
        Guest existingGuest = Guest.builder().voucherNumber(voucher).build();
        when(guestRepository.findByVoucherNumber(voucher)).thenReturn(new ArrayList<>(List.of(existingGuest)));

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(1L)
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now()) 
                .guests(List.of(GuestRequest.GuestDto.builder().firstname("Elif").lastname("Dogan").build()))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            guestService.updateReservation(voucher, request);
        });

        assertEquals("Check-out tarihi check-in tarihinden sonra olmalıdır.", exception.getMessage());
    }

    @Test
    void updateReservation_WhenUpdateIsSuccessfulWithSameGuestCount_ShouldUpdateAndSave() {
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
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));

        GuestResponse.Creation response = guestService.updateReservation(voucher, request);

        assertNotNull(response);
        assertEquals(2, response.getGuests().size());
        assertEquals("YeniAd1", response.getGuests().get(0).getFirstname());
        assertEquals("YeniAd2", response.getGuests().get(1).getFirstname());

        verify(guestRepository, times(1)).saveAll(anyList());
        verify(guestRepository, never()).deleteAll(anyList());
    }

    @Test
    void isRoomAvailable_WhenGuestCountExceedsCapacity_ShouldThrowIllegalArgumentException(){
        Room mockRoom = new Room();
        mockRoom.setMaxCapacity(2);

        int requestedGuestCount = 3;
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);
        String existingVoucher = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            guestService.isRoomAvailable(mockRoom, requestedGuestCount, checkIn, checkOut, existingVoucher);
        });

        assertTrue(exception.getMessage().contains("maximum capacity is 2"));

    }

    @Test
    void isRoomAvailable_WhenRoomIsEmptyAndDatesAreFree_ShouldReturnTrue() {
        Room mockRoom = new Room();
        mockRoom.setMaxCapacity(4);
        mockRoom.setGuests(new ArrayList<>());

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);

        Boolean result = guestService.isRoomAvailable(mockRoom, 2, checkIn, checkOut, null);

        assertTrue(result); 
    }

    @Test
    void isRoomAvailable_WhenDatesOverlapWithExistingGuest_ShouldReturnFalse() {

        Room mockRoom = new Room();
        mockRoom.setMaxCapacity(4);

        Guest existingGuest = Guest.builder()
            .voucherNumber("VCH-EXISTING")
            .checkInDate(LocalDate.of(2026, 7, 10))
            .checkOutDate(LocalDate.of(2026, 7, 15))
            .build();
    
        mockRoom.setGuests(List.of(existingGuest)); 

    
        LocalDate newCheckIn = LocalDate.of(2026, 7, 12);
        LocalDate newCheckOut = LocalDate.of(2026, 7, 18);

        Boolean result = guestService.isRoomAvailable(mockRoom, 1, newCheckIn, newCheckOut, null);

        assertFalse(result); 
    }

    @Test
    void createGuest_WhenRoomCannotBeFound_ShouldThrowEntityNotFoundException(){

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);

        Long roomId = 999L;

        GuestRequest.GuestDto guestDto = GuestRequest.GuestDto.builder()
                                                            .firstname("Elif")
                                                            .lastname("Dogan")
                                                            .build();
        GuestRequest.Creation request = GuestRequest.Creation.builder()
                                                            .roomId(roomId)
                                                            .checkInDate(checkIn)
                                                            .checkOutDate(checkOut)
                                                            .guests(List.of(guestDto))
                                                            .build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            guestService.createGuest(request);
        });

        assertEquals("Room cannot be found", exception.getMessage());
    }

    @Test 
    void createGuest_WhenCheckOutDateIsBeforeCheckInDate_ShouldThrowIllegalArgumentException(){
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().minusDays(2);

        Room mockRoom = new Room();
        Long roomId = 1L;
        mockRoom.setId(roomId);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(mockRoom));
        
        GuestRequest.GuestDto guestDto = GuestRequest.GuestDto.builder()
                                                            .firstname("Elif")
                                                            .lastname("Dogan")
                                                            .build();
        GuestRequest.Creation request = GuestRequest.Creation.builder()
                                                            .roomId(mockRoom.getId())
                                                            .checkInDate(checkIn)
                                                            .checkOutDate(checkOut)
                                                            .guests(List.of(guestDto))
                                                            .build();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            guestService.createGuest(request);
        });

        assertEquals("Check-out tarihi check-in tarihinden sonra olmalıdır.", exception.getMessage());
    }

    @Test
    void createGuest_WhenGuestCountExceedsCapacity_ShouldThrowIllegalArgumentException(){
        Room mockRoom = new Room();
        mockRoom.setId(1L);
        mockRoom.setMaxCapacity(2);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(mockRoom));

        GuestRequest.GuestDto guest1 = GuestRequest.GuestDto.builder().firstname("Elif").lastname("Doğan").build();
        GuestRequest.GuestDto guest2 = GuestRequest.GuestDto.builder().firstname("Ali").lastname("Yılmaz").build();
        GuestRequest.GuestDto guest3 = GuestRequest.GuestDto.builder().firstname("Ayşe").lastname("Kaya").build();

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(1L)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(2))
                .guests(List.of(guest1, guest2, guest3)) 
                .build();
        

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            guestService.createGuest(request);
        });

        assertTrue(exception.getMessage().contains("maximum capacity is 2"));
    }

    @Test 
    void createGuest_WhenRoomIsNotAvailableOnChoosenDates_ShouldThrowIllegalArgumentException(){


        Guest existingGuest1 = Guest.builder()
            .id(50L)
            .firstname("Ahmet")
            .lastname("Yılmaz")
            .voucherNumber("VCH-EXISTING")
            .checkInDate(LocalDate.of(2026, 7, 10))
            .checkOutDate(LocalDate.of(2026, 7, 15))
            .build();
        Guest existingGuest2 = Guest.builder()
            .id(50L)
            .firstname("Elif")
            .lastname("Dogan")
            .voucherNumber("VCH-EXISTING")
            .checkInDate(LocalDate.of(2026, 7, 10))
            .checkOutDate(LocalDate.of(2026, 7, 15))
            .build();


        Room mockRoom = new Room();
        mockRoom.setId(1L);
        mockRoom.setMaxCapacity(4);
        mockRoom.setGuests(new ArrayList<>(List.of(existingGuest1, existingGuest2)));

        when(roomRepository.findById(1L)).thenReturn(Optional.of(mockRoom));

        GuestRequest.GuestDto guest1 = GuestRequest.GuestDto.builder().firstname("Zeynep").lastname("Doğan").build();
        GuestRequest.GuestDto guest2 = GuestRequest.GuestDto.builder().firstname("AHmet").lastname("Yılmaz").build();

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(1L)
                .checkInDate(LocalDate.of(2026, 7, 14))
                .checkOutDate(LocalDate.of(2026, 7, 17))
                .guests(List.of(guest1, guest2)) 
                .build();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            guestService.createGuest(request);
        });

        assertEquals("The room that chosen is not available between these dates", exception.getMessage());

    }

    @Test 
    void createGuest_WhenCreationIsSuccessfull_ShouldReturnCreationResponse(){
        Room mockRoom = new Room();
        Long roomId = 99L;
        mockRoom.setMaxCapacity(4);
        mockRoom.setId(roomId);
        mockRoom.setGuests(new ArrayList<>());

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));

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

        GuestResponse.Creation response = guestService.createGuest(request);

        assertNotNull(response);
        assertNotNull(response.getVoucherNumber());
        assertTrue(response.getVoucherNumber().startsWith("VCH-")); 
        assertEquals(2, response.getGuests().size());
        verify(guestRepository, times(1)).saveAll(anyList());
    }
        


    
}