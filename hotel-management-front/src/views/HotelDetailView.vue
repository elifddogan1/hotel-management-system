<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { hotelService, type Hotel } from '../services/hotelService';
import { roomService, type Room, type RoomCreationRequest } from '../services/roomService';
import { guestService, type Guest } from '../services/guestService';
// Profesyonel ikonlarımızı ekledik
import { ArrowLeft, MapPin, Phone, BedDouble, CalendarCheck, PlusCircle, Trash2, TicketX, Loader2, Building2 } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const hotelId = Number(route.params.id);

const hotel = ref<Hotel | null>(null);
const rooms = ref<Room[]>([]);
const guests = ref<Guest[]>([]);

const activeTab = ref<'rooms' | 'reservations'>('rooms');
const isLoading = ref(true);
const errorMessage = ref('');

const newRoom = ref<RoomCreationRequest>({
  roomNumber: '',
  roomType: 'STANDARD',
  maxCapacity: 2
});

const loadHotelData = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const fetchedHotels = await hotelService.getAllHotels();
    const foundHotel = fetchedHotels.find(h => h.id === hotelId);
    
    if (!foundHotel) {
      errorMessage.value = 'Otel bulunamadı.';
      return;
    }
    hotel.value = foundHotel;

    const [fetchedRooms, fetchedGuests] = await Promise.all([
      roomService.getRoomsByHotelId(hotelId),
      guestService.getGuestsByHotelId(hotelId)
    ]);
    rooms.value = fetchedRooms;
    guests.value = fetchedGuests;
  } catch (error) {
    errorMessage.value = 'Veriler yüklenirken bir hata oluştu.';
  } finally {
    isLoading.value = false;
  }
};

const handleCreateRoom = async () => {
  if (!newRoom.value.roomNumber) {
    alert('Lütfen oda numarasını girin!');
    return;
  }
  try {
    await roomService.createRoomOnHotel(hotelId, newRoom.value);
    newRoom.value = { roomNumber: '', roomType: 'STANDARD', maxCapacity: 2 };
    rooms.value = await roomService.getRoomsByHotelId(hotelId);
  } catch (error) {
    alert('Oda eklenirken hata oluştu.');
  }
};

const handleDeleteRoom = async (id: number | undefined) => {
  if (!id || !confirm('Bu odayı silmek istediğinize emin misiniz?')) return;
  try {
    await roomService.deleteRoom(id);
    rooms.value = await roomService.getRoomsByHotelId(hotelId);
  } catch (error) {
    alert('Oda silinemedi.');
  }
};

const handleCancelVoucher = async (voucherNumber: string) => {
  if (!confirm(`Voucher No: ${voucherNumber} olan rezervasyonu iptal etmek istediğinize emin misiniz?`)) return;
  try {
    await guestService.cancelReservation(voucherNumber);
    guests.value = await guestService.getGuestsByHotelId(hotelId);
  } catch (error) {
    alert('Rezervasyon iptal edilemedi.');
  }
};

onMounted(() => {
  loadHotelData();
});
</script>

