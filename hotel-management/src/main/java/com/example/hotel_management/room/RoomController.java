package com.example.hotel_management.room;

import java.time.LocalDate;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.hotel_management.common.PagedResponse;
import com.example.hotel_management.room.dto.RoomRequest;
import com.example.hotel_management.room.dto.RoomResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // Sayfalamalı ve filtrelemeli ana arama endpoint'i
    @GetMapping
    public PagedResponse<RoomResponse.QueryDetail> getRooms(
            @Valid @ParameterObject @ModelAttribute RoomRequest.Search search) {
        return roomService.searchAndSortRooms(search);
    }

    // Sayfalama olmadan tüm odaları getirmek için
    @GetMapping("/all")
    public List<RoomResponse.QueryDetail> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/hotel/{hotelId}")
    public List<RoomResponse.QueryDetail> getRoomsByHotelId(@PathVariable Long hotelId) {
        return roomService.getRoomsByHotelId(hotelId);
    }

    @GetMapping("/{roomId}")
    public RoomResponse.QueryDetail getRoomById(@PathVariable Long roomId) {
        return roomService.getRoomById(roomId);
    }

    @GetMapping("/available")
    public List<RoomResponse.QueryDetail> getAvailableRooms(
            @RequestParam Long hotelId, 
            @RequestParam int numberOfPerson,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate) {
        return roomService.findOptimalRoom(hotelId, numberOfPerson, checkInDate, checkOutDate);
    }

    @GetMapping("/available-all")
    public List<RoomResponse.QueryDetail> getAvailableRoomsWithoutHotelFilter(
            @RequestParam int numberOfPerson,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate) {
        return roomService.findOptimalRoomWithoutHotelFilter(numberOfPerson, checkInDate, checkOutDate);
    }

    // GuestController'daki "/create" yapısına uygun hale getirildi. 
    // hotelId zaten RoomRequest.Creation DTO'sunun içinde olduğu için PathVariable kaldırıldı.
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse.Creation createRoom(@Valid @RequestBody RoomRequest.Creation request) {
        return roomService.createRoom(request);
    }

    @PutMapping("/{roomId}")
    public RoomResponse.Creation updateRoom(
            @PathVariable Long roomId, 
            @Valid @RequestBody RoomRequest.Creation request) {
        return roomService.updateRoom(roomId, request);
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
    }
}