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

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room with ID" + roomId + "couldn't be found"));
    }

    public Room createRoom(RoomCreationRequest request) {
        // Sadece oda numarası kontrolü yeterli (otel zaten URL'den geldiği için
        // güvenilir)
        if (roomRepository.existsByRoomNumberAndHotelId(request.getRoomNumber(), request.getHotelId())) {
            throw new IllegalArgumentException("This room already exists in this hotel.");
        }

        // Oteli çekiyoruz (Otel id'si geçersizse EntityNotFoundException fırlatılır)
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Hotel with ID " + request.getHotelId() + " could not be found."));

        // Odayı oluşturuyoruz
        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .roomType(request.getRoomType())
                .maxCapacity(request.getMaxCapacity())
                .hotel(hotel)
                .build();

        // Veritabanına kaydedip geri dönüyoruz
        return roomRepository.save(room);
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

}