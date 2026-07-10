package com.example.hotel_management.guest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.hotel_management.common.PagedResponse;
import com.example.hotel_management.guest.v1.dto.GuestRequest;
import com.example.hotel_management.guest.v1.dto.GuestResponse;
import com.example.hotel_management.room.Room;
import com.example.hotel_management.room.RoomRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GuestService {
        private final GuestRepository guestRepository;
        private final RoomRepository roomRepository;

        public List<GuestResponse.QueryDetail> getGuestsByHotelId(Long hotelId) {
                List<Guest> guests = guestRepository.findByRoom_Hotel_Id(hotelId);

                return guests.stream()
                                .map(this::convertToQueryDetailResponse)
                                .toList();
        }

        public List<GuestResponse.QueryDetail> getGuestsByRoomId(Long roomId) {
                List<Guest> guests = guestRepository.findByRoom_Id(roomId);

                return guests.stream()
                                .map(this::convertToQueryDetailResponse)
                                .toList();
        }

        @Transactional
        public GuestResponse.Creation createGuest(GuestRequest.Creation request) {
                Room room = roomRepository.findById(request.getRoomId())
                                .orElseThrow(() -> new EntityNotFoundException("Room cannot be found"));
                if (!request.getCheckInDate().isBefore(request.getCheckOutDate())) {
                        throw new IllegalArgumentException("Check-out tarihi check-in tarihinden sonra olmalıdır.");
                }

                Boolean isAvailable = isRoomAvailable(room, request.getGuests().size(),
                                request.getCheckInDate(), request.getCheckOutDate(), null);

                if (!isAvailable) {
                        throw new IllegalArgumentException("The room that chosen is not available between these dates");
                }

                String generatedVoucher = "VCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                List<Guest> guestsToSave = new ArrayList<>();
                for (GuestRequest.GuestDto guestDto : request.getGuests()) {
                        Guest guest = Guest.builder()
                                        .firstname(guestDto.getFirstname())
                                        .lastname(guestDto.getLastname())
                                        .voucherNumber(generatedVoucher)
                                        .room(room)
                                        .checkInDate(request.getCheckInDate())
                                        .checkOutDate(request.getCheckOutDate())
                                        .build();
                        guestsToSave.add(guest);
                }

                guestRepository.saveAll(guestsToSave);
                return convertToSingleVoucherResponse(guestsToSave);
        }

        public void deleteGuest(Long guestId) {
                boolean exists = guestRepository.existsById(guestId);

                if (!exists) {
                        throw new EntityNotFoundException("Reservation with ID " + guestId + " could not be found.");
                }

                guestRepository.deleteById(guestId);
        }

        public void cancelReservation(String voucherNumber) {
                List<Guest> guests = guestRepository.findByVoucherNumber(voucherNumber);
                if (guests.isEmpty()) {
                        throw new EntityNotFoundException(
                                        "Reservation with ID " + voucherNumber + " could not be found.");
                }
                guestRepository.deleteAll(guests);
        }

        @Transactional
        public GuestResponse.Creation updateReservation(String voucherNumber, GuestRequest.Creation request) {
                List<Guest> currentGuests = guestRepository.findByVoucherNumber(voucherNumber);
                if (currentGuests.isEmpty()) {
                        throw new EntityNotFoundException(
                                        "Reservation with voucher " + voucherNumber + " could not be found.");
                }

                if (!request.getCheckInDate().isBefore(request.getCheckOutDate()) || request.getCheckInDate().equals(request.getCheckOutDate())) {
                        throw new IllegalArgumentException("Check-out tarihi check-in tarihinden sonra olmalıdır.");
                }

                Room newRoom = roomRepository.findById(request.getRoomId())
                                .orElseThrow(() -> new EntityNotFoundException("Room cannot be found"));

                Boolean isAvailable = isRoomAvailable(newRoom, request.getGuests().size(),
                                request.getCheckInDate(), request.getCheckOutDate(), voucherNumber);

                if (!isAvailable) {
                        throw new IllegalArgumentException("The room is not available between these dates");
                }

                int currentSize = currentGuests.size();
                int requestSize = request.getGuests().size();
                int minSize = Math.min(currentSize, requestSize);

                for (int i = 0; i < minSize; i++) {
                        Guest existingGuest = currentGuests.get(i);
                        GuestRequest.GuestDto dto = request.getGuests().get(i);

                        existingGuest.setFirstname(dto.getFirstname());
                        existingGuest.setLastname(dto.getLastname());
                        existingGuest.setRoom(newRoom);
                        existingGuest.setCheckInDate(request.getCheckInDate());
                        existingGuest.setCheckOutDate(request.getCheckOutDate());
                }

                if (requestSize > currentSize) {
                        for (int i = minSize; i < requestSize; i++) {
                                GuestRequest.GuestDto dto = request.getGuests().get(i);
                                Guest newGuest = Guest.builder()
                                                .firstname(dto.getFirstname())
                                                .lastname(dto.getLastname())
                                                .voucherNumber(voucherNumber)
                                                .room(newRoom)
                                                .checkInDate(request.getCheckInDate())
                                                .checkOutDate(request.getCheckOutDate())
                                                .build();
                                currentGuests.add(newGuest);
                        }
                }

                if (currentSize > requestSize) {
                        List<Guest> guestsToRemove = new ArrayList<>(currentGuests.subList(minSize, currentSize));
                        guestRepository.deleteAll(guestsToRemove);
                        currentGuests.removeAll(guestsToRemove);
                }
                guestRepository.saveAll(currentGuests);
                return convertToSingleVoucherResponse(currentGuests);
        }

        public PagedResponse<GuestResponse.QueryDetail> searchAndSortGuests(GuestRequest.Search request) {

                Specification<Guest> spec = Specification.where(GuestSpecification.firstnameFiltering(request.getFirstname()))
                                .and(GuestSpecification.lastnameFiltering(request.getLastname()))
                                .and(GuestSpecification.voucherNumberFiltering(request.getVoucherNumber()))
                                .and(GuestSpecification.roomNumberFiltering(request.getRoomNumber()))
                                .and(GuestSpecification.hotelNameFiltering(request.getHotelName()));

                String sortField = (request.getSortBy() != null && !request.getSortBy().trim().isEmpty())
                                ? request.getSortBy()
                                : "id";
                Sort sort = (request.getDirection() != null && request.getDirection().equalsIgnoreCase("desc"))
                                ? Sort.by(sortField).descending()
                                : Sort.by(sortField).ascending();

                int pageNum = (request.getPage() != null) ? request.getPage() : 0;
                int pageSize = (request.getSize() != null) ? request.getSize() : 10;
                PageRequest pageable = PageRequest.of(pageNum, pageSize, sort);

                Page<Guest> guestPage = guestRepository.findAll(spec, pageable);

                List<GuestResponse.QueryDetail> dtoList = guestPage.getContent().stream()
                                .map(guest -> GuestResponse.QueryDetail.builder()
                                                .id(guest.getId())
                                                .firstname(guest.getFirstname())
                                                .lastname(guest.getLastname())
                                                .voucherNumber(guest.getVoucherNumber())
                                                .roomNumber(guest.getRoom() != null ? guest.getRoom().getRoomNumber()
                                                                : null)
                                                .hotelName((guest.getRoom() != null
                                                                && guest.getRoom().getHotel() != null)
                                                                                ? guest.getRoom().getHotel().getName()
                                                                                : null)
                                                .checkInDate(guest.getCheckInDate())
                                                .checkOutDate(guest.getCheckOutDate())
                                                .build())
                                .toList();

                return PagedResponse.<GuestResponse.QueryDetail>builder()
                                .content(dtoList)
                                .pageNo(guestPage.getNumber())
                                .pageSize(guestPage.getSize())
                                .totalElements(guestPage.getTotalElements())
                                .totalPages(guestPage.getTotalPages())
                                .last(guestPage.isLast())
                                .build();
        }

        public Boolean isRoomAvailable(Room room, int guestCount, LocalDate checkIn, LocalDate checkOut,
                        String ignoreVoucherNumber) {
                if (room.getMaxCapacity() < guestCount) {
                        throw new IllegalArgumentException("The room's maximum capacity is " + room.getMaxCapacity()
                                        + ", but " + guestCount + " guests were requested.");
                }
                return room.getGuests().stream()
                                .filter(guest -> ignoreVoucherNumber == null
                                                || !guest.getVoucherNumber().equals(ignoreVoucherNumber))
                                .noneMatch(guest -> guest.getCheckOutDate().isAfter(checkIn)
                                                && guest.getCheckInDate().isBefore(checkOut));
        }

        private GuestResponse.Creation convertToSingleVoucherResponse(List<Guest> guests) {
                if (guests == null || guests.isEmpty()) {
                        return null;
                }

                Guest firstGuest = guests.get(0);

                List<GuestResponse.GuestDetail> guestDetails = guests.stream()
                                .map(this::convertToDetailResponse)
                                .toList();

                return GuestResponse.Creation.builder()
                                .voucherNumber(firstGuest.getVoucherNumber())
                                .checkInDate(firstGuest.getCheckInDate())
                                .checkOutDate(firstGuest.getCheckOutDate())
                                .roomId(firstGuest.getRoom() != null ? firstGuest.getRoom().getId() : null)
                                .guests(guestDetails)
                                .build();
        }

        private GuestResponse.GuestDetail convertToDetailResponse(Guest guest) {

                return GuestResponse.GuestDetail.builder()

                                .id(guest.getId())

                                .firstname(guest.getFirstname())

                                .lastname(guest.getLastname())

                                .build();

        }

        private GuestResponse.QueryDetail convertToQueryDetailResponse(Guest guest) {
                return GuestResponse.QueryDetail.builder()
                                .id(guest.getId())
                                .firstname(guest.getFirstname())
                                .lastname(guest.getLastname())
                                .voucherNumber(guest.getVoucherNumber())
                                .roomNumber(guest.getRoom() != null ? guest.getRoom().getRoomNumber() : null)
                                .hotelName((guest.getRoom() != null && guest.getRoom().getHotel() != null)
                                                ? guest.getRoom().getHotel().getName()
                                                : null)
                                .checkInDate(guest.getCheckInDate())
                                .checkOutDate(guest.getCheckOutDate())
                                .build();
        }

}