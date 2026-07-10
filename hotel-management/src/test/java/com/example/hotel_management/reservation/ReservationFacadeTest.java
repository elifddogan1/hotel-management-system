package com.example.hotel_management.reservation;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.hotel_management.guest.GuestService;
import com.example.hotel_management.guest.v1.dto.GuestRequest;
import com.example.hotel_management.guest.v1.dto.GuestResponse;
import com.example.hotel_management.room.Room;
import com.example.hotel_management.room.RoomService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservationFacadeTest {

    @Mock
    private GuestService guestService;

    @Mock
    private RoomService roomService;

    @org.mockito.Spy
    private VoucherGenerator voucherGenerator = new VoucherGenerator();

    @InjectMocks
    private ReservationFacade reservationFacade;

    @Test
    void createReservation_WhenCheckOutDateIsBeforeCheckInDate_ShouldThrowIllegalArgumentException() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().minusDays(2);

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guests(List.of(GuestRequest.GuestDto.builder().firstname("Elif").lastname("Dogan").build()))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationFacade.createReservation(request);
        });

        assertEquals("Check-out tarihi check-in tarihinden sonra olmalıdır.", exception.getMessage());
        verify(roomService, never()).getRoomEntityById(any());
    }

    @Test
    void createReservation_WhenRoomCannotBeFound_ShouldThrowEntityNotFoundException() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);
        Long roomId = 999L;

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(roomId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guests(List.of(GuestRequest.GuestDto.builder().firstname("Elif").lastname("Dogan").build()))
                .build();

        when(roomService.getRoomEntityById(roomId)).thenThrow(new EntityNotFoundException("Room with ID 999 could not be found."));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            reservationFacade.createReservation(request);
        });

        assertEquals("Room with ID 999 could not be found.", exception.getMessage());
        verify(roomService, never()).isRoomAvailable(any(), anyInt(), any(), any(), any());
    }

    @Test
    void createReservation_WhenRoomIsNotAvailable_ShouldThrowIllegalArgumentException() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);
        Long roomId = 1L;
        Room mockRoom = new Room();

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(roomId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guests(List.of(GuestRequest.GuestDto.builder().firstname("Elif").lastname("Dogan").build()))
                .build();

        when(roomService.getRoomEntityById(roomId)).thenReturn(mockRoom);
        when(roomService.isRoomAvailable(eq(mockRoom), anyInt(), eq(checkIn), eq(checkOut), any())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationFacade.createReservation(request);
        });

        assertEquals("Seçilen oda bu tarihler arasında müsait değil.", exception.getMessage());
        verify(guestService, never()).createGuestsForReservation(any(), anyString(), any());
    }

    @Test
    void createReservation_WhenSuccessful_ShouldReturnResponse() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);
        Long roomId = 1L;
        Room mockRoom = new Room();

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(roomId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guests(List.of(GuestRequest.GuestDto.builder().firstname("Elif").lastname("Dogan").build()))
                .build();

        GuestResponse.Creation mockResponse = GuestResponse.Creation.builder()
                .voucherNumber("VCH-TEST")
                .build();

        when(roomService.getRoomEntityById(roomId)).thenReturn(mockRoom);
        when(roomService.isRoomAvailable(eq(mockRoom), anyInt(), eq(checkIn), eq(checkOut), any())).thenReturn(true);
        when(guestService.createGuestsForReservation(eq(mockRoom), anyString(), eq(request))).thenReturn(mockResponse);

        GuestResponse.Creation result = reservationFacade.createReservation(request);

        assertNotNull(result);
        assertEquals("VCH-TEST", result.getVoucherNumber());
        verify(guestService, times(1)).createGuestsForReservation(eq(mockRoom), anyString(), eq(request));
    }

    @Test
    void updateReservation_WhenCheckOutDateIsBeforeCheckInDate_ShouldThrowIllegalArgumentException() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().minusDays(2);

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guests(List.of(GuestRequest.GuestDto.builder().firstname("Elif").lastname("Dogan").build()))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationFacade.updateReservation("VCH-TEST", request);
        });

        assertEquals("Check-out tarihi check-in tarihinden sonra olmalıdır.", exception.getMessage());
        verify(roomService, never()).getRoomEntityById(any());
    }

    @Test
    void updateReservation_WhenRoomCannotBeFound_ShouldThrowEntityNotFoundException() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);
        Long roomId = 999L;

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(roomId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guests(List.of(GuestRequest.GuestDto.builder().firstname("Elif").lastname("Dogan").build()))
                .build();

        when(roomService.getRoomEntityById(roomId)).thenThrow(new EntityNotFoundException("Room with ID 999 could not be found."));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            reservationFacade.updateReservation("VCH-TEST", request);
        });

        assertEquals("Room with ID 999 could not be found.", exception.getMessage());
        verify(roomService, never()).isRoomAvailable(any(), anyInt(), any(), any(), any());
    }

    @Test
    void updateReservation_WhenRoomIsNotAvailable_ShouldThrowIllegalArgumentException() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);
        Long roomId = 1L;
        Room mockRoom = new Room();

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(roomId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guests(List.of(GuestRequest.GuestDto.builder().firstname("Elif").lastname("Dogan").build()))
                .build();

        when(roomService.getRoomEntityById(roomId)).thenReturn(mockRoom);
        when(roomService.isRoomAvailable(eq(mockRoom), anyInt(), eq(checkIn), eq(checkOut), eq("VCH-TEST"))).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationFacade.updateReservation("VCH-TEST", request);
        });

        assertEquals("Oda bu tarihler arasında müsait değil.", exception.getMessage());
        verify(guestService, never()).updateGuestsForReservation(any(), any(), any());
    }

    @Test
    void updateReservation_WhenSuccessful_ShouldReturnResponse() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);
        Long roomId = 1L;
        Room mockRoom = new Room();

        GuestRequest.Creation request = GuestRequest.Creation.builder()
                .roomId(roomId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guests(List.of(GuestRequest.GuestDto.builder().firstname("Elif").lastname("Dogan").build()))
                .build();

        GuestResponse.Creation mockResponse = GuestResponse.Creation.builder()
                .voucherNumber("VCH-TEST")
                .build();

        when(roomService.getRoomEntityById(roomId)).thenReturn(mockRoom);
        when(roomService.isRoomAvailable(eq(mockRoom), anyInt(), eq(checkIn), eq(checkOut), eq("VCH-TEST"))).thenReturn(true);
        when(guestService.updateGuestsForReservation(eq("VCH-TEST"), eq(mockRoom), eq(request))).thenReturn(mockResponse);

        GuestResponse.Creation result = reservationFacade.updateReservation("VCH-TEST", request);

        assertNotNull(result);
        assertEquals("VCH-TEST", result.getVoucherNumber());
        verify(guestService, times(1)).updateGuestsForReservation(eq("VCH-TEST"), eq(mockRoom), eq(request));
    }
}
