<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { roomService, type Room } from '../services/roomService';
import { guestService, type Guest } from '../services/guestService';
// Profesyonel tasarıma uygun vektörel ikonlarımızı dahil ettik
import { ArrowLeft, BedDouble, Users, CalendarPlus, CalendarClock, Loader2, CheckCircle2, XCircle, UserCircle } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();

const roomId = Number(route.params.roomId);
const hotelId = Number(route.params.hotelId);

const room = ref<Room | null>(null);
const reservations = ref<Guest[]>([]);
const isLoading = ref(true);

// V-Calendar için rezervasyon aralıklarını işaretle
const attributes = computed(() => {
  return reservations.value.map(res => ({
    key: `res-${res.id}`,
    highlight: { color: 'red', fillMode: 'solid' },
    dates: { 
      start: new Date(res.checkInDate), 
      end: new Date(res.checkOutDate) 
    },
    popover: { label: `${res.firstname} ${res.lastname}` }
  }));
});

const loadData = async () => {
  isLoading.value = true;
  try {
    const allRooms = await roomService.getRoomsByHotelId(hotelId);
    room.value = allRooms.find(r => r.id === roomId) || null;
    reservations.value = await guestService.getGuestsByRoomId(roomId);
  } catch (error) {
    console.error("Veri yüklenirken hata:", error);
  } finally {
    isLoading.value = false;
  }
};

const isRoomCurrentlyOccupied = computed(() => {
  const today = new Date();
  return reservations.value.some(res => {
    return today >= new Date(res.checkInDate) && today <= new Date(res.checkOutDate);
  });
});

const goToReservation = () => {
  router.push({ name: 'reservation', query: { hotelId: hotelId, roomId: roomId } });
};

onMounted(loadData);
</script>

<template>
  <div class="dashboard-layout">
    <!-- Cam katmanların arkasında yumuşak parlayan derinlik baloncukları -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <div class="content-wrapper">
      <!-- GERİ DÖN BUTONU -->
      <button @click="router.back()" class="btn-back">
        <ArrowLeft :size="18" />
        <span>Geri Dön</span>
      </button>
      
      <!-- YÜKLENİYOR EKRANI -->
      <div v-if="isLoading" class="state-container glass-card">
        <Loader2 class="spinner-icon" :size="48" />
        <p>Oda detayları ve takvim verileri yükleniyor...</p>
      </div>

      <div v-else-if="room" class="content fade-in">
        <!-- ODA ÜST BİLGİ KARTI -->
        <header class="room-header glass-card">
          <div class="header-left">
            <div class="icon-box blue-box">
              <BedDouble :size="32" stroke-width="1.5" />
            </div>
            <div>
              <h1>Oda {{ room.roomNumber }}</h1>
              <span class="badge blue-badge">{{ room.roomType }}</span>
            </div>
          </div>
          <div class="header-right">
            <div class="meta-item">
              <Users :size="20" class="text-slate" />
              <span>Maksimum Kapasite: <strong>{{ room.maxCapacity }} Kişi</strong></span>
            </div>
          </div>
        </header>

        <!-- ANA PANEL GRID DÜZENİ -->
        <div class="main-grid">
          <!-- SOL PANEL: TAKVİM VE DURUM -->
          <section class="calendar-section glass-card">
            <h2 class="section-title">Oda Doluluk Takvimi</h2>
            <div class="calendar-wrapper">
              <!-- is-dark kaldırılarak aydınlık cam temasına tam uyum sağlandı -->
              <VCalendar 
                :attributes="attributes" 
                expanded 
                borderless 
                transparent
                class="custom-calendar"
              />
            </div>
            <div class="legend">
              <div v-if="isRoomCurrentlyOccupied" class="status-badge occupied">
                <XCircle :size="16" />
                <span>Oda Şu An Dolu (Konaklama Var)</span>
              </div>
              <div v-else class="status-badge available">
                <CheckCircle2 :size="16" />
                <span>Oda Şu An Müsait</span>
              </div>
            </div>
          </section>

          <!-- SAĞ PANEL: HIZLI İŞLEMLER VE GEÇMİŞ/YAKLAŞAN REZERVASYONLAR -->
          <section class="info-section">
            <div class="glass-card inner-card">
              <h2 class="section-title">Hızlı İşlemler</h2>
              <button @click="goToReservation" class="btn-primary">
                <CalendarPlus :size="20" />
                <span>Bu Odaya Rezervasyon Ekle</span>
              </button>
              
              <h2 class="section-title mt-30">Bu Odadaki Konaklamalar</h2>
              <div v-if="reservations.length === 0" class="empty-list">
                <CalendarClock :size="32" class="empty-icon" />
                <p>Bu odaya ait kayıtlı bir rezervasyon geçmişi bulunmuyor.</p>
              </div>
              
              <ul v-else class="res-list">
                <li v-for="res in reservations" :key="res.id" class="res-item">
                  <div class="res-guest">
                    <UserCircle :size="18" class="text-sky" />
                    <strong>{{ res.firstname }} {{ res.lastname }}</strong>
                  </div>
                  <div class="res-dates">
                    <span class="date-badge">{{ res.checkInDate }}</span>
                    <span class="date-sep">➔</span>
                    <span class="date-badge">{{ res.checkOutDate }}</span>
                  </div>
                </li>
              </ul>
            </div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Genel Yapı ve Arka Plan Tasarımı */
