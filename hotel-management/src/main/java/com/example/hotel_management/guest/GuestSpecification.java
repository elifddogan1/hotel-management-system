package com.example.hotel_management.guest;

import org.springframework.data.jpa.domain.Specification;

import com.example.hotel_management.hotel.Hotel;
import com.example.hotel_management.room.Room;

import jakarta.persistence.criteria.Join;

public class GuestSpecification {

    public static Specification<Guest> firstnameFiltering(String firstname) {
        return (root, query, cb) -> {
            if (firstname == null || firstname.trim().isEmpty()) return cb.conjunction();
            return cb.like(cb.lower(root.get("firstname")), "%" + firstname.toLowerCase() + "%");
        };
    }

    public static Specification<Guest> lastnameFiltering(String lastname) {
        return (root, query, cb) -> {
            if (lastname == null || lastname.trim().isEmpty()) return cb.conjunction();
            return cb.like(cb.lower(root.get("lastname")), "%" + lastname.toLowerCase() + "%");
        };
    }

    public static Specification<Guest> voucherNumberFiltering(String voucherNumber) {
        return (root, query, cb) -> {
            if (voucherNumber == null || voucherNumber.trim().isEmpty()) return cb.conjunction();
            return cb.equal(root.get("voucherNumber"), voucherNumber);
        };
    }

    public static Specification<Guest> roomNumberFiltering(String roomNumber) {
        return (root, query, cb) -> {
            if (roomNumber == null || roomNumber.trim().isEmpty())
                return cb.conjunction();
            Join<Guest, Room> roomJoin = root.join("room");
            return cb.equal(roomJoin.get("roomNumber"), roomNumber);
        };
    }

    public static Specification<Guest> hotelNameFiltering(String hotelName) {
        return (root, query, cb) -> {
            if (hotelName == null || hotelName.trim().isEmpty())
                return cb.conjunction();
            Join<Guest, Room> roomJoin = root.join("room");
            Join<Room, Hotel> hotelJoin = roomJoin.join("hotel");
            return cb.like(cb.lower(hotelJoin.get("name")), "%" + hotelName.toLowerCase() + "%");
        };
    }
}