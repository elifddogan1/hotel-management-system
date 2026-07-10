package com.example.hotel_management.hotel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.hotel_management.hotel.dto.HotelRequest;
import com.example.hotel_management.hotel.dto.HotelResponse;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    @Test
    void getAllHotels_WhenHotelsExist_ShouldReturnHotelResponseList() {
        // Arrange (Hazırlık)
        Hotel hotel1 = Hotel.builder()
                .id(1L)
                .name("Büyük Otel")
                .location("Antalya")
                .contactInfo("0242 111 2233")
                .build();
                
        Hotel hotel2 = Hotel.builder()
                .id(2L)
                .name("Şehir Oteli")
                .location("Ankara")
                .contactInfo("0312 444 5566")
                .build();
                
        List<Hotel> mockHotelList = List.of(hotel1, hotel2);
        
        // Repository'nin findAll metodu çağrıldığında bizim oluşturduğumuz mock listeyi dönmesini söylüyoruz.
        when(hotelRepository.findAll()).thenReturn(mockHotelList);

        // Act (Eylem)
        List<HotelResponse> result = hotelService.getAllHotels();

        // Assert (Doğrulama)
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // DTO dönüşümünün doğru yapılıp yapılmadığını kontrol ediyoruz.
        assertEquals(1L, result.get(0).getId());
        assertEquals("Büyük Otel", result.get(0).getName());
        assertEquals("Antalya", result.get(0).getLocation());
        
        assertEquals(2L, result.get(1).getId());
        assertEquals("Şehir Oteli", result.get(1).getName());
        
        // findAll metodunun tam olarak 1 kere çağrıldığını doğruluyoruz.
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void getAllHotels_WhenNoHotelsExist_ShouldReturnEmptyList() {
        // Arrange (Hazırlık)
        // Veritabanı boşken findAll boş bir liste dönecektir.
        when(hotelRepository.findAll()).thenReturn(new ArrayList<>());

        // Act (Eylem)
        List<HotelResponse> result = hotelService.getAllHotels();

        // Assert (Doğrulama)
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // findAll metodunun tam olarak 1 kere çağrıldığını doğruluyoruz.
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void getHotelById_WhenHotelExists_ShouldReturnHotelResponse() {
        // Arrange (Hazırlık)
        Long hotelId = 1L;
        Hotel hotel = Hotel.builder()
                .id(hotelId)
                .name("Akdeniz Resort")
                .location("Antalya")
                .contactInfo("0242 123 4567")
                .build();

        // Repository'den bu ID ile otel arandığında dolu bir Optional dönmesini simüle ediyoruz.
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        // Act (Eylem)
        HotelResponse result = hotelService.getHotelById(hotelId);

        // Assert (Doğrulama)
        assertNotNull(result);
        assertEquals(hotelId, result.getId());
        assertEquals("Akdeniz Resort", result.getName());
        assertEquals("Antalya", result.getLocation());
        assertEquals("0242 123 4567", result.getContactInfo());

        // findById metodunun ilgili ID ile tam olarak 1 kere çağrıldığını doğruluyoruz.
        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void getHotelById_WhenHotelDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Arrange (Hazırlık)
        Long hotelId = 99L; // Olmayan bir ID
        
        // Repository'den bu ID ile otel arandığında boş bir Optional (Optional.empty()) dönmesini simüle ediyoruz.
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        // Act & Assert (Eylem ve Doğrulama)
        // Metot çağrıldığında EntityNotFoundException fırlatılmasını bekliyoruz.
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            hotelService.getHotelById(hotelId);
        });

        // Fırlatılan hatanın mesajının bizim servis katmanında yazdığımız mesajla eşleştiğini kontrol ediyoruz.
        assertEquals("Hotel not found with ID: " + hotelId, exception.getMessage());

        // findById metodunun ilgili ID ile tam olarak 1 kere çağrıldığını doğruluyoruz.
        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void deleteHotel_WhenHotelExists_ShouldDeleteHotel() {
        // Arrange (Hazırlık)
        Long hotelId = 1L;
        
        // Repository'ye "bu ID'ye sahip bir otel var mı?" diye sorulduğunda "evet (true)" dönmesini simüle ediyoruz.
        when(hotelRepository.existsById(hotelId)).thenReturn(true);

        // Act (Eylem)
        hotelService.deleteHotel(hotelId);

        // Assert (Doğrulama)
        // existById kontrolünün yapıldığını doğruluyoruz.
        verify(hotelRepository, times(1)).existsById(hotelId);
        
        // Otel mevcut olduğu için deleteById metodunun çağrıldığını doğruluyoruz.
        verify(hotelRepository, times(1)).deleteById(hotelId);
    }

    @Test
    void deleteHotel_WhenHotelDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Arrange (Hazırlık)
        Long hotelId = 99L; // Olmayan bir ID
        
        // Repository'ye sorulduğunda "hayır (false)" dönmesini simüle ediyoruz.
        when(hotelRepository.existsById(hotelId)).thenReturn(false);

        // Act & Assert (Eylem ve Doğrulama)
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            hotelService.deleteHotel(hotelId);
        });

        // Fırlatılan hatanın mesajını kontrol ediyoruz.
        assertEquals("Hotel not found with ID: " + hotelId, exception.getMessage());

        // existById kontrolünün yapıldığını doğruluyoruz.
        verify(hotelRepository, times(1)).existsById(hotelId);
        
        // Otel BULUNAMADIĞI için deleteById metodunun HİÇ ÇAĞRILMADIĞINI (never) garanti altına alıyoruz.
        verify(hotelRepository, never()).deleteById(anyLong());
    }

    // ==========================================
    // UPDATE HOTEL (OTEL GÜNCELLEME) TESTLERİ
    // ==========================================

    @Test
    void updateHotel_WhenHotelExists_ShouldUpdateAndSave() {
        // Arrange (Hazırlık)
        Long hotelId = 1L;
        
        // Veritabanındaki mevcut otel durumu
        Hotel existingHotel = Hotel.builder()
                .id(hotelId)
                .name("Eski İsim")
                .location("Eski Lokasyon")
                .contactInfo("Eski İletişim")
                .build();

        // Dışarıdan gelen güncelleme isteği
        HotelRequest.Update request = HotelRequest.Update.builder()
                .name("Yeni İsim")
                .location("Yeni Lokasyon")
                .contactInfo("Yeni İletişim")
                .build();

        // Kayıt sonrası oluşacak güncel otel durumu
        Hotel updatedHotel = Hotel.builder()
                .id(hotelId)
                .name(request.getName())
                .location(request.getLocation())
                .contactInfo(request.getContactInfo())
                .build();

        // Mock'lamalar: Önce otel bulunsun, sonra save edilince güncel hali dönsün
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(existingHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(updatedHotel);

        // Act (Eylem)
        HotelResponse result = hotelService.updateHotel(hotelId, request);

        // Assert (Doğrulama)
        assertNotNull(result);
        assertEquals(hotelId, result.getId());
        assertEquals("Yeni İsim", result.getName());
        assertEquals("Yeni Lokasyon", result.getLocation());
        assertEquals("Yeni İletişim", result.getContactInfo());

        // findById'nin 1 kez, save'in 1 kez çağrıldığını doğruluyoruz.
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelRepository, times(1)).save(existingHotel); // referans üzerinden güncellendiği için existingHotel kaydedilir
    }

    @Test
    void updateHotel_WhenHotelDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Arrange (Hazırlık)
        Long hotelId = 99L; // Olmayan bir ID
        HotelRequest.Update request = HotelRequest.Update.builder()
                .name("Yeni İsim")
                .build();

        // Repository boş dönsün
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        // Act & Assert (Eylem ve Doğrulama)
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            hotelService.updateHotel(hotelId, request);
        });

        assertEquals("Hotel not found with ID: " + hotelId, exception.getMessage());

        // findById çağrılmış olmalı ancak kayıt (save) İŞLEMİ HİÇ YAPILMAMALI
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelRepository, never()).save(any(Hotel.class));
    }

    // ==========================================
    // CREATE HOTEL (OTEL OLUŞTURMA) TESTİ
    // ==========================================

    @Test
    void createHotel_WhenCreationIsSuccessful_ShouldReturnHotelResponse() {
        // Arrange (Hazırlık)
        HotelRequest.Creation request = HotelRequest.Creation.builder()
                .name("Yeni Otel")
                .location("Sahil Yolu")
                .contactInfo("iletisim@yeniotel.com")
                .build();

        // Veritabanına kaydedildikten sonra (ID almış haliyle) dönecek olan mock nesne
        Hotel savedHotel = Hotel.builder()
                .id(1L)
                .name(request.getName())
                .location(request.getLocation())
                .contactInfo(request.getContactInfo())
                .build();

        // Herhangi bir Hotel nesnesi save edilmek istendiğinde savedHotel dönülsün
        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);

        // Act (Eylem)
        HotelResponse result = hotelService.createHotel(request);

        // Assert (Doğrulama)
        assertNotNull(result);
        assertEquals(1L, result.getId()); // ID'nin doğru set edildiğini doğrula
        assertEquals("Yeni Otel", result.getName());
        assertEquals("Sahil Yolu", result.getLocation());
        assertEquals("iletisim@yeniotel.com", result.getContactInfo());

        // save işleminin tam 1 kere çağrıldığını doğruluyoruz
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }
    
}