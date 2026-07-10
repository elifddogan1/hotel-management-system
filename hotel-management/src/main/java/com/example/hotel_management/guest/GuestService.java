package com.example.hotel_management.guest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.hotel_management.common.PagedResponse;
import com.example.hotel_management.guest.v1.dto.GuestRequest;
import com.example.hotel_management.guest.v1.dto.GuestResponse;
import com.example.hotel_management.room.Room;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GuestService {
        private final GuestRepository guestRepository;
        private final GuestMapper guestMapper;

        public List<GuestResponse.QueryDetail> getGuestsByHotelId(Long hotelId) {
                List<Guest> guests = guestRepository.findByRoom_Hotel_Id(hotelId);

                return guests.stream()
                                .map(guestMapper::toQueryDetail)
                                .toList();
        }

        public List<GuestResponse.QueryDetail> getGuestsByRoomId(Long roomId) {
                List<Guest> guests = guestRepository.findByRoom_Id(roomId);

                return guests.stream()
                                .map(guestMapper::toQueryDetail)
                                .toList();
        }

        @Transactional
        public GuestResponse.Creation createGuestsForReservation(Room room, String voucherNumber, GuestRequest.Creation request) {
        List<Guest> guestsToSave = new ArrayList<>();
        for (GuestRequest.GuestDto guestDto : request.getGuests()) {
            Guest guest = Guest.builder()
                    .firstname(guestDto.getFirstname())
                    .lastname(guestDto.getLastname())
                    .voucherNumber(voucherNumber)
                    .room(room)
                    .checkInDate(request.getCheckInDate())
                    .checkOutDate(request.getCheckOutDate())
                    .build();
            guestsToSave.add(guest);
        }
                guestRepository.saveAll(guestsToSave);
                return guestMapper.toCreationResponse(guestsToSave);
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
        public GuestResponse.Creation updateGuestsForReservation(String voucherNumber, Room newRoom, GuestRequest.Creation request) {
        List<Guest> currentGuests = guestRepository.findByVoucherNumber(voucherNumber);
        if (currentGuests.isEmpty()) {
            throw new EntityNotFoundException("Reservation with voucher " + voucherNumber + " could not be found.");
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
        return guestMapper.toCreationResponse(currentGuests);
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
                                .map(guestMapper::toQueryDetail)
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

}