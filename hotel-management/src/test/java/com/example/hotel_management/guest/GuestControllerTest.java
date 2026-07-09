package com.example.hotel_management.guest;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.hotel_management.common.GlobalExceptionHandler;
import com.example.hotel_management.common.PagedResponse;
import com.example.hotel_management.guest.dto.GuestRequest;
import com.example.hotel_management.guest.dto.GuestResponse;

@WebMvcTest({GuestController.class, GlobalExceptionHandler.class})
public class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestService guestService;

    @Test
    @DisplayName("Should return 200 OK and populated PagedResponse with valid search parameters")
    void getGuests_WithValidParams_ShouldReturn200() throws Exception {
        GuestResponse.QueryDetail mockDetail = GuestResponse.QueryDetail.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .voucherNumber("VCH-12345678")
                .build();

        PagedResponse<GuestResponse.QueryDetail> mockResponse = PagedResponse.<GuestResponse.QueryDetail>builder()
                .content(List.of(mockDetail))
                .pageNo(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(guestService.searchAndSortGuests(any(GuestRequest.Search.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/guests")
                .param("firstname", "John")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstname").value("John"))
                .andExpect(jsonPath("$.content[0].voucherNumber").value("VCH-12345678"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should return 200 OK with default parameters when no query params are provided")
    void getGuests_WithoutParams_ShouldReturn200() throws Exception {
        PagedResponse<GuestResponse.QueryDetail> mockResponse = PagedResponse.<GuestResponse.QueryDetail>builder()
                .content(List.of())
                .pageNo(0)
                .pageSize(10)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();

        when(guestService.searchAndSortGuests(any(GuestRequest.Search.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNo").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    @DisplayName("Should return 200 OK and an empty list when no guests match the criteria")
    void getGuests_WithNoMatches_ShouldReturn200AndEmptyList() throws Exception {
        PagedResponse<GuestResponse.QueryDetail> emptyResponse = PagedResponse.<GuestResponse.QueryDetail>builder()
                .content(Collections.emptyList())
                .pageNo(0)
                .pageSize(10)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();

        when(guestService.searchAndSortGuests(any(GuestRequest.Search.class))).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/guests")
                .param("voucherNumber", "NON-EXISTING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when search parameters are invalid")
    void getGuests_WithInvalidParams_ShouldReturn400() throws Exception {
        // Geçersiz bir parametre gönderiyoruz (Örn: page eksi bir değer olamaz)
        mockMvc.perform(get("/api/guests")
                .param("page", "-1") 
                .param("size", "10"))
                .andExpect(status().isBadRequest())
                // Fırlattığınız manuel exception mesajını kontrol edebilirsiniz
                // reason yerine doğrudan dönen body'nin içinde arama yapabilirsin
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Validation failed")));
    }



    @Test
    @DisplayName("Should return 200 OK and list of guests for a valid hotelId")
    void getGuestsByHotelId_WithValidId_ShouldReturn200() throws Exception {
        GuestResponse.QueryDetail mockDetail = GuestResponse.QueryDetail.builder()
                .id(1L)
                .firstname("Jane")
                .lastname("Doe")
                .hotelName("Grand Hotel")
                .build();

        when(guestService.getGuestsByHotelId(1L)).thenReturn(List.of(mockDetail));

        mockMvc.perform(get("/api/guests/hotel/{hotelId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstname").value("Jane"))
                .andExpect(jsonPath("$[0].hotelName").value("Grand Hotel"))
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @DisplayName("Should return 200 OK and an empty list if no guests exist for the hotelId")
    void getGuestsByHotelId_WithNoGuests_ShouldReturn200AndEmptyList() throws Exception {
        when(guestService.getGuestsByHotelId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/guests/hotel/{hotelId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when hotelId is not a valid Long type")
    void getGuestsByHotelId_WithInvalidIdType_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/guests/hotel/{hotelId}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 Not Found when hotelId does not exist")
    void getGuestsByHotelId_WithNonExistentId_ShouldReturn404() throws Exception {
        when(guestService.getGuestsByHotelId(999L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Hotel cannot be found"));

        mockMvc.perform(get("/api/guests/hotel/{hotelId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 OK and list of guests for a valid roomId")
    void getGuestsByRoomId_WithValidId_ShouldReturn200() throws Exception {
        GuestResponse.QueryDetail mockDetail = GuestResponse.QueryDetail.builder()
                .id(1L)
                .firstname("Alice")
                .lastname("Smith")
                .roomNumber("101")
                .build();

        when(guestService.getGuestsByRoomId(2L)).thenReturn(List.of(mockDetail));

        mockMvc.perform(get("/api/guests/room/{roomId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstname").value("Alice"))
                .andExpect(jsonPath("$[0].roomNumber").value("101"))
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when roomId is not a valid Long type")
    void getGuestsByRoomId_WithInvalidIdType_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/guests/room/{roomId}", "invalid-room-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 Not Found when roomId does not exist")
    void getGuestsByRoomId_WithNonExistentId_ShouldReturn404() throws Exception {
        when(guestService.getGuestsByRoomId(999L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Room cannot be found"));

        mockMvc.perform(get("/api/guests/room/{roomId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 201 Created and the created guest details with valid payload")
    void createGuest_WithValidPayload_ShouldReturn201() throws Exception {
        GuestResponse.Creation mockResponse = GuestResponse.Creation.builder()
                .voucherNumber("VCH-12345678")
                .roomId(1L)
                .build();

        when(guestService.createGuest(any(GuestRequest.Creation.class))).thenReturn(mockResponse);

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

        mockMvc.perform(post("/api/guests/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voucherNumber").value("VCH-12345678"))
                .andExpect(jsonPath("$.roomId").value(1));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is missing or malformed")
    void createGuest_WithMalformedJson_ShouldReturn400() throws Exception {
        String malformedJson = "{ invalid_json_format }";

        mockMvc.perform(post("/api/guests/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 Not Found when room does not exist")
    void createGuest_WithNonExistentRoom_ShouldReturn404() throws Exception {
        when(guestService.createGuest(any(GuestRequest.Creation.class)))
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

        mockMvc.perform(post("/api/guests/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 204 No Content when guest is successfully deleted")
    void deleteGuest_WithValidId_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/guests/{guestId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when guestId is not a valid Long type")
    void deleteGuest_WithInvalidIdType_ShouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/guests/{guestId}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 Not Found when guestId does not exist")
    void deleteGuest_WithNonExistentId_ShouldReturn404() throws Exception {
        org.mockito.Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Reservation could not be found."))
                .when(guestService).deleteGuest(999L);

        mockMvc.perform(delete("/api/guests/{guestId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 204 No Content when reservation is successfully canceled")
    void cancelReservation_WithValidVoucher_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/guests/cancel-reservation/{voucherNumber}", "VCH-12345678"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 Not Found when voucherNumber does not exist")
    void cancelReservation_WithNonExistentVoucher_ShouldReturn404() throws Exception {
        org.mockito.Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Reservation could not be found."))
                .when(guestService).cancelReservation("VCH-INVALID");

        mockMvc.perform(delete("/api/guests/cancel-reservation/{voucherNumber}", "VCH-INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 OK and updated reservation details with valid payload")
    void updateReservation_WithValidPayload_ShouldReturn200() throws Exception {
        GuestResponse.Creation mockResponse = GuestResponse.Creation.builder()
                .voucherNumber("VCH-12345678")
                .roomId(2L)
                .build();

        when(guestService.updateReservation(org.mockito.ArgumentMatchers.eq("VCH-12345678"), any(GuestRequest.Creation.class)))
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

        mockMvc.perform(put("/api/guests/reservation/{voucherNumber}", "VCH-12345678")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voucherNumber").value("VCH-12345678"))
                .andExpect(jsonPath("$.roomId").value(2));
    }

    @Test
    @DisplayName("Should return 404 Not Found when voucherNumber does not exist")
    void updateReservation_WithNonExistentVoucher_ShouldReturn404() throws Exception {
        when(guestService.updateReservation(org.mockito.ArgumentMatchers.eq("VCH-INVALID"), any(GuestRequest.Creation.class)))
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

        mockMvc.perform(put("/api/guests/reservation/{voucherNumber}", "VCH-INVALID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is malformed")
    void updateReservation_WithMalformedJson_ShouldReturn400() throws Exception {
        String malformedJson = "{ invalid_json_format }";

        mockMvc.perform(put("/api/guests/reservation/{voucherNumber}", "VCH-12345678")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
}
