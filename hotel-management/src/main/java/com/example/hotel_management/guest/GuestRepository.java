package com.example.hotel_management.guest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long>, JpaSpecificationExecutor<Guest> {

    boolean existsByVoucherNumber(Long voucherNumber);

    List<Guest> findByVoucherNumber(Long voucherNumber);

    @Query("SELECT g FROM Guest g WHERE g.room.hotel.id = :hotelId")
    List<Guest> findByRoomHotelId(@Param("hotelId") Long hotelId);

    // 🔥 Odaya göre misafirleri garanti altına alan JPQL sorgusu
    @Query("SELECT g FROM Guest g WHERE g.room.id = :roomId")
    List<Guest> findByRoomId(@Param("roomId") Long roomId);

    List<Guest> findByRoomIdOrderByCheckInDateAsc(Long roomId);

}