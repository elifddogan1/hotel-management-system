package com.example.hotel_management.room;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.hotel_management.room.request.RoomCreationRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/all")
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/hotel/{hotelId}")
    public List<Room> getRoomsByHotelId(@PathVariable Long hotelId) {
        return roomService.getRoomsByHotelId(hotelId);
    }

    @GetMapping("/{roomId}")
    public Room getRoomById(@PathVariable Long roomId) {
        return roomService.getRoomById(roomId);
    }

    @GetMapping
    public List<Room> getAvailableRooms(@RequestParam Long hotelId, @RequestParam int numberOfPerson,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate) {
        return roomService.findOptimalRoom(hotelId, numberOfPerson, checkInDate, checkOutDate);
    }

    @GetMapping("/available-all")
    public List<Room> getAvailableRoomsWithoutHotelFilter(@RequestParam int numberOfPerson,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate) {
        return roomService.findOptimalRoomWithoutHotelFilter(numberOfPerson, checkInDate, checkOutDate);
    }

    @PostMapping("/hotel/{hotelId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Room createRoom(@PathVariable Long hotelId, @RequestBody RoomCreationRequest request) {
        request.setHotelId(hotelId);
        return roomService.createRoom(request);
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
    }

    @PutMapping("/{roomId}")
    public Room updateRoom(@PathVariable Long roomId, @RequestBody RoomCreationRequest request) {
        return roomService.updateRoom(roomId, request);
    }

}