<template>
  <div class="detail-container">
    <!-- Arka plan dekoratif elementleri -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <div v-if="isLoading" class="state-container full-height">
      <Loader2 class="spinner-icon" :size="48" />
      <p>Otel detayları yükleniyor...</p>
    </div>

    <div v-else-if="errorMessage" class="state-container full-height">
      <p class="error-text">{{ errorMessage }}</p>
      <button @click="router.push('/hotels')" class="btn-primary mt-20" style="max-width: 250px; margin: 20px auto;">
        Otellere Geri Dön
      </button>
    </div>

    <div v-else-if="hotel" class="content-wrapper">
      
      <!-- OTEL BİLGİLERİ (HEADER) -->
      <header class="hotel-header glass-card">
        <button @click="router.push('/hotels')" class="btn-back">
          <ArrowLeft :size="18" />
          <span>Otel Listesine Dön</span>
        </button>
        <div class="hotel-title-section">
          <div class="title-row">
            <div class="icon-box blue-box">
              <Building2 :size="32" stroke-width="1.5" />
            </div>
            <div>
              <h1>{{ hotel.name }}</h1>
              <div class="meta-row">
                <span class="meta-item"><MapPin :size="16" /> {{ hotel.location }}</span>
                <span v-if="hotel.contactInfo" class="meta-item"><Phone :size="16" /> {{ hotel.contactInfo }}</span>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- SEKMELER (TABS) -->
      <div class="tab-control glass-card">
        <button 
          @click="activeTab = 'rooms'" 
          :class="['tab-btn', { active: activeTab === 'rooms' }]"
        >
          <BedDouble :size="20" />
          <span>Odaları Yönet</span>
        </button>
        <button 
          @click="activeTab = 'reservations'" 
          :class="['tab-btn', { active: activeTab === 'reservations' }]"
        >
          <CalendarCheck :size="20" />
          <span>Rezervasyonları Göster</span>
        </button>
      </div>

      <!-- İÇERİK ALANI -->
      <div class="tab-content fade-in">
        
        <!-- ODALAR SEKMESİ -->
        <div v-if="activeTab === 'rooms'" class="grid-layout">
          <!-- Sol: Yeni Oda Formu -->
          <section class="glass-card form-section">
            <div class="section-header">
              <div class="icon-box emerald-box">
                <PlusCircle :size="22" />
              </div>
              <h2>Yeni Oda Tanımla</h2>
            </div>
            
            <form @submit.prevent="handleCreateRoom">
              <div class="form-group">
                <label>Oda Numarası / Adı <span class="required">*</span></label>
                <input v-model="newRoom.roomNumber" type="text" placeholder="Örn: 101, A Blok-1" required />
              </div>
              <div class="form-group">
                <label>Oda Tipi <span class="required">*</span></label>
                <select v-model="newRoom.roomType" required>
                  <option value="STANDARD">Standard Room</option>
                  <option value="DELUXE">Deluxe Room</option>
                  <option value="SUITE">Suite Room</option>
                  <option value="FAMILY">Family Room</option>
                </select>
              </div>
              <div class="form-group">
                <label>Maksimum Kapasite <span class="required">*</span></label>
                <input v-model.number="newRoom.maxCapacity" type="number" min="1" max="10" required />
              </div>
              <button type="submit" class="btn-primary">
                Odayı Otelle İlişkilendir
              </button>
            </form>
          </section>

          <!-- Sağ: Mevcut Odalar -->
          <section class="glass-card list-section">
            <div class="section-header">
              <div class="icon-box blue-box">
                <BedDouble :size="22" />
              </div>
              <h2>Mevcut Odalar <span class="count-badge">{{ rooms.length }}</span></h2>
            </div>

            <div v-if="rooms.length === 0" class="state-container empty-state">
              <BedDouble :size="40" class="empty-icon" />
              <p>Bu otele ait henüz hiç oda tanımlanmamış.</p>
            </div>
            
            <div v-else class="list-grid">
              <div v-for="room in rooms" :key="room.id" class="item-card clickable"
                @click="router.push(`/hotels/${hotelId}/rooms/${room.id}`)">
                <div class="card-info">
                  <h3>Oda {{ room.roomNumber }}</h3>
                  <div class="tags">
                    <span class="badge blue-badge">{{ room.roomType }}</span>
                    <span class="badge slate-badge">👥 {{ room.maxCapacity }} Kişi</span>
                  </div>
                </div>
                <div class="card-actions-right">
                  <button @click.stop="handleDeleteRoom(room.id)" class="btn-icon-danger" title="Odayı Sil">
                    <Trash2 :size="18" />
                  </button>
                </div>
              </div>
            </div>
          </section>
        </div>

        <!-- REZERVASYONLAR SEKMESİ -->
        <div v-if="activeTab === 'reservations'" class="glass-card full-width">
          <div class="section-header">
            <div class="icon-box rose-box">
              <CalendarCheck :size="22" />
            </div>
            <h2>Aktif Rezervasyon Kayıtları <span class="count-badge rose">{{ guests.length }}</span></h2>
          </div>

          <div v-if="guests.length === 0" class="state-container empty-state">
            <CalendarCheck :size="40" class="empty-icon" />
            <p>Bu otele ait kayıtlı bir misafir veya rezervasyon bulunamadı.</p>
          </div>
          
          <div v-else class="list-grid triple">
            <div v-for="guest in guests" :key="guest.id" class="item-card guest-card">
              <div class="guest-info">
                <h3>{{ guest.firstname }} {{ guest.lastname }}</h3>
                <div class="voucher-box">
                  <span class="voucher-label">VOUCHER</span>
                  <span class="voucher-number">{{ guest.voucherNumber }}</span>
                </div>
                <div class="date-row">
                  <div class="date-col">
                    <span class="date-label">Giriş</span>
                    <span class="date-val">{{ guest.checkInDate }}</span>
                  </div>
                  <div class="date-sep">➔</div>
                  <div class="date-col">
                    <span class="date-label">Çıkış</span>
                    <span class="date-val">{{ guest.checkOutDate }}</span>
                  </div>
                </div>
              </div>
              <button @click="handleCancelVoucher(guest.voucherNumber)" class="btn-danger-outline">
                <TicketX :size="16" />
                <span>Rezervasyonu İptal Et</span>
              </button>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
