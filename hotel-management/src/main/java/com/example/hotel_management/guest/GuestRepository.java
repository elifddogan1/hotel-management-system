package com.example.hotel_management.guest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hotel_management.guest.request.GuestCreationRequest;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    Guest save(GuestCreationRequest request);
}