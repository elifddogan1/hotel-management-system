package com.example.hotel_management.reservation.v1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.hotel_management.common.GlobalExceptionHandler;
import com.example.hotel_management.guest.v1.dto.GuestRequest;
import com.example.hotel_management.guest.v1.dto.GuestResponse;
import com.example.hotel_management.reservation.ReservationFacade;

@WebMvcTest({ReservationController.class, GlobalExceptionHandler.class})
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationFacade reservationFacade;

    @Test
    @DisplayName("Should return 201 Created and the created reservation details with valid payload")
    void createReservation_WithValidPayload_ShouldReturn201() throws Exception {
        GuestResponse.Creation mockResponse = GuestResponse.Creation.builder()
                .voucherNumber("VCH-12345678")
                .roomId(1L)
                .build();

        when(reservationFacade.createReservation(any(GuestRequest.Creation.class))).thenReturn(mockResponse);

        String jsonPayload = """
                {
                    "roomId": 1,
                    "checkInDate": "2026-08-01",
                    "checkOutDate": "2026-08-05",
                    "guests": [
                        { "firstname": "John", "lastname": "Doe" }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voucherNumber").value("VCH-12345678"))
                .andExpect(jsonPath("$.roomId").value(1));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is missing or malformed")
    void createReservation_WithMalformedJson_ShouldReturn400() throws Exception {
        String malformedJson = "{ invalid_json_format }";

        mockMvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 Not Found when room does not exist")
    void createReservation_WithNonExistentRoom_ShouldReturn404() throws Exception {
        when(reservationFacade.createReservation(any(GuestRequest.Creation.class)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Room cannot be found"));

        String jsonPayload = """
                {
                    "roomId": 999,
                    "checkInDate": "2026-08-01",
                    "checkOutDate": "2026-08-05",
                    "guests": [
                        { "firstname": "Jane", "lastname": "Smith" }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 OK and updated reservation details with valid payload")
    void updateReservation_WithValidPayload_ShouldReturn200() throws Exception {
        GuestResponse.Creation mockResponse = GuestResponse.Creation.builder()
                .voucherNumber("VCH-12345678")
                .roomId(2L)
                .build();

        when(reservationFacade.updateReservation(eq("VCH-12345678"), any(GuestRequest.Creation.class)))
                .thenReturn(mockResponse);

        String jsonPayload = """
                {
                    "roomId": 2,
                    "checkInDate": "2026-09-01",
                    "checkOutDate": "2026-09-10",
                    "guests": [
                        { "firstname": "John", "lastname": "Doe updated" }
                    ]
                }
                """;

        mockMvc.perform(put("/api/v1/reservations/{voucherNumber}", "VCH-12345678")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voucherNumber").value("VCH-12345678"))
                .andExpect(jsonPath("$.roomId").value(2));
    }

    @Test
    @DisplayName("Should return 404 Not Found when voucherNumber does not exist")
    void updateReservation_WithNonExistentVoucher_ShouldReturn404() throws Exception {
        when(reservationFacade.updateReservation(eq("VCH-INVALID"), any(GuestRequest.Creation.class)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Reservation could not be found."));

        String jsonPayload = """
                {
                    "roomId": 2,
                    "checkInDate": "2026-09-01",
                    "checkOutDate": "2026-09-10",
                    "guests": [
                        { "firstname": "John", "lastname": "Doe" }
                    ]
                }
                """;

        mockMvc.perform(put("/api/v1/reservations/{voucherNumber}", "VCH-INVALID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is malformed on update")
    void updateReservation_WithMalformedJson_ShouldReturn400() throws Exception {
        String malformedJson = "{ invalid_json_format }";

        mockMvc.perform(put("/api/v1/reservations/{voucherNumber}", "VCH-12345678")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
}
