package com.example.hotel_management.room;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);

    List<Room> findByHotelIdAndMaxCapacityGreaterThanEqual(Long hotelId, int capacity);

    List<Room> findByMaxCapacityGreaterThanEqual(int capacity);

    boolean isExistsByIdAndHotelId(Long roomId, Long hotelId);

    boolean isExistsByRoomNumberAndHotelId(String roomNumber, Long hotelId);

}