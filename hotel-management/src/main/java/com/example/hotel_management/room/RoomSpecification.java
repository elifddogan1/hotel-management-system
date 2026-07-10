package com.example.hotel_management.room;

import org.springframework.data.jpa.domain.Specification;

public class RoomSpecification {

    private RoomSpecification() {
    }

    public static Specification<Room> roomNumberFiltering(String roomNumber) {
        return (root, query, criteriaBuilder) -> {
            if (roomNumber == null || roomNumber.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("roomNumber")),
                    "%" + roomNumber.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Room> roomTypeFiltering(String roomType) {
        return (root, query, criteriaBuilder) -> {
            if (roomType == null || roomType.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("roomType")),
                    "%" + roomType.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Room> hotelIdFiltering(Long hotelId) {
        return (root, query, criteriaBuilder) -> {
            if (hotelId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("hotel").get("id"), hotelId);
        };
    }
}