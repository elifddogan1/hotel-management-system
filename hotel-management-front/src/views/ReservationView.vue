<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { roomService, type RoomQueryDetail, type AvailableRoomSearchParams } from '../services/roomService';
import { hotelService, type HotelResponse } from '../services/hotelService';
import { guestService, type GuestCreationRequest } from '../services/guestService';
// Profesyonel ikonlarımızı ekledik
import { CalendarSearch, BedDouble, CheckCircle2, Users, UserPlus, Trash2, Ticket, Building2, CalendarPlus } from 'lucide-vue-next';

const router = useRouter();
const route = useRoute();
const rooms = ref<RoomQueryDetail[]>([]);
const hotels = ref<HotelResponse[]>([]);
const isLoading = ref(true);

const searchParams = ref<AvailableRoomSearchParams>({
  hotelId: undefined,   
  numberOfPerson: 2,
  checkInDate: '',
  checkOutDate: ''
});

// Form State
const selectedRoom = ref<RoomQueryDetail | null>(null);
const newReservation = ref<GuestCreationRequest>({
  roomId: 0, checkInDate: '', checkOutDate: '',
  guests: [{ firstname: '', lastname: '' }]
});

const loadAllData = async () => {
  isLoading.value = true;
  try {
    hotels.value = await hotelService.getAllHotels();
  } catch (error) {
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

const handleAvailabilitySearch = async () => {
  if (!searchParams.value.checkInDate || !searchParams.value.checkOutDate) {
    alert('Lütfen giriş ve çıkış tarihlerini seçin.');
    return;
  }
  isLoading.value = true;
  selectedRoom.value = null; 
  try {
    rooms.value = await roomService.getAvailableRooms(searchParams.value);
  } catch (error) {
    alert('Müsaitlik araması sırasında hata oluştu.');
  } finally {
    isLoading.value = false;
  }
};

const selectRoomForBooking = (room: RoomQueryDetail) => {
  selectedRoom.value = room;
  const guestCount = Math.min(searchParams.value.numberOfPerson, room.maxCapacity);
  newReservation.value = {
    roomId: room.id!,
    checkInDate: searchParams.value.checkInDate,
    checkOutDate: searchParams.value.checkOutDate,
    guests: Array.from({ length: guestCount }, () => ({ firstname: '', lastname: '' }))
  };
};

const isDirectBooking = computed(() => !!route.query.roomId);

const selectedHotel = computed(() => {
  if (!route.query.hotelId || hotels.value.length === 0) return null;
  return hotels.value.find(h => h.id === Number(route.query.hotelId)) || null;
});

const addGuestField = () => {
  if (selectedRoom.value && newReservation.value.guests.length >= selectedRoom.value.maxCapacity) {
    alert(`Odadaki kişi sayısı maksimum ${selectedRoom.value.maxCapacity} olabilir.`);
    return;
  }
  newReservation.value.guests.push({ firstname: '', lastname: '' });
  if (isDirectBooking.value) {
    searchParams.value.numberOfPerson = newReservation.value.guests.length;
  }
};

const removeGuestField = (index: number) => {
  newReservation.value.guests.splice(index, 1);
  if (isDirectBooking.value) {
    searchParams.value.numberOfPerson = newReservation.value.guests.length;
  }
};

const submitReservation = async () => {
  try {
    await guestService.createGuest(newReservation.value);
    alert('Rezervasyon başarıyla oluşturuldu!');
    router.push({ name: 'guests' }); // İşlem bitince Misafirler Listesine dön
  } catch (error: any) {
    alert(error.response?.data?.message || 'Rezervasyon oluşturulamadı.');
  }
};

// Keep guests array in sync with numberOfPerson
watch(() => searchParams.value.numberOfPerson, (newVal) => {
  if (isDirectBooking.value && newVal && newVal > 0) {
    const maxCap = selectedRoom.value?.maxCapacity || 10;
    if (newVal > maxCap) {
      searchParams.value.numberOfPerson = maxCap;
      return;
    }
    const currentLength = newReservation.value.guests.length;
    if (newVal > currentLength) {
      for (let i = currentLength; i < newVal; i++) {
        if (newReservation.value.guests.length < maxCap) {
          newReservation.value.guests.push({ firstname: '', lastname: '' });
        }
      }
    } else if (newVal < currentLength) {
      newReservation.value.guests.splice(newVal);
    }
  }
});

onMounted(async () => {
  await loadAllData();
  // RoomDetail'den gelindiyse otel bilgisini otomatik filtreye ekle
  if (route.query.hotelId) {
    searchParams.value.hotelId = Number(route.query.hotelId);
  }
  if (route.query.roomId && route.query.hotelId) {
    try {
      const allRooms = await roomService.getRoomsByHotelId(Number(route.query.hotelId));
      const room = allRooms.find(r => r.id === Number(route.query.roomId));
      if (room) {
        selectedRoom.value = room;
        newReservation.value.roomId = room.id!;
        const guestCount = Math.min(searchParams.value.numberOfPerson, room.maxCapacity);
        newReservation.value.guests = Array.from(
          { length: guestCount },
          () => ({ firstname: '', lastname: '' })
        );
      }
    } catch (error) {
      console.error("Direct room loading error:", error);
    }
  }
});
</script>

<template>
  <div class="dashboard-layout">
    <!-- Arka plan dekoratif elementleri -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <div class="content-wrapper">
      <header class="header">
        <h1>Yeni Rezervasyon Oluştur</h1>
        <p class="subtitle">Müsait odaları listeleyin ve hızlıca konaklama kaydı açın.</p>
      </header>

      <div class="grid-layout" :class="{ 'single-col': !selectedRoom }">
        <!-- SOL: ARAMA VE LİSTE (Normal Rezervasyon Akışı) -->
        <div v-if="!isDirectBooking" class="left-col fade-in">
          
          <!-- ADIM 1: TARİH VE ARAMA -->
          <section class="glass-card mb-24">
            <div class="section-header">
              <div class="icon-box blue-box"><CalendarSearch :size="24" /></div>
              <h2>1. Tarih ve Müsaitlik</h2>
            </div>
            
            <form @submit.prevent="handleAvailabilitySearch" class="search-form">
              <div class="flex-row">
                <div class="form-group half">
                  <label>Giriş Tarihi <span class="required">*</span></label>
                  <input v-model="searchParams.checkInDate" type="date" required />
                </div>
                <div class="form-group half">
                  <label>Çıkış Tarihi <span class="required">*</span></label>
                  <input v-model="searchParams.checkOutDate" type="date" required />
                </div>
              </div>
              <div class="flex-row mt-10">
                <div class="form-group half">
                  <label>Otel Tercihi</label>
                  <select v-model="searchParams.hotelId">
                    <option :value="undefined">Tüm Otellerde Ara</option>
                    <option v-for="hotel in hotels" :key="hotel.id" :value="hotel.id">{{ hotel.name }}</option>
                  </select>
                </div>
                <div class="form-group half">
                  <label>Kişi Sayısı <span class="required">*</span></label>
                  <input v-model.number="searchParams.numberOfPerson" type="number" min="1" required />
                </div>
              </div>
              <button type="submit" class="btn-primary mt-15">
                <CalendarSearch :size="18" /> Müsait Odaları Bul
              </button>
            </form>
          </section>

          <!-- ADIM 2: ODA SEÇİMİ -->
          <section v-if="rooms.length > 0" class="glass-card fade-in">
            <div class="section-header">
              <div class="icon-box emerald-box"><BedDouble :size="24" /></div>
              <h2>2. Oda Seçimi</h2>
            </div>
            
            <div class="list-column">
              <div 
                v-for="room in rooms" 
                :key="room.id" 
                class="item-card" 
                :class="{ 'selected-card': selectedRoom?.id === room.id }"
              >
                <div class="card-info">
                  <h3>
                    <BedDouble :size="20" class="text-slate" />
                    Oda {{ room.roomNumber }}
                    <span class="badge blue-badge">{{ room.roomType }}</span>
                  </h3>
                  <div class="meta-row">
                    <span class="meta-item"><Users :size="16"/> Maks. {{ room.maxCapacity }} Kişi</span>
                  </div>
                </div>
                
                <button 
                  v-if="selectedRoom?.id === room.id" 
                  class="btn-selected" 
                  disabled
                >
                  <CheckCircle2 :size="18" /> Seçildi
                </button>
                <button 
                  v-else 
                  @click="selectRoomForBooking(room)" 
                  class="btn-outline"
                >
                  Bu Odayı Seç
                </button>
              </div>
            </div>
          </section>
        </div>

        <!-- SOL: DİREKT ODA BİLGİSİ VE TARİH SEÇİMİ (Detay Sayfasından Gelindiğinde) -->
        <div v-else class="left-col fade-in">
          <!-- ODA VE OTEL DETAYI -->
          <section class="glass-card mb-24">
            <div class="section-header">
              <div class="icon-box blue-box"><Building2 :size="24" /></div>
              <h2>Oda ve Otel Bilgisi</h2>
            </div>
            
            <div class="room-direct-info">
              <h3 class="hotel-title" v-if="selectedHotel">
                <Building2 :size="18" class="text-slate" />
                {{ selectedHotel.name }}
              </h3>
              <div class="room-details-box mt-10" v-if="selectedRoom">
                <div class="room-main">
                  <BedDouble :size="22" class="text-sky" />
                  <span class="room-number">Oda {{ selectedRoom.roomNumber }}</span>
                  <span class="badge blue-badge">{{ selectedRoom.roomType }}</span>
                </div>
                <div class="room-capacity mt-10">
                  <Users :size="16" class="text-slate" />
                  <span>Maksimum Kapasite: <strong>{{ selectedRoom.maxCapacity }} Kişi</strong></span>
                </div>
              </div>
            </div>
          </section>

          <!-- TARİH SEÇİMİ -->
          <section class="glass-card">
            <div class="section-header">
              <div class="icon-box blue-box"><CalendarPlus :size="24" /></div>
              <h2>Rezervasyon Tarihleri</h2>
            </div>
            
            <div class="search-form">
              <div class="flex-row">
                <div class="form-group half">
                  <label>Giriş Tarihi <span class="required">*</span></label>
                  <input v-model="newReservation.checkInDate" type="date" required />
                </div>
                <div class="form-group half">
                  <label>Çıkış Tarihi <span class="required">*</span></label>
                  <input v-model="newReservation.checkOutDate" type="date" required />
                </div>
              </div>
              <div class="flex-row mt-10">
                <div class="form-group half">
                  <label>Kişi Sayısı <span class="required">*</span></label>
                  <input v-model.number="searchParams.numberOfPerson" type="number" min="1" :max="selectedRoom?.maxCapacity || 10" required />
                </div>
              </div>
            </div>
          </section>
        </div>

        <!-- SAĞ: MİSAFİR BİLGİLERİ -->
        <div class="right-col fade-in" v-if="selectedRoom">
          <section class="glass-card sticky-card">
            <div class="section-header">
              <div class="icon-box rose-box"><UserPlus :size="24" /></div>
              <h2>3. Misafir Bilgileri</h2>
            </div>
            
            <div class="summary-box">
              <div class="summary-item">
                <span class="summary-label">Seçilen Oda</span>
                <span class="summary-value text-sky">Oda {{ selectedRoom.roomNumber }}</span>
              </div>
              <div class="summary-divider"></div>
              <div class="summary-item">
                <span class="summary-label">Konaklama Tarihi</span>
                <span class="summary-value" v-if="newReservation.checkInDate && newReservation.checkOutDate">
                  {{ newReservation.checkInDate }} ➔ {{ newReservation.checkOutDate }}
                </span>
                <span class="summary-value" v-else-if="searchParams.checkInDate && searchParams.checkOutDate">
                  {{ searchParams.checkInDate }} ➔ {{ searchParams.checkOutDate }}
                </span>
                <span class="summary-value text-slate" v-else>Tarih seçilmedi</span>
              </div>
            </div>
            
            <form @submit.prevent="submitReservation">
              <div class="dynamic-guests mt-20">
                <div class="guests-header">
                  <label>Konaklayacak Misafirler</label>
                  <button 
                    type="button" 
                    class="btn-outline-sm" 
                    @click="addGuestField"
                    :disabled="selectedRoom && newReservation.guests.length >= selectedRoom.maxCapacity"
                  >
                    <UserPlus :size="14" /> Yeni Kişi Ekle
                  </button>
                </div>
                
                <div v-for="(g, index) in newReservation.guests" :key="index" class="guest-row">
                  <div class="guest-number">{{ index + 1 }}</div>
                  <input v-model="g.firstname" placeholder="Ad" required class="flex-1" />
                  <input v-model="g.lastname" placeholder="Soyad" required class="flex-1" />
                  <button 
                    v-if="newReservation.guests.length > 1" 
                    type="button" 
                    class="btn-icon-danger" 
                    @click="removeGuestField(index)"
                    title="Misafiri Sil"
                  >
                    <Trash2 :size="18" />
                  </button>
                </div>
              </div>
              
              <button type="submit" class="btn-success mt-20">
                <CheckCircle2 :size="20" /> Rezervasyonu Tamamla
              </button>
            </form>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Genel Yerleşim ve Arka Plan */
.dashboard-layout { 
  min-height: 100vh; 
  background-color: #f8fafc; 
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', system-ui, sans-serif; 
  color: #1e293b; 
  padding: 40px 20px; 
}

/* Arka Plan Efektleri */
.bg-shape { position: absolute; border-radius: 50%; filter: blur(80px); z-index: 0; opacity: 0.3; animation: float 10s infinite ease-in-out alternate; }
.shape-1 { top: -5%; left: -5%; width: 400px; height: 400px; background: #38bdf8; }
.shape-2 { bottom: 10%; right: -5%; width: 500px; height: 500px; background: #fb7185; animation-delay: -3s; }
@keyframes float { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(20px, 30px) scale(1.05); } }

.content-wrapper { position: relative; z-index: 1; max-width: 1400px; margin: 0 auto; }

/* Başlık */
.header { text-align: center; margin-bottom: 40px; animation: fadeInDown 0.5s ease-out; }
.header h1 { font-size: 2.4rem; font-weight: 800; color: #0f172a; margin-bottom: 8px; letter-spacing: -0.5px; }
.subtitle { color: #64748b; font-size: 1.1rem; }

/* Grid Sistemi */
.grid-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; transition: all 0.4s ease; }
.single-col { grid-template-columns: minmax(auto, 800px); justify-content: center; }

/* Glassmorphism Ortak Sınıfı */
.glass-card { 
  background: rgba(255, 255, 255, 0.8); 
  backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); 
  border: 1px solid rgba(255, 255, 255, 1); 
  border-radius: 20px; 
  padding: 30px; 
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05); 
}
.mb-24 { margin-bottom: 24px; }
.mt-10 { margin-top: 10px; }
.mt-15 { margin-top: 15px; }
.mt-20 { margin-top: 20px; }

/* Başlıklar ve İkonlar */
.section-header { display: flex; align-items: center; gap: 15px; margin-bottom: 25px; padding-bottom: 15px; border-bottom: 1px solid #e2e8f0; }
.section-header h2 { font-size: 1.4rem; font-weight: 700; color: #0f172a; margin: 0; }
.icon-box { width: 45px; height: 45px; display: flex; align-items: center; justify-content: center; border-radius: 12px; }
.blue-box { background: #e0f2fe; color: #0284c7; }
.emerald-box { background: #d1fae5; color: #059669; }
.rose-box { background: #ffe4e6; color: #e11d48; }

/* Form Elemanları */
.flex-row { display: flex; gap: 20px; }
.half { flex: 1; }
.form-group { margin-bottom: 15px; position: relative; }
label { display: block; margin-bottom: 8px; color: #475569; font-size: 0.95rem; font-weight: 600; }
.required { color: #e11d48; }

input, select { 
  width: 100%; padding: 12px 15px; background: #ffffff; border: 1px solid #cbd5e1; 
  border-radius: 10px; color: #1e293b; font-size: 1rem; transition: all 0.3s; box-sizing: border-box; outline: none; 
}
input:focus, select:focus { border-color: #38bdf8; box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2); }
.input-with-icon { position: relative; }
.input-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.pl-40 { padding-left: 40px; }

/* Butonlar */
.btn-primary, .btn-success { 
  display: flex; align-items: center; justify-content: center; gap: 10px; width: 100%; 
  padding: 14px; border: none; border-radius: 10px; color: white; 
  font-weight: 600; font-size: 1.05rem; cursor: pointer; transition: all 0.2s; 
}
.btn-primary { background: #0ea5e9; box-shadow: 0 4px 6px -1px rgba(14, 165, 233, 0.2); }
.btn-primary:hover { background: #0284c7; transform: translateY(-2px); }
.btn-success { background: #10b981; box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.2); }
.btn-success:hover { background: #059669; transform: translateY(-2px); }

.btn-outline { 
  background: #ffffff; border: 1px solid #bae6fd; color: #0ea5e9; 
  padding: 10px 20px; border-radius: 10px; cursor: pointer; font-weight: 600; transition: all 0.2s; 
}
.btn-outline:hover { background: #0ea5e9; color: white; }
.btn-selected { 
  display: flex; align-items: center; gap: 6px; background: #f0fdf4; border: 1px solid #bbf7d0; 
  color: #059669; padding: 10px 20px; border-radius: 10px; font-weight: 600; cursor: default; 
}

/* Oda Listesi Kartları */
.list-column { display: flex; flex-direction: column; gap: 15px; max-height: 500px; overflow-y: auto; padding-right: 5px; }
.list-column::-webkit-scrollbar { width: 6px; }
.list-column::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }

.item-card { 
  background: #ffffff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 20px; 
  display: flex; justify-content: space-between; align-items: center; transition: all 0.3s; 
}
.item-card:hover { border-color: #bae6fd; box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.05); }
.selected-card { border-color: #0ea5e9; background: #f0f9ff; box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.2); }

.card-info h3 { margin: 0 0 10px 0; font-size: 1.2rem; color: #0f172a; display: flex; align-items: center; gap: 8px; }
.text-slate { color: #64748b; }
.badge { padding: 4px 10px; border-radius: 8px; font-size: 0.8rem; font-weight: 600; }
.blue-badge { background: #e0f2fe; color: #0284c7; }
.meta-row { color: #64748b; font-size: 0.95rem; }
.meta-item { display: flex; align-items: center; gap: 6px; }

/* Misafir Özeti (Sağ Panel) */
.sticky-card { position: sticky; top: 24px; }
.summary-box { background: #f8fafc; border: 1px solid #e2e8f0; padding: 20px; border-radius: 12px; display: flex; flex-direction: column; gap: 12px; border-left: 4px solid #0ea5e9; }
.summary-item { display: flex; justify-content: space-between; align-items: center; }
.summary-label { color: #64748b; font-size: 0.95rem; font-weight: 600; }
.summary-value { color: #0f172a; font-weight: 700; font-size: 1.05rem; }
.text-sky { color: #0ea5e9; }
.summary-divider { height: 1px; background: #e2e8f0; }

/* Dinamik Misafir Formu */
.dynamic-guests { background: #f8fafc; border: 1px solid #e2e8f0; padding: 20px; border-radius: 12px; }
.guests-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.guests-header label { margin: 0; }

.btn-outline-sm { 
  display: flex; align-items: center; gap: 6px; background: #ffffff; border: 1px solid #cbd5e1; 
  color: #475569; padding: 6px 12px; border-radius: 8px; cursor: pointer; font-size: 0.85rem; font-weight: 600; transition: all 0.2s; 
}
.btn-outline-sm:hover { background: #f1f5f9; color: #0f172a; border-color: #94a3b8; }
.btn-outline-sm:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  border-color: #e2e8f0;
  cursor: not-allowed;
  opacity: 0.6;
}

.guest-row { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.guest-number { background: #e2e8f0; color: #475569; width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; border-radius: 50%; font-size: 0.85rem; font-weight: bold; flex-shrink: 0; }
.flex-1 { flex: 1; }

.btn-icon-danger { 
  background: #fff1f2; color: #e11d48; border: 1px solid #fecdd3; 
  width: 42px; height: 42px; border-radius: 10px; display: flex; align-items: center; justify-content: center; 
  cursor: pointer; transition: all 0.2s; flex-shrink: 0; 
}
.btn-icon-danger:hover { background: #e11d48; color: white; }

/* Animasyonlar */
.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-15px); } to { opacity: 1; transform: translateY(0); } }

/* Oda ve Otel Bilgisi (Direkt Rezervasyon) */
.room-direct-info {
  background: #f8fafc;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}
.hotel-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #0f172a;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px 0;
}
.room-details-box {
  background: #ffffff;
  padding: 15px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}
.room-main {
  display: flex;
  align-items: center;
  gap: 10px;
}
.room-number {
  font-size: 1.15rem;
  font-weight: 700;
  color: #0f172a;
}
.room-capacity {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95rem;
  color: #475569;
  margin-top: 10px;
}

/* Mobil Uyumluluk */
@media (max-width: 900px) { 
  .grid-layout { grid-template-columns: 1fr; } 
  .flex-row { flex-direction: column; gap: 15px; } 
  .item-card { flex-direction: column; align-items: flex-start; gap: 15px; } 
  .item-card button { width: 100%; }
}
</style>