package com.example.hotel_management.reservation;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class VoucherGenerator {

    public String generateVoucherCode() {
        return "VCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
