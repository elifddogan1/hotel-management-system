package com.example.hotel_management.guest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long>, JpaSpecificationExecutor<Guest> {

    boolean existsByVoucherNumber(Long voucherNumber);

    List<Guest> findByVoucherNumber(Long voucherNumber);
}