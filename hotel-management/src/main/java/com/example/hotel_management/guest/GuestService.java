package com.example.hotel_management.guest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.hotel_management.guest.request.GuestCreationRequest;
import com.example.hotel_management.guest.request.GuestDto;
import com.example.hotel_management.room.Room;
import com.example.hotel_management.room.RoomRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GuestService {
        private final GuestRepository guestRepository;
        private final RoomRepository roomRepository;

        public List<Guest> getAllGuests() {
                return guestRepository.findAll();
        }

        public List<Guest> getGuestsByHotelId(Long hotelId) {
                return guestRepository.findByRoomHotelId(hotelId);
        }

        public List<Guest> getGuestsByRoomId(Long roomId) {
                return guestRepository.findByRoomId(roomId);
        }

        public List<Guest> createGuest(GuestCreationRequest request) {
                Room room = roomRepository.findById(request.getRoomId())
                                .orElseThrow(() -> new EntityNotFoundException("Room cannot be found"));
                Boolean isAvailable = isRoomAvailable(room, request.getGuests().size(), request.getCheckInDate(),
                                request.getCheckOutDate());

                if (isAvailable) {
                        List<Guest> guestsToSave = new ArrayList<>();

                        for (GuestDto guestDto : request.getGuests()) {
                                Guest guest = Guest.builder()
                                                .firstname(guestDto.getFirstname())
                                                .lastname(guestDto.getLastname())
                                                .voucherNumber(request.getVoucherNumber())
                                                .room(room)
                                                .checkInDate(request.getCheckInDate())
                                                .checkOutDate(request.getCheckOutDate())
                                                .build();

                                guestsToSave.add(guest);
                        }
                        return guestRepository.saveAll(guestsToSave);

                } else {
                        throw new IllegalArgumentException(
                                        "The room that choosen is not available between these dates");
                }
        }

        public void deleteGuest(Long guestId) {
                boolean exists = guestRepository.existsById(guestId);

                if (!exists) {
                        throw new EntityNotFoundException("Reservation with ID " + guestId + " could not be found.");
                }

                guestRepository.deleteById(guestId);
        }

        public void cancelReservation(String voucherNumber) {
                boolean exists = guestRepository.existsByVoucherNumber(voucherNumber);
                if (!exists) {
                        throw new EntityNotFoundException(
                                        "Reservation with ID " + voucherNumber + " could not be found.");
                }

                List<Guest> guests = guestRepository.findByVoucherNumber(voucherNumber);

                guestRepository.deleteAll(guests);

        }

        public List<Guest> updateReservation(String voucherNumber, GuestCreationRequest request) {
                List<Guest> currentGuests = guestRepository.findByVoucherNumber(voucherNumber);
                if (currentGuests.isEmpty()) {
                        throw new EntityNotFoundException("Reservation with voucher " + voucherNumber + " could not be found.");
                }

                Room newRoom = roomRepository.findById(request.getRoomId())
                                .orElseThrow(() -> new EntityNotFoundException("Room cannot be found"));

                if (newRoom.getMaxCapacity() < request.getGuests().size()) {
                        throw new IllegalArgumentException("The room's maximum capacity is " + newRoom.getMaxCapacity() + ", but " + request.getGuests().size() + " guests were requested.");
                }

                // Check availability (ignoring this voucher's own bookings)
                boolean isAvailable = newRoom.getGuests().stream()
                                .filter(guest -> !guest.getVoucherNumber().equals(voucherNumber))
                                .noneMatch(guest -> guest.getCheckOutDate().isAfter(request.getCheckInDate())
                                                && guest.getCheckInDate().isBefore(request.getCheckOutDate()));

                if (!isAvailable) {
                        throw new IllegalArgumentException("The room is not available between these dates");
                }

                // Delete old guests and insert new ones
                guestRepository.deleteAll(currentGuests);

                List<Guest> guestsToSave = new ArrayList<>();
                for (GuestDto guestDto : request.getGuests()) {
                        Guest guest = Guest.builder()
                                        .firstname(guestDto.getFirstname())
                                        .lastname(guestDto.getLastname())
                                        .voucherNumber(voucherNumber)
                                        .room(newRoom)
                                        .checkInDate(request.getCheckInDate())
                                        .checkOutDate(request.getCheckOutDate())
                                        .build();
                        guestsToSave.add(guest);
                }

                return guestRepository.saveAll(guestsToSave);
        }

        public List<Guest> searchAndSortGuests(String lastname, String voucherNumber, String sortBy, String direction) {
                Specification<Guest> spec = (root, query, cb) -> cb.conjunction();
                if (lastname != null && !lastname.trim().isEmpty()) {
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

        private boolean isRoomAvailable(Room room, int maxCapacity, LocalDate checkInDate, LocalDate checkOutDate) {
                if (room.getMaxCapacity() < maxCapacity)
                        return false;
                return room.getGuests().stream()
                                .noneMatch(guest -> guest.getCheckOutDate().isAfter(checkInDate)
                                                && guest.getCheckInDate().isBefore(checkOutDate));
        }

}