.dashboard-layout { 
  min-height: 100vh; 
  background-color: #f8fafc; /* Kırık beyaz standardı */
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', system-ui, sans-serif; 
  color: #1e293b; 
  padding: 40px 20px; 
}

/* Canlı Renk Baloncukları */
.bg-shape { position: absolute; border-radius: 50%; filter: blur(80px); z-index: 0; opacity: 0.3; animation: float 10s infinite ease-in-out alternate; }
.shape-1 { top: -10%; left: -5%; width: 400px; height: 400px; background: #38bdf8; }
.shape-2 { bottom: -10%; right: -5%; width: 500px; height: 500px; background: #10b981; animation-delay: -3s; }
@keyframes float { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(20px, 30px) scale(1.05); } }

.content-wrapper { position: relative; z-index: 1; max-width: 1100px; margin: 0 auto; }

/* Geri Butonu */
.btn-back { 
  display: inline-flex; align-items: center; gap: 8px; 
  background: #ffffff; border: 1px solid #cbd5e1; color: #475569; 
  padding: 10px 18px; border-radius: 10px; cursor: pointer; font-weight: 600; font-size: 0.95rem;
  margin-bottom: 25px; transition: all 0.2s; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}
.btn-back:hover { background: #f1f5f9; color: #0f172a; transform: translateX(-4px); }

/* Glassmorphic Ortak Sınıf */
.glass-card { 
  background: rgba(255, 255, 255, 0.8); 
  backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); 
  border: 1px solid rgba(255, 255, 255, 1); 
  border-radius: 20px; 
  padding: 30px; 
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05); 
}

