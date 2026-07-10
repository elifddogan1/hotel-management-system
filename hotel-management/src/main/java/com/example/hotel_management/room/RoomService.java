package com.example.hotel_management.room;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.hotel_management.common.PagedResponse;
import com.example.hotel_management.guest.Guest;
import com.example.hotel_management.hotel.Hotel;
import com.example.hotel_management.hotel.HotelRepository;
import com.example.hotel_management.room.v1.dto.RoomRequest;
import com.example.hotel_management.room.v1.dto.RoomResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public List<RoomResponse.QueryDetail> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToQueryDetailResponse)
                .toList();
    }

    public List<RoomResponse.QueryDetail> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId).stream()
                .map(this::convertToQueryDetailResponse)
                .toList();
    }

    public RoomResponse.QueryDetail getRoomById(Long roomId) {
        Room room = getRoomEntityById(roomId);
        return convertToQueryDetailResponse(room);
    }

    @Transactional
    public RoomResponse.Creation createRoom(RoomRequest.Creation request) {
        if (roomRepository.existsByRoomNumberAndHotelId(request.getRoomNumber(), request.getHotelId())) {
            throw new IllegalArgumentException("This room already exists in this hotel.");
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

        room = roomRepository.save(room);
        return convertToCreationResponse(room);
    }

    @Transactional
    public RoomResponse.Creation updateRoom(Long roomId, RoomRequest.Creation request) {
        Room room = getRoomEntityById(roomId);

        // Oda numarası değişiyorsa, aynı otelde başka bir oda bu numarayı kullanıyor mu kontrolü
        if (!room.getRoomNumber().equals(request.getRoomNumber())) {
            if (roomRepository.existsByRoomNumberAndHotelId(request.getRoomNumber(), request.getHotelId())) {
                throw new IllegalArgumentException("This room already exists in this hotel.");
            }
        }

        // Kapasite düşürülmek isteniyorsa mevcut rezervasyonları kontrol et
        if (request.getMaxCapacity() < room.getMaxCapacity() && room.getGuests() != null) {
            Map<String, Long> guestsPerVoucher = room.getGuests().stream()
                    .collect(Collectors.groupingBy(Guest::getVoucherNumber, Collectors.counting()));
            
            for (Map.Entry<String, Long> entry : guestsPerVoucher.entrySet()) {
                if (entry.getValue() > request.getMaxCapacity()) {
                    throw new IllegalArgumentException(
                            "Cannot reduce capacity below the number of guests in an existing booking ("
                                    + entry.getValue() + " guests in voucher " + entry.getKey() + ").");
                }
            }
        }

        if (!room.getHotel().getId().equals(request.getHotelId())) {
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Hotel with ID " + request.getHotelId() + " could not be found."));
            room.setHotel(hotel);
        }

        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setMaxCapacity(request.getMaxCapacity());
        
        room = roomRepository.save(room);
        return convertToCreationResponse(room);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
    Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Room with ID " + roomId + " could not be found."));

        if (room.getGuests() != null && !room.getGuests().isEmpty()) {
            throw new IllegalArgumentException("Bu odaya ait aktif rezervasyonlar bulunmaktadır. Önce rezervasyonları iptal etmeli veya başka bir odaya taşımalısınız.");
        }

        roomRepository.delete(room);
    }


    public PagedResponse<RoomResponse.QueryDetail> searchAndSortRooms(RoomRequest.Search request) {
        
        Specification<Room> spec = Specification.where(RoomSpecification.roomNumberFiltering(request.getRoomNumber()))
                .and(RoomSpecification.roomTypeFiltering(request.getRoomType()))
                .and(RoomSpecification.hotelIdFiltering(request.getHotelId()));

        String sortField = (request.getSortBy() != null && !request.getSortBy().trim().isEmpty())
                ? request.getSortBy()
                : "id";
        Sort sort = (request.getDirection() != null && request.getDirection().equalsIgnoreCase("desc"))
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        int pageNum = (request.getPage() != null) ? request.getPage() : 0;
        int pageSize = (request.getSize() != null) ? request.getSize() : 10;
        PageRequest pageable = PageRequest.of(pageNum, pageSize, sort);

        Page<Room> roomPage = roomRepository.findAll(spec, pageable);

        List<RoomResponse.QueryDetail> dtoList = roomPage.getContent().stream()
                .map(this::convertToQueryDetailResponse)
                .toList();

        return PagedResponse.<RoomResponse.QueryDetail>builder()
                .content(dtoList)
                .pageNo(roomPage.getNumber())
                .pageSize(roomPage.getSize())
                .totalElements(roomPage.getTotalElements())
                .totalPages(roomPage.getTotalPages())
                .last(roomPage.isLast())
                .build();
    }

    public List<RoomResponse.QueryDetail> findOptimalRoom(Long hotelId, int numberOfPerson, LocalDate checkInDate,
            LocalDate checkOutDate) {
        List<Room> roomsWithSufficientCapacity = roomRepository.findByHotelIdAndMaxCapacityGreaterThanEqual(
                hotelId, numberOfPerson);

        return filterOptimalRooms(roomsWithSufficientCapacity, checkInDate, checkOutDate);
    }

    public List<RoomResponse.QueryDetail> findOptimalRoomWithoutHotelFilter(int numberOfPerson, LocalDate checkInDate,
            LocalDate checkOutDate) {
        List<Room> roomsWithSufficientCapacity = roomRepository
                .findByMaxCapacityGreaterThanEqual(numberOfPerson);

        return filterOptimalRooms(roomsWithSufficientCapacity, checkInDate, checkOutDate);
    }


    private Room getRoomEntityById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room with ID " + roomId + " could not be found."));
    }

    private List<RoomResponse.QueryDetail> filterOptimalRooms(List<Room> rooms, LocalDate checkInDate, LocalDate checkOutDate) {
        return rooms.stream()
                .filter(room -> room.getGuests() == null || room.getGuests().stream()
                        .noneMatch(guest -> guest.getCheckOutDate().isAfter(checkInDate)
                                && guest.getCheckInDate().isBefore(checkOutDate)))
                .map(this::convertToQueryDetailResponse)
                .toList();
    }

    private RoomResponse.QueryDetail convertToQueryDetailResponse(Room room) {
        return RoomResponse.QueryDetail.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .maxCapacity(room.getMaxCapacity())
                .hotelId(room.getHotel() != null ? room.getHotel().getId() : null)
                .hotelName(room.getHotel() != null ? room.getHotel().getName() : null)
                .build();
    }

    private RoomResponse.Creation convertToCreationResponse(Room room) {
        return RoomResponse.Creation.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .maxCapacity(room.getMaxCapacity())
                .hotelId(room.getHotel() != null ? room.getHotel().getId() : null)
                .build();
    }
}