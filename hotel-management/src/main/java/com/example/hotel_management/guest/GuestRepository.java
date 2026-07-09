package com.example.hotel_management.guest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long>, JpaSpecificationExecutor<Guest> {

    boolean existsByVoucherNumber(String voucherNumber);

    List<Guest> findByVoucherNumber(String voucherNumber);

    @Query("SELECT g FROM Guest g WHERE g.room.hotel.id = :hotelId")
    List<Guest> findByRoomHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT g FROM Guest g WHERE g.room.id = :roomId")
    List<Guest> findByRoomId(@Param("roomId") Long roomId);

    List<Guest> findByRoomIdOrderByCheckInDateAsc(Long roomId);

    List<Guest> findByRoom_Hotel_Id(Long hotelId);

    List<Guest> findByRoom_Id(Long roomId);

}