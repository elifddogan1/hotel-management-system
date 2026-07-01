package com.example.hotel_management.guest;

import org.springframework.data.jpa.domain.Specification;

public class GuestSpecification {
    public static Specification<Guest> lastnameFiltering(String searchedLastname) {

        return (root, query, cb) -> {
            if (searchedLastname == null || searchedLastname.trim().isEmpty()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("lastname")),
                    "%" + searchedLastname.toLowerCase() + "%");

        };
    }

    public static Specification<Guest> voucherNumberFiltering(String searchedVoucherNumber) {
        return (root, query, cb) -> {
            if (searchedVoucherNumber == null || searchedVoucherNumber.trim().isEmpty()) {
                return cb.conjunction();
            }

            return cb.equal(
                    cb.lower(root.get("voucherNumber")),
                    searchedVoucherNumber.toLowerCase());
        };
    }
}