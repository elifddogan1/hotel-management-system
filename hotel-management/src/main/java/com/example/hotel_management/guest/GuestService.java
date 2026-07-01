package com.example.hotel_management.guest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.hotel_management.guest.request.GuestCreationRequest;
import com.example.hotel_management.room.Room;
import com.example.hotel_management.room.RoomRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GuestService {
        private final GuestRepository guestRepository;
        private final RoomRepository roomRepository;

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

        public Guest createGuest(GuestCreationRequest request) {
                Room room = roomRepository.findById(request.getRoomId())
                                .orElseThrow(() -> new EntityNotFoundException("Room cannot be found"));
                Boolean isAvailable = isRoomAvailable(room, request.getCheckInDate(), request.getCheckOutDate());

                if (isAvailable) {
                        Guest guest = Guest.builder()
                                        .firstname(request.getFirstname())
                                        .lastname(request.getLastname())
                                        .voucherNumber(request.getVoucherNumber())
                                        .room(room)
                                        .checkInDate(request.getCheckInDate())
                                        .checkOutDate(request.getCheckOutDate())
                                        .build();
                        return guestRepository.save(guest);

                } else {
                        throw new IllegalArgumentException(
                                        "The room that choosen is not available between these dates");
                }
        }

        public List<Guest> searchAndSortGuests(String lastname, String voucherNumber, String sortBy, String direction) {
                Specification<Guest> spec = (root, query, cb) -> cb.conjunction();
                if (lastname != null || !lastname.trim().isEmpty()) {
                        spec = spec.and(GuestSpecification.lastnameFiltering(lastname));
                }
                if (voucherNumber != null && !voucherNumber.trim().isEmpty()) {
                        spec = spec.and(GuestSpecification.voucherNumberFiltering(voucherNumber));
                }

                String field = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : "id";
                Sort sort = (direction != null && direction.equalsIgnoreCase("desc")) ? Sort.by(field).descending()
                                : Sort.by(field).ascending();

                return guestRepository.findAll(spec, sort);
        }

        private boolean isRoomAvailable(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
                return room.getGuests().stream()
                                .noneMatch(guest -> guest.getCheckOutDate().isAfter(checkInDate)
                                                && guest.getCheckInDate().isBefore(checkOutDate));
        }
}