/* Genel Yerleşim */
.detail-container { 
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

.content-wrapper { position: relative; z-index: 1; max-width: 1200px; margin: 0 auto; display: flex; flex-direction: column; gap: 24px; }

/* Glassmorphism Ortak Sınıfı */
.glass-card { 
  background: rgba(255, 255, 255, 0.8); 
  backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); 
  border: 1px solid rgba(255, 255, 255, 1); 
  border-radius: 20px; 
  padding: 30px; 
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05); 
}

/* Header & Geri Butonu */
.hotel-header { padding: 25px 30px; animation: fadeInDown 0.5s ease-out; }
.btn-back { 
  display: inline-flex; align-items: center; gap: 8px; 
  background: #f1f5f9; border: 1px solid #e2e8f0; color: #475569; 
  padding: 8px 16px; border-radius: 10px; cursor: pointer; font-weight: 600; font-size: 0.9rem;
  margin-bottom: 20px; transition: all 0.2s; 
}
.btn-back:hover { background: #e2e8f0; color: #0f172a; transform: translateX(-4px); }

.title-row { display: flex; align-items: center; gap: 20px; }
.hotel-title-section h1 { font-size: 2.2rem; font-weight: 800; margin: 0 0 8px 0; color: #0f172a; letter-spacing: -0.5px; }
.meta-row { display: flex; gap: 20px; color: #64748b; font-size: 0.95rem; flex-wrap: wrap; }
.meta-item { display: flex; align-items: center; gap: 6px; }

/* Ortak İkon Kutuları */
.icon-box { width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; border-radius: 14px; }
.icon-box.blue-box { background: #e0f2fe; color: #0284c7; }
.icon-box.emerald-box { background: #d1fae5; color: #059669; }
.icon-box.rose-box { background: #ffe4e6; color: #e11d48; }

/* Sekmeler (Tabs) */
.tab-control { display: flex; gap: 16px; padding: 12px; }
.tab-btn { 
  flex: 1; display: flex; align-items: center; justify-content: center; gap: 10px; 
  padding: 16px; background: transparent; border: none; border-radius: 12px; 
  color: #64748b; font-weight: 600; font-size: 1.05rem; cursor: pointer; transition: all 0.3s ease; 
}
.tab-btn:hover { background: #f1f5f9; color: #334155; }
.tab-btn.active { background: #0ea5e9; color: white; box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3); transform: translateY(-2px); }

/* İçerik Animasyonu */
.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-15px); } to { opacity: 1; transform: translateY(0); } }

/* Grid Düzeni */
.grid-layout { display: grid; grid-template-columns: 1fr 2fr; gap: 24px; align-items: start; }
.full-width { width: 100%; }

/* Başlıklar ve Badge'ler */
.section-header { display: flex; align-items: center; gap: 15px; margin-bottom: 25px; padding-bottom: 15px; border-bottom: 1px solid #e2e8f0; }
.section-header h2 { font-size: 1.4rem; font-weight: 700; color: #0f172a; display: flex; align-items: center; gap: 10px; margin: 0; }
.count-badge { background: #e0f2fe; color: #0284c7; font-size: 0.9rem; padding: 4px 12px; border-radius: 20px; }
.count-badge.rose { background: #ffe4e6; color: #e11d48; }

/* Formlar */
.form-group { margin-bottom: 20px; }
label { display: block; margin-bottom: 8px; color: #475569; font-size: 0.95rem; font-weight: 600; }
.required { color: #e11d48; }
input, select { 
  width: 100%; padding: 12px 15px; background: #ffffff; border: 1px solid #cbd5e1; 
  border-radius: 10px; color: #1e293b; font-size: 1rem; transition: all 0.3s; box-sizing: border-box; outline: none; 
}
input:focus, select:focus { border-color: #38bdf8; box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2); }

/* Butonlar */
.btn-primary { 
  width: 100%; padding: 14px; background: #0ea5e9; border: none; border-radius: 10px; 
  color: white; font-weight: 600; font-size: 1rem; cursor: pointer; transition: all 0.2s; 
  box-shadow: 0 4px 6px -1px rgba(14, 165, 233, 0.2); 
}
.btn-primary:hover { background: #0284c7; transform: translateY(-2px); box-shadow: 0 6px 8px -1px rgba(14, 165, 233, 0.3); }

/* Listeler ve Kartlar */
.list-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.list-grid.triple { grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); }

.item-card { 
  background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; 
  display: flex; justify-content: space-between; align-items: center; transition: all 0.3s; 
}
.clickable { cursor: pointer; }
.clickable:hover { transform: translateY(-4px); border-color: #bae6fd; box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.05); }

.card-info h3 { margin: 0 0 10px 0; font-size: 1.2rem; color: #0f172a; }
.tags { display: flex; gap: 8px; flex-wrap: wrap; }
.badge { padding: 4px 10px; border-radius: 8px; font-size: 0.8rem; font-weight: 600; }
.blue-badge { background: #f0f9ff; color: #0284c7; border: 1px solid #bae6fd; }
.slate-badge { background: #f8fafc; color: #475569; border: 1px solid #e2e8f0; }

.btn-icon-danger { 
  background: #fff1f2; color: #e11d48; border: 1px solid #fecdd3; 
  width: 36px; height: 36px; border-radius: 8px; display: flex; align-items: center; justify-content: center; 
  cursor: pointer; transition: all 0.2s; 
}
.btn-icon-danger:hover { background: #e11d48; color: white; }

/* Misafir Kartı (Rezervasyonlar) */
.guest-card { flex-direction: column; align-items: stretch; gap: 16px; border-top: 4px solid #0ea5e9; }
.guest-info h3 { margin: 0 0 12px 0; font-size: 1.3rem; color: #0f172a; }
.voucher-box { background: #f8fafc; border: 1px dashed #cbd5e1; padding: 8px 12px; border-radius: 8px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; }
.voucher-label { font-size: 0.75rem; font-weight: 700; color: #64748b; letter-spacing: 1px; }
.voucher-number { font-size: 1.1rem; font-weight: 800; color: #0ea5e9; font-family: monospace; }

.date-row { display: flex; align-items: center; justify-content: space-between; background: #f1f5f9; padding: 10px 15px; border-radius: 10px; }
.date-col { display: flex; flex-direction: column; gap: 4px; }
.date-label { font-size: 0.75rem; color: #64748b; font-weight: 600; text-transform: uppercase; }
.date-val { font-weight: 600; color: #1e293b; font-size: 0.95rem; }
.date-sep { color: #94a3b8; font-size: 1.2rem; }

.btn-danger-outline { 
  display: flex; align-items: center; justify-content: center; gap: 8px; width: 100%; 
  background: transparent; border: 1px solid #ef4444; color: #ef4444; 
  padding: 10px; border-radius: 10px; cursor: pointer; font-weight: 600; transition: all 0.2s; 
}
.btn-danger-outline:hover { background: #ef4444; color: white; }

/* Durum Sınıfları */
.state-container { text-align: center; padding: 60px 20px; color: #64748b; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.state-container.full-height { min-height: 50vh; }
.spinner-icon { animation: spin 1s linear infinite; margin-bottom: 15px; color: #0ea5e9; }
.empty-icon { color: #cbd5e1; margin-bottom: 15px; }
.error-text { color: #e11d48; font-size: 1.1rem; font-weight: 600; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Mobil Uyumluluk */
@media (max-width: 992px) { 
  .grid-layout { grid-template-columns: 1fr; } 
  .form-section { order: 2; } .list-section { order: 1; }
  .tab-control { flex-direction: column; }
}
</style>