/* Üst Header Altyapısı */
.room-header { 
  display: flex; justify-content: space-between; align-items: center; 
  margin-bottom: 30px; flex-wrap: wrap; gap: 20px; 
}
.header-left { display: flex; align-items: center; gap: 20px; }
.header-left h1 { margin: 0 0 8px 0; font-size: 2rem; font-weight: 800; color: #0f172a; letter-spacing: -0.5px; }

.icon-box { width: 56px; height: 56px; display: flex; align-items: center; justify-content: center; border-radius: 16px; }
.blue-box { background: #e0f2fe; color: #0284c7; }

.badge { padding: 4px 12px; border-radius: 20px; font-size: 0.85rem; font-weight: 700; letter-spacing: 0.5px; }
.blue-badge { background: #f0f9ff; color: #0284c7; border: 1px solid #bae6fd; }

.meta-item { display: flex; align-items: center; gap: 10px; background: #f8fafc; padding: 12px 20px; border-radius: 12px; border: 1px dashed #cbd5e1; font-size: 1rem; color: #475569; }
.meta-item strong { color: #0f172a; font-size: 1.1rem; }
.text-slate { color: #64748b; }

/* Grid Yerleşimi */
.main-grid { display: grid; grid-template-columns: 1.2fr 1fr; gap: 30px; }
.section-title { font-size: 1.2rem; font-weight: 700; color: #0f172a; margin-bottom: 20px; display: flex; align-items: center; gap: 10px; border-bottom: 1px solid #e2e8f0; padding-bottom: 10px;}
.mt-30 { margin-top: 30px; }

/* VCalendar Stil Ezmeleri (Aydınlık Uyumlu) */
.calendar-wrapper { min-height: 350px; width: 100%; margin-bottom: 20px; }
:deep(.custom-calendar) { font-family: inherit; --vc-bg: transparent; }
:deep(.custom-calendar .vc-title) { color: #0f172a !important; font-weight: 700; font-size: 1.1rem; }
:deep(.custom-calendar .vc-arrow) { color: #0ea5e9 !important; background: transparent; }
:deep(.custom-calendar .vc-arrow:hover) { background: rgba(14, 165, 233, 0.1) !important; }
:deep(.custom-calendar .vc-weekday) { color: #64748b; font-weight: 600; }
:deep(.custom-calendar .vc-day-content:hover) { background: rgba(14, 165, 233, 0.1); }

/* Durum Rozetleri */
.legend { display: flex; justify-content: center; padding-top: 15px; border-top: 1px dashed #e2e8f0; }
.status-badge { display: flex; align-items: center; gap: 8px; padding: 8px 16px; border-radius: 20px; font-weight: 600; font-size: 0.95rem; }
.status-badge.occupied { background: #fef2f2; color: #ef4444; border: 1px solid #fecaca; }
.status-badge.available { background: #f0fdf4; color: #22c55e; border: 1px solid #bbf7d0; }

/* Sağ Panel ve Buton Tasarımı */
.inner-card { padding: 25px; height: 100%; }
.btn-primary { 
  display: flex; align-items: center; justify-content: center; gap: 10px; width: 100%; 
  padding: 14px; background: #0ea5e9; border: none; border-radius: 12px; 
  color: white; font-weight: 600; font-size: 1.05rem; cursor: pointer; transition: all 0.2s; 
  box-shadow: 0 4px 6px -1px rgba(14, 165, 233, 0.2); 
}
.btn-primary:hover { background: #0284c7; transform: translateY(-2px); box-shadow: 0 6px 8px -1px rgba(14, 165, 233, 0.3); }

/* Rezervasyon Kartları Listesi */
.res-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 12px; max-height: 350px; overflow-y: auto; padding-right: 5px; }
.res-list::-webkit-scrollbar { width: 6px; }
.res-list::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }

.res-item { background: #ffffff; padding: 16px; border-radius: 12px; border: 1px solid #e2e8f0; transition: all 0.2s; }
.res-item:hover { border-color: #bae6fd; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.res-guest { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; font-size: 1.05rem; color: #0f172a; }
.text-sky { color: #0ea5e9; }

.res-dates { display: flex; align-items: center; gap: 8px; background: #f8fafc; padding: 8px; border-radius: 8px; }
.date-badge { font-size: 0.85rem; font-weight: 600; color: #475569; }
.date-sep { color: #94a3b8; font-size: 0.9rem; }

/* Durum / Boş Liste Alanları */
.state-container { text-align: center; padding: 60px 20px; color: #64748b; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.spinner-icon { animation: spin 1s linear infinite; margin-bottom: 15px; color: #0ea5e9; }
.empty-list { text-align: center; padding: 30px 10px; color: #64748b; }
.empty-icon { color: #cbd5e1; margin-bottom: 10px; }

/* Giriş Animasyonu */
.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes spin { to { transform: rotate(360deg); } }

/* Mobil Ekran Uyumu */
@media (max-width: 992px) { 
  .main-grid { grid-template-columns: 1fr; } 
  .room-header { flex-direction: column; align-items: flex-start; }
}
</style>