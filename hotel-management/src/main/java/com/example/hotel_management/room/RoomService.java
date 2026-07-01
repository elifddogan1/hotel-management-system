package com.example.hotel_management.room;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.hotel_management.hotel.Hotel;
import com.example.hotel_management.hotel.HotelRepository;
import com.example.hotel_management.room.request.RoomCreationRequest;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public Room createRoom(RoomCreationRequest request) {
        if (isRoomExists(request.getRoomNumber(), request.getHotelId())) {
            throw new IllegalArgumentException("This room is exist");
        }

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Hotel with ID " + request.getHotelId() + " could not be found."));

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .roomType(request.getRoomType())
                .maxCapacity(request.getMaxCapacity())
                .hotel(hotel)
                .build();
        return room;

    }

    public void deleteRoom(Long roomId) {
        if (!roomRepository.existsById(roomId))
            throw new EntityNotFoundException("Room with ID" + roomId + "couldn't be found");

        roomRepository.deleteById(roomId);
    }

    public List<Room> findOptimalRoom(Long hotelId, int numberOfPerson, LocalDate checkInDate,
            LocalDate checkOutDate) {
        List<Room> roomsWithSufficientCapacity = roomRepository.findByHotelIdAndMaxCapacityGreaterThanEqual(
                hotelId,
                numberOfPerson);

        List<Room> roomsWithOptimalDate = roomsWithSufficientCapacity.stream()
                .filter(room -> room.getGuests().stream()
                        .noneMatch(guest -> guest.getCheckOutDate().isAfter(checkInDate)
                                && guest.getCheckInDate().isBefore(checkOutDate)))
                .toList();

        return roomsWithOptimalDate;
    }

    public List<Room> findOptimalRoomWithoutHotelFilter(int numberOfPerson, LocalDate checkInDate,
            LocalDate checkOutDate) {
        List<Room> roomsWithSufficientCapacity = roomRepository
                .findByMaxCapacityGreaterThanEqual(numberOfPerson);

        List<Room> roomsWithOptimalDate = roomsWithSufficientCapacity.stream()
                .filter(room -> room.getGuests().stream()
                        .noneMatch(guest -> guest.getCheckOutDate().isAfter(checkInDate)
                                && guest.getCheckInDate().isBefore(checkOutDate)))
                .toList();

        return roomsWithOptimalDate;

    }

    private boolean isRoomExists(String roomNumber, Long hotelId) {
        if (!hotelRepository.existsById(hotelId))
            throw new IllegalArgumentException("Hotel cannot be found with hotel ID:" + hotelId);
        return roomRepository.existsByRoomNumberAndHotelId(roomNumber, hotelId);
    }
}