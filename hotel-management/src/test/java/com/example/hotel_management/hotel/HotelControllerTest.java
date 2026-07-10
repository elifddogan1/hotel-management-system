package com.example.hotel_management.hotel;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

import com.example.hotel_management.common.GlobalExceptionHandler;
import com.example.hotel_management.hotel.dto.HotelRequest;
import com.example.hotel_management.hotel.dto.HotelResponse;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest({HotelController.class, GlobalExceptionHandler.class})
public class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelService hotelService;

    

    @Test
    @DisplayName("Bütün otelleri listele - Başarılı")
    void getAllHotels_ShouldReturn200AndList() throws Exception {
        HotelResponse response = HotelResponse.builder().id(1L).name("Test Otel").build();
        when(hotelService.getAllHotels()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Otel"));
    }

    @Test
    @DisplayName("Bütün otelleri listele - Liste Boş")
    void getAllHotels_WhenEmpty_ShouldReturn200AndEmptyList() throws Exception {
        when(hotelService.getAllHotels()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }


    @Test
    @DisplayName("Geçerli ID ile otel getir - Başarılı")
    void getHotelById_WithValidId_ShouldReturn200() throws Exception {
        HotelResponse response = HotelResponse.builder().id(1L).name("Test Otel").build();
        when(hotelService.getHotelById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/hotels/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Otel"));
    }

    @Test
    @DisplayName("Geçersiz Tipte ID ile otel getir - HTTP 400")
    void getHotelById_WithInvalidIdType_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/hotels/{id}", "abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Olmayan ID ile otel getir - HTTP 404")
    void getHotelById_WithNonExistentId_ShouldReturn404() throws Exception {
        when(hotelService.getHotelById(99L)).thenThrow(new EntityNotFoundException("Hotel not found"));

        mockMvc.perform(get("/api/hotels/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Geçerli veri ile otel oluştur - Başarılı (HTTP 201)")
    void createHotel_WithValidPayload_ShouldReturn201() throws Exception {
        HotelResponse response = HotelResponse.builder().id(1L).name("Yeni Otel").build();
        
        when(hotelService.createHotel(any(HotelRequest.Creation.class))).thenReturn(response);

        String validPayload = """
                {
                    "name": "Yeni Otel",
                    "location": "Antalya",
                    "contactInfo": "123456"
                }
                """;

        mockMvc.perform(post("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Yeni Otel"));
    }

    @Test
    @DisplayName("Zorunlu alan eksik otel oluştur - HTTP 400")
    void createHotel_WithMissingRequiredFields_ShouldReturn400() throws Exception {
        String invalidPayload = """
                {
                    "location": "Antalya"
                }
                """;

        mockMvc.perform(post("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Geçerli veri ile otel güncelle - Başarılı")
    void updateHotel_WithValidPayload_ShouldReturn200() throws Exception {
        HotelResponse response = HotelResponse.builder().id(1L).name("Güncel Otel").build();
        
        when(hotelService.updateHotel(eq(1L), any(HotelRequest.Update.class))).thenReturn(response);

        String validPayload = """
                {
                    "name": "Güncel Otel",
                    "location": "Güncel Lokasyon"
                }
                """;

        mockMvc.perform(put("/api/hotels/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Güncel Otel"));
    }

    @Test
    @DisplayName("Olmayan oteli güncelle - HTTP 404")
    void updateHotel_WithNonExistentId_ShouldReturn404() throws Exception {
        when(hotelService.updateHotel(eq(99L), any(HotelRequest.Update.class)))
                .thenThrow(new EntityNotFoundException("Hotel not found"));

        String payload = """
                {
                    "name": "Test",
                    "location": "Test"
                }
                """;

        mockMvc.perform(put("/api/hotels/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Geçerli ID ile otel sil - Başarılı (HTTP 204)")
    void deleteHotel_WithValidId_ShouldReturn204() throws Exception {
        doNothing().when(hotelService).deleteHotel(1L);

        mockMvc.perform(delete("/api/hotels/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Olmayan ID ile otel sil - HTTP 404")
    void deleteHotel_WithNonExistentId_ShouldReturn404() throws Exception {
        doThrow(new EntityNotFoundException("Hotel not found")).when(hotelService).deleteHotel(99L);

        mockMvc.perform(delete("/api/hotels/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}