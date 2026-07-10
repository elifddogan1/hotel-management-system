<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { roomService, type RoomQueryDetail } from '../services/roomService';
// Profesyonel ikonlarımızı ekledik
import { Search, Building2, Users, BedDouble, ChevronRight, Loader2, SearchX } from 'lucide-vue-next';

const router = useRouter();
const rooms = ref<RoomQueryDetail[]>([]);
const isLoading = ref(true);

// Filtre stateleri
const filterCapacity = ref<number | ''>('');
const filterType = ref<string>('');

const loadData = async () => {
  isLoading.value = true;
  try {
    const fetchedRooms = await roomService.getAllRooms();
    rooms.value = fetchedRooms;
  } catch (error) {
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

// Frontend üzerinde anlık filtreleme
const filteredRooms = computed(() => {
  return rooms.value.filter(room => {
    const matchCapacity = filterCapacity.value === '' || room.maxCapacity >= Number(filterCapacity.value);
    const matchType = filterType.value === '' || room.roomType.toLowerCase().includes(filterType.value.toLowerCase());
    return matchCapacity && matchType;
  });
});

const goToDetail = (room: RoomQueryDetail) => {
  router.push({ name: 'RoomDetail', params: { hotelId: room.hotelId, roomId: room.id } });
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="dashboard-layout">
    <!-- Arka plan dekoratif elementleri -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <div class="content-wrapper">
      <header class="header">
        <h1>Oda Rehberi</h1>
        <p class="subtitle">Sistemdeki tüm odaları filtreleyin ve detaylarını inceleyin.</p>
      </header>

      <!-- Arama ve Filtreleme Alanı -->
      <div class="filter-bar glass-card">
        <div class="filter-header">
          <Search :size="20" class="text-sky" />
          <h2>Hızlı Arama</h2>
        </div>
        
        <div class="filter-grid">
          <div class="form-group">
            <label>Minimum Kapasite</label>
            <input v-model="filterCapacity" type="number" min="1" placeholder="Örn: 2 Kişilik" />
          </div>
          <div class="form-group">
            <label>Oda Tipi</label>
            <input v-model="filterType" type="text" placeholder="Örn: Suite, Standard, Family..." />
          </div>
        </div>
      </div>

      <!-- Yükleniyor Durumu -->
      <div v-if="isLoading" class="state-container">
        <Loader2 class="spinner-icon" :size="48" />
        <p>Oda rehberi güncelleniyor...</p>
      </div>

      <!-- Filtre Sonucu Boşsa -->
      <div v-else-if="filteredRooms.length === 0" class="state-container glass-card fade-in">
        <SearchX :size="48" class="empty-icon" />
        <p class="empty-text">Aradığınız kriterlere uygun oda bulunamadı.</p>
        <button @click="filterCapacity=''; filterType=''" class="btn-clear">
          Filtreleri Temizle
        </button>
      </div>

      <!-- Oda Listesi -->
      <div v-else class="list-grid fade-in">
        <div 
          v-for="room in filteredRooms" 
          :key="room.id" 
          class="item-card clickable"
          @click="goToDetail(room)"
        >
          <div class="card-header">
            <h3>
              <BedDouble :size="20" class="text-slate" />
              Oda {{ room.roomNumber }}
            </h3>
            <span class="badge blue-badge">{{ room.roomType }}</span>
          </div>
          
          <div class="card-body">
            <p class="meta-info">
              <Building2 :size="16" class="meta-icon" />
              <span>{{ room.hotelName || 'Bilinmeyen Otel' }}</span>
            </p>
            <p class="meta-info">
              <Users :size="16" class="meta-icon" />
              <span>Maks. Kapasite: <strong>{{ room.maxCapacity }} Kişi</strong></span>
            </p>
          </div>
          
          <div class="card-footer">
            <span class="click-hint">Detayları Gör</span>
            <ChevronRight :size="18" class="hint-icon" />
          </div>
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

/* Arka Plan Efektleri (Buzlu Cam Arkası) */
.bg-shape { position: absolute; border-radius: 50%; filter: blur(80px); z-index: 0; opacity: 0.3; animation: float 10s infinite ease-in-out alternate; }
.shape-1 { top: -5%; left: 10%; width: 400px; height: 400px; background: #38bdf8; }
.shape-2 { bottom: 10%; right: -5%; width: 450px; height: 450px; background: #10b981; animation-delay: -3s; }
@keyframes float { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(20px, 30px) scale(1.05); } }

.content-wrapper { position: relative; z-index: 1; max-width: 1000px; margin: 0 auto; }

/* Başlık */
.header { text-align: center; margin-bottom: 40px; animation: fadeInDown 0.5s ease-out; }
.header h1 { font-size: 2.4rem; font-weight: 800; color: #0f172a; margin-bottom: 8px; letter-spacing: -0.5px; }
.subtitle { color: #64748b; font-size: 1.1rem; }

/* Glassmorphism Ortak Sınıfı */
.glass-card { 
  background: rgba(255, 255, 255, 0.8); 
  backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); 
  border: 1px solid rgba(255, 255, 255, 1); 
  border-radius: 20px; 
  padding: 30px; 
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05); 
}

/* Filtreleme Çubuğu */
.filter-bar { margin-bottom: 40px; }
.filter-header { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #e2e8f0; }
.filter-header h2 { font-size: 1.2rem; font-weight: 700; color: #0f172a; margin: 0; }
.text-sky { color: #0ea5e9; }

.filter-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; }
.form-group { display: flex; flex-direction: column; }
label { margin-bottom: 8px; color: #475569; font-size: 0.95rem; font-weight: 600; }

input { 
  width: 100%; padding: 12px 15px; background: #ffffff; border: 1px solid #cbd5e1; 
  border-radius: 10px; color: #1e293b; font-size: 1rem; transition: all 0.3s; box-sizing: border-box; outline: none; 
}
input:focus { border-color: #38bdf8; box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2); }
input::placeholder { color: #94a3b8; }

/* Oda Kartları Grid */
.list-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }

.item-card { 
  background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 24px; 
  display: flex; flex-direction: column; transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); 
}
.item-card.clickable { cursor: pointer; }
.item-card.clickable:hover { transform: translateY(-8px); border-color: #bae6fd; box-shadow: 0 15px 30px -10px rgba(0, 0, 0, 0.1); }

/* Kart İçi Tasarım */
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #f1f5f9; }
.card-header h3 { font-size: 1.3rem; font-weight: 700; color: #0f172a; margin: 0; display: flex; align-items: center; gap: 10px; }
.text-slate { color: #64748b; }

.badge { padding: 4px 12px; border-radius: 20px; font-size: 0.8rem; font-weight: 600; letter-spacing: 0.5px; }
.blue-badge { background: #e0f2fe; color: #0284c7; border: 1px solid #bae6fd; }

.card-body { flex-grow: 1; display: flex; flex-direction: column; gap: 12px; margin-bottom: 20px; }
.meta-info { display: flex; align-items: center; gap: 10px; color: #475569; font-size: 0.95rem; margin: 0; }
.meta-icon { color: #94a3b8; }
.meta-info strong { color: #0f172a; }

.card-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 15px; border-top: 1px dashed #e2e8f0; color: #0ea5e9; transition: all 0.2s; }
.click-hint { font-size: 0.95rem; font-weight: 600; }
.hint-icon { transition: transform 0.2s; }
.item-card:hover .card-footer { color: #0284c7; }
.item-card:hover .hint-icon { transform: translateX(5px); }

/* Durum Bildirimleri (Yükleniyor / Boş) */
.state-container { text-align: center; padding: 60px 20px; color: #64748b; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.spinner-icon { animation: spin 1s linear infinite; margin-bottom: 15px; color: #0ea5e9; }
.empty-icon { color: #cbd5e1; margin-bottom: 15px; }
.empty-text { font-size: 1.1rem; margin-bottom: 20px; color: #475569; }
.btn-clear { background: #f1f5f9; border: 1px solid #cbd5e1; color: #475569; padding: 10px 20px; border-radius: 8px; cursor: pointer; font-weight: 600; transition: all 0.2s; }
.btn-clear:hover { background: #e2e8f0; color: #0f172a; }

/* Animasyonlar */
.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-15px); } to { opacity: 1; transform: translateY(0); } }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .filter-grid { grid-template-columns: 1fr; }
}
</style>