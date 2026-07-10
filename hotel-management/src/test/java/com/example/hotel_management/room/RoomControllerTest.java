package com.example.hotel_management.room;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.hotel_management.common.PagedResponse;
import com.example.hotel_management.room.v1.RoomController;
import com.example.hotel_management.room.v1.dto.RoomRequest;
import com.example.hotel_management.room.v1.dto.RoomResponse;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    private RoomResponse.QueryDetail sampleRoomResponse;
    private PagedResponse<RoomResponse.QueryDetail> mockPagedResponse;

    @BeforeEach
    void setUp() {
        sampleRoomResponse = RoomResponse.QueryDetail.builder()
                .id(100L)
                .roomNumber("101")
                .roomType("Deluxe Suite")
                .maxCapacity(3)
                .hotelId(1L)
                .hotelName("Akdeniz Grand Hotel")
                .build();

        mockPagedResponse = PagedResponse.<RoomResponse.QueryDetail>builder()
                .content(List.of(sampleRoomResponse))
                .pageNo(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .build();
    }

    @Test
    @DisplayName("getAllRooms_ShouldReturn200")
    void getAllRooms_ShouldReturn200() throws Exception {
        when(roomService.getAllRooms()).thenReturn(List.of(sampleRoomResponse));

        mockMvc.perform(get("/api/v1/rooms/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].roomNumber").value("101"));

        verify(roomService, times(1)).getAllRooms();
    }

    @Test
    @DisplayName("getRoomsByHotelId_WithValidId_ShouldReturn200")
    void getRoomsByHotelId_WithValidId_ShouldReturn200() throws Exception {
        when(roomService.getRoomsByHotelId(1L)).thenReturn(List.of(sampleRoomResponse));

        mockMvc.perform(get("/api/v1/rooms/hotel/{hotelId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hotelId").value(1L));

        verify(roomService, times(1)).getRoomsByHotelId(1L);
    }

    @Test
    @DisplayName("getRoomsByHotelId_WithNoRooms_ShouldReturn200AndEmptyList")
    void getRoomsByHotelId_WithNoRooms_ShouldReturn200AndEmptyList() throws Exception {
        when(roomService.getRoomsByHotelId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/rooms/hotel/{hotelId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("getRoomsByHotelId_WithInvalidIdType_ShouldReturn400")
    void getRoomsByHotelId_WithInvalidIdType_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/rooms/hotel/{hotelId}", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(roomService, never()).getRoomsByHotelId(any());
    }

    @Test
    @DisplayName("getRoomById_WithValidId_ShouldReturn200")
    void getRoomById_WithValidId_ShouldReturn200() throws Exception {
        when(roomService.getRoomById(100L)).thenReturn(sampleRoomResponse);

        mockMvc.perform(get("/api/v1/rooms/{roomId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));

        verify(roomService, times(1)).getRoomById(100L);
    }

    @Test
    @DisplayName("getRoomById_WithNonExistentId_ShouldReturn404")
    void getRoomById_WithNonExistentId_ShouldReturn404() throws Exception {
        when(roomService.getRoomById(999L)).thenThrow(new EntityNotFoundException("Room with ID 999 could not be found."));

        mockMvc.perform(get("/api/v1/rooms/{roomId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getAvailableRooms_WithValidParams_ShouldReturn200")
    void getAvailableRooms_WithValidParams_ShouldReturn200() throws Exception {
        LocalDate checkIn = LocalDate.of(2026, 8, 1);
        LocalDate checkOut = LocalDate.of(2026, 8, 5);

        when(roomService.findOptimalRoom(1L, 2, checkIn, checkOut)).thenReturn(List.of(sampleRoomResponse));

        mockMvc.perform(get("/api/v1/rooms/available")
                        .param("hotelId", "1")
                        .param("numberOfPerson", "2")
                        .param("checkInDate", "2026-08-01")
                        .param("checkOutDate", "2026-08-05")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    @DisplayName("getAvailableRoomsWithoutHotelFilter_WithValidParams_ShouldReturn200")
    void getAvailableRoomsWithoutHotelFilter_WithValidParams_ShouldReturn200() throws Exception {
        LocalDate checkIn = LocalDate.of(2026, 8, 1);
        LocalDate checkOut = LocalDate.of(2026, 8, 5);

        when(roomService.findOptimalRoomWithoutHotelFilter(2, checkIn, checkOut)).thenReturn(List.of(sampleRoomResponse));

        mockMvc.perform(get("/api/v1/rooms/available-all")
                        .param("numberOfPerson", "2")
                        .param("checkInDate", "2026-08-01")
                        .param("checkOutDate", "2026-08-05")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    @DisplayName("getAvailableRooms_WithMissingParams_ShouldReturn400")
    void getAvailableRooms_WithMissingParams_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/rooms/available")
                        .param("hotelId", "1")
                        .param("numberOfPerson", "2")
                        .param("checkOutDate", "2026-08-05")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createRoom_WithValidPayload_ShouldReturn201")
    void createRoom_WithValidPayload_ShouldReturn201() throws Exception {
        RoomRequest.Creation payload = RoomRequest.Creation.builder()
                .hotelId(1L)
                .roomNumber("101")
                .roomType("Deluxe")
                .maxCapacity(3)
                .build();

        RoomResponse.Creation responseDto = RoomResponse.Creation.builder()
                .id(100L)
                .roomNumber("101")
                .roomType("Deluxe")
                .maxCapacity(3)
                .hotelId(1L)
                .build();

        when(roomService.createRoom(any(RoomRequest.Creation.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/rooms/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.roomNumber").value("101"));

        verify(roomService, times(1)).createRoom(any(RoomRequest.Creation.class));
    }

    @Test
    @DisplayName("createRoom_WithMalformedJson_ShouldReturn400")
    void createRoom_WithMalformedJson_ShouldReturn400() throws Exception {
        RoomRequest.Creation invalidPayload = RoomRequest.Creation.builder()
                .hotelId(1L)
                .roomNumber("")
                .roomType("Deluxe")
                .maxCapacity(0)
                .build();

        mockMvc.perform(post("/api/v1/rooms/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidPayload)))
                .andExpect(status().isBadRequest());

        verify(roomService, never()).createRoom(any());
    }

    @Test
    @DisplayName("createRoom_WithNonExistentHotel_ShouldReturn404")
    void createRoom_WithNonExistentHotel_ShouldReturn404() throws Exception {
        RoomRequest.Creation payload = RoomRequest.Creation.builder()
                .hotelId(999L)
                .roomNumber("101")
                .roomType("Deluxe")
                .maxCapacity(2)
                .build();

        when(roomService.createRoom(any(RoomRequest.Creation.class)))
                .thenThrow(new EntityNotFoundException("Hotel with ID 999 could not be found."));

        mockMvc.perform(post("/api/v1/rooms/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("createRoom_WithDuplicateRoomNumber_ShouldReturn400")
    void createRoom_WithDuplicateRoomNumber_ShouldReturn400() throws Exception {
        RoomRequest.Creation payload = RoomRequest.Creation.builder()
                .hotelId(1L)
                .roomNumber("101")
                .roomType("Deluxe")
                .maxCapacity(2)
                .build();

        when(roomService.createRoom(any(RoomRequest.Creation.class)))
                .thenThrow(new IllegalArgumentException("This room already exists in this hotel."));

        mockMvc.perform(post("/api/v1/rooms/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("updateRoom_WithValidPayload_ShouldReturn200")
    void updateRoom_WithValidPayload_ShouldReturn200() throws Exception {
        RoomRequest.Creation payload = RoomRequest.Creation.builder()
                .hotelId(1L)
                .roomNumber("101")
                .roomType("Updated Deluxe")
                .maxCapacity(4)
                .build();

        RoomResponse.Creation responseDto = RoomResponse.Creation.builder()
                .id(100L)
                .roomNumber("101")
                .roomType("Updated Deluxe")
                .maxCapacity(4)
                .hotelId(1L)
                .build();

        when(roomService.updateRoom(eq(100L), any(RoomRequest.Creation.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/rooms/{roomId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomType").value("Updated Deluxe"))
                .andExpect(jsonPath("$.maxCapacity").value(4));
    }

    @Test
@DisplayName("updateRoom_WithNonExistentId_ShouldReturn404")
void updateRoom_WithNonExistentId_ShouldReturn404() throws Exception {
    RoomRequest.Creation payload = RoomRequest.Creation.builder()
            .hotelId(1L)
            .roomNumber("101")
            .roomType("Deluxe Suite") 
            .maxCapacity(2)          
            .build();

    when(roomService.updateRoom(eq(999L), any(RoomRequest.Creation.class)))
            .thenThrow(new EntityNotFoundException("Room with ID 999 could not be found."));

    mockMvc.perform(put("/api/v1/rooms/{roomId}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(payload)))
            .andExpect(status().isNotFound()); 
}

    @Test
    @DisplayName("updateRoom_WithCapacityUnderExistingGuests_ShouldReturn400")
    void updateRoom_WithCapacityUnderExistingGuests_ShouldReturn400() throws Exception {
        RoomRequest.Creation payload = RoomRequest.Creation.builder()
                .hotelId(1L)
                .roomNumber("101")
                .maxCapacity(1) 
                .build();

        when(roomService.updateRoom(eq(100L), any(RoomRequest.Creation.class)))
                .thenThrow(new IllegalArgumentException("Cannot reduce capacity below the number of guests in an existing booking"));

        mockMvc.perform(put("/api/v1/rooms/{roomId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deleteRoom_WithValidId_ShouldReturn204")
    void deleteRoom_WithValidId_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/rooms/{roomId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(roomService, times(1)).deleteRoom(100L);
    }

    @Test
    @DisplayName("deleteRoom_WithNonExistentId_ShouldReturn404")
    void deleteRoom_WithNonExistentId_ShouldReturn404() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("Room with ID 999 could not be found."))
                .when(roomService).deleteRoom(999L);

        mockMvc.perform(delete("/api/v1/rooms/{roomId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
}