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
import com.example.hotel_management.guest.v1.GuestController;
import com.example.hotel_management.guest.v1.dto.GuestRequest;
import com.example.hotel_management.guest.v1.dto.GuestResponse;

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

        mockMvc.perform(get("/api/v1/guests")
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

        mockMvc.perform(get("/api/v1/guests"))
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

        mockMvc.perform(get("/api/v1/guests")
                .param("voucherNumber", "NON-EXISTING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when search parameters are invalid")
    void getGuests_WithInvalidParams_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/guests")
                .param("page", "-1") 
                .param("size", "10"))
                .andExpect(status().isBadRequest())
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

        mockMvc.perform(get("/api/v1/guests/hotel/{hotelId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstname").value("Jane"))
                .andExpect(jsonPath("$[0].hotelName").value("Grand Hotel"))
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @DisplayName("Should return 200 OK and an empty list if no guests exist for the hotelId")
    void getGuestsByHotelId_WithNoGuests_ShouldReturn200AndEmptyList() throws Exception {
        when(guestService.getGuestsByHotelId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/guests/hotel/{hotelId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when hotelId is not a valid Long type")
    void getGuestsByHotelId_WithInvalidIdType_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/guests/hotel/{hotelId}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 Not Found when hotelId does not exist")
    void getGuestsByHotelId_WithNonExistentId_ShouldReturn404() throws Exception {
        when(guestService.getGuestsByHotelId(999L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Hotel cannot be found"));

        mockMvc.perform(get("/api/v1/guests/hotel/{hotelId}", 999L))
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

        mockMvc.perform(get("/api/v1/guests/room/{roomId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstname").value("Alice"))
                .andExpect(jsonPath("$[0].roomNumber").value("101"))
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when roomId is not a valid Long type")
    void getGuestsByRoomId_WithInvalidIdType_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/guests/room/{roomId}", "invalid-room-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 Not Found when roomId does not exist")
    void getGuestsByRoomId_WithNonExistentId_ShouldReturn404() throws Exception {
        when(guestService.getGuestsByRoomId(999L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Room cannot be found"));

        mockMvc.perform(get("/api/v1/guests/room/{roomId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 204 No Content when guest is successfully deleted")
    void deleteGuest_WithValidId_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/guests/{guestId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when guestId is not a valid Long type")
    void deleteGuest_WithInvalidIdType_ShouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/v1/guests/{guestId}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 Not Found when guestId does not exist")
    void deleteGuest_WithNonExistentId_ShouldReturn404() throws Exception {
        org.mockito.Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Reservation could not be found."))
                .when(guestService).deleteGuest(999L);

        mockMvc.perform(delete("/api/v1/guests/{guestId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 204 No Content when reservation is successfully canceled")
    void cancelReservation_WithValidVoucher_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/guests/cancel-reservation/{voucherNumber}", "VCH-12345678"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 Not Found when voucherNumber does not exist")
    void cancelReservation_WithNonExistentVoucher_ShouldReturn404() throws Exception {
        org.mockito.Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Reservation could not be found."))
                .when(guestService).cancelReservation("VCH-INVALID");

        mockMvc.perform(delete("/api/v1/guests/cancel-reservation/{voucherNumber}", "VCH-INVALID"))
                .andExpect(status().isNotFound());
    }
}
