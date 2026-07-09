package com.example.hotel_management.guest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.example.hotel_management.hotel.Hotel;
import com.example.hotel_management.room.Room;

@DataJpaTest
public class GuestRepositoryTest {

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Room testRoom;
    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        Hotel hotel = Hotel.builder()
                .name("Grand Test Hotel")
                .location("Istanbul")
                .contactInfo("12345")
                .build();
        testHotel = entityManager.persistAndFlush(hotel);

        Room room = Room.builder()
                .roomNumber("101")
                .roomType("Suite")
                .maxCapacity(4)
                .hotel(testHotel)
                .build();
        testRoom = entityManager.persistAndFlush(room);
    }

    @Test
    @DisplayName("Should return true when guest with voucher number exists")
    void existsByVoucherNumber_WhenExists_ShouldReturnTrue() {
        Guest guest = Guest.builder()
                .firstname("Elif")
                .lastname("Yılmaz")
                .voucherNumber("VCH-123")
                .room(testRoom)
                .build();
        entityManager.persistAndFlush(guest);

        boolean exists = guestRepository.existsByVoucherNumber("VCH-123");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when guest with voucher number does not exist")
    void existsByVoucherNumber_WhenNotExists_ShouldReturnFalse() {
        boolean exists = guestRepository.existsByVoucherNumber("VCH-NONEXISTENT");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find guests by voucher number")
    void findByVoucherNumber_ShouldReturnGuests() {
        Guest guest1 = Guest.builder().firstname("A").lastname("B").voucherNumber("VCH-999").room(testRoom).build();
        Guest guest2 = Guest.builder().firstname("C").lastname("D").voucherNumber("VCH-999").room(testRoom).build();
        entityManager.persist(guest1);
        entityManager.persist(guest2);
        entityManager.flush();

        List<Guest> guests = guestRepository.findByVoucherNumber("VCH-999");

        assertThat(guests).hasSize(2);
        assertThat(guests).extracting(Guest::getVoucherNumber).containsOnly("VCH-999");
    }

    @Test
    @DisplayName("Should find guests by room hotel id using custom query")
    void findByRoomHotelId_ShouldReturnGuests() {
        Guest guest = Guest.builder().firstname("Elif").lastname("Y").room(testRoom).build();
        entityManager.persistAndFlush(guest);
        
        Long hotelId = testRoom.getHotel() != null ? testRoom.getHotel().getId() : null;

        if (hotelId != null) {
            List<Guest> guests = guestRepository.findByRoomHotelId(hotelId);
            assertThat(guests).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Should find guests by room hotel id using naming convention")
    void findByRoom_Hotel_Id_ShouldReturnGuests() {
        Guest guest = Guest.builder().firstname("Elif").lastname("Y").room(testRoom).build();
        entityManager.persistAndFlush(guest);
        
        Long hotelId = testRoom.getHotel() != null ? testRoom.getHotel().getId() : null;

        if (hotelId != null) {
            List<Guest> guests = guestRepository.findByRoom_Hotel_Id(hotelId);
            assertThat(guests).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Should find guests by room id using custom query")
    void findByRoomId_ShouldReturnGuests() {
        Guest guest = Guest.builder().firstname("Test").lastname("User").room(testRoom).build();
        entityManager.persistAndFlush(guest);

        List<Guest> guests = guestRepository.findByRoomId(testRoom.getId());

        assertThat(guests).hasSize(1);
        assertThat(guests.get(0).getRoom().getId()).isEqualTo(testRoom.getId());
    }

    @Test
    @DisplayName("Should find guests by room id using naming convention")
    void findByRoom_Id_ShouldReturnGuests() {
        Guest guest = Guest.builder().firstname("Test").lastname("User").room(testRoom).build();
        entityManager.persistAndFlush(guest);

        List<Guest> guests = guestRepository.findByRoom_Id(testRoom.getId());

        assertThat(guests).hasSize(1);
        assertThat(guests.get(0).getRoom().getId()).isEqualTo(testRoom.getId());
    }

    @Test
    @DisplayName("Should find guests by room id and order by check-in date ascending")
    void findByRoomIdOrderByCheckInDateAsc_ShouldReturnSortedGuests() {
        Guest guest1 = Guest.builder().firstname("First").lastname("Guest").room(testRoom).checkInDate(LocalDate.now().plusDays(5)).build();
        Guest guest2 = Guest.builder().firstname("Second").lastname("Guest").room(testRoom).checkInDate(LocalDate.now().plusDays(2)).build();
        Guest guest3 = Guest.builder().firstname("Third").lastname("Guest").room(testRoom).checkInDate(LocalDate.now().plusDays(10)).build();
        
        entityManager.persist(guest1);
        entityManager.persist(guest2);
        entityManager.persist(guest3);
        entityManager.flush();

        List<Guest> sortedGuests = guestRepository.findByRoomIdOrderByCheckInDateAsc(testRoom.getId());

        assertThat(sortedGuests).hasSize(3);
        assertThat(sortedGuests.get(0).getFirstname()).isEqualTo("Second");
        assertThat(sortedGuests.get(1).getFirstname()).isEqualTo("First");
        assertThat(sortedGuests.get(2).getFirstname()).isEqualTo("Third");
    }

    @Test
    @DisplayName("Should not return deleted guests due to SQLRestriction")
    void softDelete_ShouldNotReturnDeletedGuest() {
        Guest guest = Guest.builder()
                .firstname("To Be")
                .lastname("Deleted")
                .voucherNumber("VCH-DEL")
                .room(testRoom)
                .build();
        Guest savedGuest = guestRepository.save(guest);
        entityManager.flush();

        guestRepository.deleteById(savedGuest.getId());
        entityManager.flush();
        entityManager.clear();

        List<Guest> foundGuests = guestRepository.findByVoucherNumber("VCH-DEL");
        assertThat(foundGuests).isEmpty();
        
        Guest nativeQueryGuest = (Guest) entityManager.getEntityManager()
                .createNativeQuery("SELECT * FROM guests WHERE id = :id", Guest.class)
                .setParameter("id", savedGuest.getId())
                .getSingleResult();
                
        assertThat(nativeQueryGuest.isDeleted()).isTrue();
    }
}