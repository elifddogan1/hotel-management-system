package com.example.hotel_management.room;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.example.hotel_management.hotel.Hotel;

@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        Hotel hotel = Hotel.builder()
                .name("Akdeniz Test Hotel")
                .location("Antalya")
                .contactInfo("555-0000")
                .build();
        testHotel = entityManager.persistAndFlush(hotel);
    }

    @Test
    @DisplayName("Should return rooms by hotel id")
    void findByHotelId_ShouldReturnRooms() {
        Room room1 = Room.builder().roomNumber("101").roomType("Standard").maxCapacity(2).hotel(testHotel).build();
        Room room2 = Room.builder().roomNumber("102").roomType("Suite").maxCapacity(4).hotel(testHotel).build();
        entityManager.persist(room1);
        entityManager.persist(room2);
        entityManager.flush();

        List<Room> rooms = roomRepository.findByHotelId(testHotel.getId());

        assertThat(rooms).hasSize(2);
        assertThat(rooms).extracting(Room::getRoomNumber).containsExactlyInAnyOrder("101", "102");
    }

    @Test
    @DisplayName("Should return rooms by hotel id and capacity greater than or equal to given value")
    void findByHotelIdAndMaxCapacityGreaterThanEqual_ShouldReturnFilteredRooms() {
        Room room1 = Room.builder().roomNumber("101").roomType("Single").maxCapacity(1).hotel(testHotel).build();
        Room room2 = Room.builder().roomNumber("102").roomType("Double").maxCapacity(2).hotel(testHotel).build();
        Room room3 = Room.builder().roomNumber("103").roomType("Family").maxCapacity(4).hotel(testHotel).build();
        entityManager.persist(room1);
        entityManager.persist(room2);
        entityManager.persist(room3);
        entityManager.flush();

        List<Room> rooms = roomRepository.findByHotelIdAndMaxCapacityGreaterThanEqual(testHotel.getId(), 2);

        assertThat(rooms).hasSize(2);
        assertThat(rooms).extracting(Room::getMaxCapacity).containsOnly(2, 4);
    }

    @Test
    @DisplayName("Should return rooms by capacity globally without hotel filter")
    void findByMaxCapacityGreaterThanEqual_ShouldReturnRoomsGlobally() {
        Hotel anotherHotel = Hotel.builder().name("Another Hotel").build();
        entityManager.persist(anotherHotel);

        Room room1 = Room.builder().roomNumber("101").maxCapacity(3).hotel(testHotel).build();
        Room room2 = Room.builder().roomNumber("201").maxCapacity(5).hotel(anotherHotel).build();
        Room room3 = Room.builder().roomNumber("102").maxCapacity(1).hotel(testHotel).build();
        entityManager.persist(room1);
        entityManager.persist(room2);
        entityManager.persist(room3);
        entityManager.flush();

        List<Room> rooms = roomRepository.findByMaxCapacityGreaterThanEqual(3);

        assertThat(rooms).hasSize(2);
        assertThat(rooms).extracting(Room::getMaxCapacity).containsOnly(3, 5);
        
        assertThat(rooms).extracting(r -> r.getHotel().getName()).contains("Akdeniz Test Hotel", "Another Hotel");
    }

    @Test
    @DisplayName("Should return true when room exists by id and hotel id")
    void existsByIdAndHotelId_WhenExists_ShouldReturnTrue() {
        Room room = Room.builder().roomNumber("101").maxCapacity(2).hotel(testHotel).build();
        Room savedRoom = entityManager.persistAndFlush(room);

        boolean exists = roomRepository.existsByIdAndHotelId(savedRoom.getId(), testHotel.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when room exists but belongs to a different hotel")
    void existsByIdAndHotelId_WhenDifferentHotel_ShouldReturnFalse() {
        Room room = Room.builder().roomNumber("101").maxCapacity(2).hotel(testHotel).build();
        Room savedRoom = entityManager.persistAndFlush(room);

        boolean exists = roomRepository.existsByIdAndHotelId(savedRoom.getId(), 999L); 

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true when room number exists in the specific hotel")
    void existsByRoomNumberAndHotelId_WhenExists_ShouldReturnTrue() {
        Room room = Room.builder().roomNumber("101").maxCapacity(2).hotel(testHotel).build();
        entityManager.persistAndFlush(room);

        boolean exists = roomRepository.existsByRoomNumberAndHotelId("101", testHotel.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when room number does not exist in the specific hotel")
    void existsByRoomNumberAndHotelId_WhenNotExists_ShouldReturnFalse() {
        Room room = Room.builder().roomNumber("101").maxCapacity(2).hotel(testHotel).build();
        entityManager.persistAndFlush(room);

        boolean exists = roomRepository.existsByRoomNumberAndHotelId("999", testHotel.getId());

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should not return deleted rooms due to SQLRestriction (Soft Delete)")
    void softDelete_ShouldNotReturnDeletedRoom() {
        Room room = Room.builder()
                .roomNumber("101")
                .roomType("Deluxe")
                .maxCapacity(2)
                .hotel(testHotel)
                .build();
        Room savedRoom = roomRepository.save(room);
        entityManager.flush();

        roomRepository.deleteById(savedRoom.getId());
        entityManager.flush();
        entityManager.clear();

        Optional<Room> foundRoom = roomRepository.findById(savedRoom.getId());
        assertThat(foundRoom).isEmpty();
        
        Room nativeQueryRoom = (Room) entityManager.getEntityManager()
                .createNativeQuery("SELECT * FROM rooms WHERE id = :id", Room.class)
                .setParameter("id", savedRoom.getId())
                .getSingleResult();
                
        assertThat(nativeQueryRoom.isDeleted()).isTrue();
    }
}