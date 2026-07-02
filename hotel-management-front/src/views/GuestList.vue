<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { guestService, type Guest, type GuestSearchParams, type GuestCreationRequest } from '../services/guestService';
import { roomService, type Room } from '../services/roomService';
// Profesyonel ve canlı tasarımlar için ikon setimizi dahil ettik
import { Users, Filter, Ticket, BedDouble, Calendar, TicketX, Loader2, SearchX, ArrowUpDown, Pencil, X, Plus, Trash2 } from 'lucide-vue-next';

const guests = ref<Guest[]>([]);
const isLoading = ref(true);

const searchParams = ref<GuestSearchParams>({ lastName: '', voucherNumber: '', sortBy: 'id', direction: 'asc' });

const isEditModalOpen = ref(false);
const editReservationForm = ref<GuestCreationRequest>({
  voucherNumber: '',
  roomId: 0,
  checkInDate: '',
  checkOutDate: '',
  guests: []
});
const hotelRooms = ref<Room[]>([]);
const editErrorMessage = ref('');

const executeSearch = async () => {
  isLoading.value = true;
  try {
    guests.value = await guestService.getGuests(searchParams.value);
  } catch (error) {
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

const handleCancelVoucher = async (voucher: string) => {
  if (!confirm(`${voucher} numaralı rezervasyon iptal edilecek. Emin misiniz?`)) return;
  try {
    await guestService.cancelReservation(voucher);
    executeSearch(); // Listeyi yenile
  } catch (error) {
    alert('İptal işlemi başarısız.');
  }
};

const openEditReservationModal = async (guest: Guest) => {
  editErrorMessage.value = '';
  try {
    const resGuests = await guestService.getGuests({ voucherNumber: guest.voucherNumber });
    if (resGuests.length === 0 || !resGuests[0]) return;
    
    const firstGuest = resGuests[0];
    const hId = firstGuest.room?.hotelId;
    if (hId) {
      hotelRooms.value = await roomService.getRoomsByHotelId(hId);
    } else {
      hotelRooms.value = [];
    }

    editReservationForm.value = {
      voucherNumber: firstGuest.voucherNumber,
      roomId: firstGuest.room?.id || 0,
      checkInDate: firstGuest.checkInDate,
      checkOutDate: firstGuest.checkOutDate,
      guests: resGuests.map(g => ({ firstname: g.firstname, lastname: g.lastname }))
    };
    isEditModalOpen.value = true;
  } catch (error) {
    alert('Rezervasyon detayları yüklenirken hata oluştu.');
    console.error(error);
  }
};

const closeEditModal = () => {
  isEditModalOpen.value = false;
};

const addGuestToEditForm = () => {
  editReservationForm.value.guests.push({ firstname: '', lastname: '' });
};

const removeGuestFromEditForm = (index: number) => {
  if (editReservationForm.value.guests.length > 1) {
    editReservationForm.value.guests.splice(index, 1);
  } else {
    alert('En az bir misafir bulunmalıdır.');
  }
};

const handleUpdateReservation = async () => {
  editErrorMessage.value = '';
  if (!editReservationForm.value.roomId || !editReservationForm.value.checkInDate || !editReservationForm.value.checkOutDate) {
    alert('Lütfen gerekli tüm alanları doldurun!');
    return;
  }
  
  if (editReservationForm.value.guests.some(g => !g.firstname || !g.lastname)) {
    alert('Lütfen tüm misafir bilgilerini doldurun!');
    return;
  }

  try {
    await guestService.updateReservation(editReservationForm.value.voucherNumber, editReservationForm.value);
    isEditModalOpen.value = false;
    await executeSearch();
  } catch (error: any) {
    if (error.response && error.response.data && error.response.data.message) {
      editErrorMessage.value = error.response.data.message;
    } else {
      editErrorMessage.value = 'Rezervasyon güncellenirken bir hata oluştu.';
    }
    console.error(error);
  }
};

onMounted(() => {
  executeSearch();
});
</script>

<template>
  <div class="dashboard-layout">
    <!-- Cam efektinin arkasında parlayan yumuşak renkli geometrik şekiller -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <div class="content-wrapper">
      <header class="header">
        <h1>Misafir & Rezervasyon Yönetimi</h1>
        <p class="subtitle">Sistemdeki aktif konaklama kayıtlarını listeleyin, arayın ve rezervasyon iptallerini yönetin.</p>
      </header>

      <!-- FİLTRELEME VE ARAMA PANELİ -->
      <section class="glass-card top-bar">
        <div class="filter-header">
          <Filter :size="20" class="text-sky" />
          <h2>Gelişmiş Filtreleme ve Sıralama</h2>
        </div>
        
        <div class="search-bar">
          <div class="input-group">
            <input v-model="searchParams.lastName" placeholder="Soyada göre ara..." @keyup.enter="executeSearch" />
          </div>
          <div class="input-group">
            <input v-model="searchParams.voucherNumber" placeholder="Voucher No ile ara..." @keyup.enter="executeSearch" />
          </div>
          <div class="input-group select-wrapper">
            <select v-model="searchParams.sortBy">
              <option value="id">Kayıt Sırasına Göre</option>
              <option value="lastname">Soyada Göre</option>
              <option value="checkInDate">Giriş Tarihine Göre</option>
            </select>
          </div>
          <div class="input-group select-wrapper">
            <select v-model="searchParams.direction">
              <option value="asc">Artan Sıralama (A-Z)</option>
              <option value="desc">Azalan Sıralama (Z-A)</option>
            </select>
          </div>
          <button @click="executeSearch" class="btn-primary">
            <ArrowUpDown :size="18" /> Filtrele
          </button>
        </div>
      </section>

      <!-- MİSAFİR LİSTESİ -->
      <section class="list-section">
        <!-- Yükleniyor Animasyonu (Spinner) -->
        <div v-if="isLoading" class="state-container glass-card">
          <Loader2 class="spinner-icon" :size="48" />
          <p>Rezervasyon kayıtları güncelleniyor...</p>
        </div>
        
        <!-- Boş Sonuç Ekranı -->
        <div v-else-if="guests.length === 0" class="state-container glass-card fade-in">
          <SearchX :size="48" class="empty-icon" />
          <p class="empty-text">Aradığınız kriterlere uygun herhangi bir misafir kaydı bulunamadı.</p>
        </div>

        <!-- Kart Listesi Grid Yapısı -->
        <div v-else class="list-grid fade-in">
          <div v-for="guest in guests" :key="guest.id" class="item-card">
            <div class="card-info">
              <div class="guest-name-row">
                <div class="avatar-icon"><Users :size="20" /></div>
                <h3>{{ guest.firstname }} {{ guest.lastname }}</h3>
              </div>
              
              <div class="voucher-box">
                <Ticket :size="16" class="text-sky" />
                <span class="voucher-text">{{ guest.voucherNumber }}</span>
              </div>
              
              <div class="meta-rows">
                <p class="meta">
                  <BedDouble :size="16" class="meta-icon" /> 
                  <span>Oda Bilgisi: <strong>Oda {{ guest.room?.roomNumber || 'Atanmamış' }}</strong></span>
                </p>
                <p class="meta">
                  <Calendar :size="16" class="meta-icon" /> 
                  <span>Konaklama Süresi: <strong>{{ guest.checkInDate }} ➔ {{ guest.checkOutDate }}</strong></span>
                </p>
              </div>
            </div>
            
            <div class="card-actions">
              <button @click="openEditReservationModal(guest)" class="btn-manage-res" style="margin-bottom: 8px;">
                <Pencil :size="18" />
                <span>Rezervasyonu Düzenle</span>
              </button>
              <button @click="handleCancelVoucher(guest.voucherNumber)" class="btn-danger">
                <TicketX :size="18" />
                <span>Rezervasyonu İptal Et</span>
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- RESERVATION EDIT MODAL -->
    <div v-if="isEditModalOpen" class="modal-overlay" @click.self="closeEditModal">
      <div class="glass-card modal-content" style="max-width: 600px; max-height: 90vh; overflow-y: auto;">
        <div class="modal-header">
          <h2>Rezervasyonu Düzenle ({{ editReservationForm.voucherNumber }})</h2>
          <button @click="closeEditModal" class="btn-close">
            <X :size="20" />
          </button>
        </div>

        <div v-if="editErrorMessage" class="error-banner">
          <p>{{ editErrorMessage }}</p>
        </div>

        <form @submit.prevent="handleUpdateReservation">
          <div class="form-row">
            <div class="form-group half">
              <label>Giriş Tarihi <span class="required">*</span></label>
              <input v-model="editReservationForm.checkInDate" type="date" required />
            </div>
            <div class="form-group half">
              <label>Çıkış Tarihi <span class="required">*</span></label>
              <input v-model="editReservationForm.checkOutDate" type="date" required />
            </div>
          </div>

          <div class="form-group">
            <label>Oda Seçimi <span class="required">*</span></label>
            <select v-model="editReservationForm.roomId" required>
              <option value="" disabled>Lütfen bir oda seçin</option>
              <option v-for="room in hotelRooms" :key="room.id" :value="room.id">
                Oda {{ room.roomNumber }} - {{ room.roomType }} (Maks: {{ room.maxCapacity }} Kişi)
              </option>
            </select>
          </div>

          <div class="guests-section">
            <div class="section-header-sub">
              <h3>Misafirler (Kişi Sayısı: {{ editReservationForm.guests.length }})</h3>
              <button type="button" @click="addGuestToEditForm" class="btn-add-guest">
                <Plus :size="16" /> Misafir Ekle
              </button>
            </div>

            <div v-for="(g, index) in editReservationForm.guests" :key="index" class="guest-input-row">
              <span class="guest-num">#{{ index + 1 }}</span>
              <input v-model="g.firstname" type="text" placeholder="Adı" required class="guest-input" />
              <input v-model="g.lastname" type="text" placeholder="Soyadı" required class="guest-input" />
              <button 
                type="button" 
                @click="removeGuestFromEditForm(index)" 
                class="btn-remove-guest" 
                title="Misafiri Çıkar"
                :disabled="editReservationForm.guests.length <= 1"
              >
                <Trash2 :size="16" />
              </button>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" @click="closeEditModal" class="btn-secondary">İptal</button>
            <button type="submit" class="btn-primary">Rezervasyonu Güncelle</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Genel Arka Plan ve Düzen */
.dashboard-layout { 
  min-height: 100vh; 
  background-color: #f8fafc; /* Profesyonel kırık beyaz */
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', system-ui, sans-serif; 
  color: #1e293b; 
  padding: 40px 20px; 
}

/* Canlı Parlama Efektleri (Buzlu Cam Arkası) */
.bg-shape { position: absolute; border-radius: 50%; filter: blur(80px); z-index: 0; opacity: 0.3; animation: float 10s infinite ease-in-out alternate; }
.shape-1 { top: -5%; left: -5%; width: 450px; height: 450px; background: #38bdf8; }
.shape-2 { bottom: -10%; right: 10%; width: 450px; height: 450px; background: #fb7185; animation-delay: -4s; }
@keyframes float { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(20px, 30px) scale(1.05); } }

.content-wrapper { position: relative; z-index: 1; max-width: 1200px; margin: 0 auto; }

/* Başlık Alanı */
.header { text-align: center; margin-bottom: 40px; animation: fadeInDown 0.5s ease-out; }
.header h1 { font-size: 2.4rem; font-weight: 800; color: #0f172a; margin-bottom: 8px; letter-spacing: -0.5px; }
.subtitle { color: #64748b; font-size: 1.1rem; max-width: 700px; margin: 0 auto; }

/* Glassmorphism Filtre Kartı */
.glass-card { 
  background: rgba(255, 255, 255, 0.8); 
  backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); 
  border: 1px solid rgba(255, 255, 255, 1); 
  border-radius: 20px; 
  padding: 24px; 
}
.top-bar { margin-bottom: 30px; box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05); }
.filter-header { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid #e2e8f0; }
.filter-header h2 { font-size: 1.15rem; font-weight: 700; color: #0f172a; margin: 0; }
.text-sky { color: #0ea5e9; }

/* Arama Çubuğu Yapısı */
.search-bar { display: flex; gap: 15px; flex-wrap: wrap; align-items: center; }
.input-group { flex: 1; min-width: 180px; }

input, select { 
  width: 100%; padding: 12px 15px; background: #ffffff; border: 1px solid #cbd5e1; 
  border-radius: 10px; color: #1e293b; font-size: 0.95rem; transition: all 0.3s; box-sizing: border-box; outline: none; 
}
input:focus, select:focus { border-color: #38bdf8; box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2); }
input::placeholder { color: #94a3b8; }

/* Butonlar */
.btn-primary { 
  display: flex; align-items: center; justify-content: center; gap: 8px;
  background: #0ea5e9; border: none; border-radius: 10px; color: white; 
  font-weight: 600; font-size: 1rem; padding: 12px 24px; cursor: pointer; transition: all 0.2s;
  box-shadow: 0 4px 6px -1px rgba(14, 165, 233, 0.2);
}
.btn-primary:hover { background: #0284c7; transform: translateY(-1px); }

.btn-danger { 
  display: flex; align-items: center; justify-content: center; gap: 8px; width: 100%;
  background: #fff1f2; border: 1px solid #fecdd3; color: #e11d48; 
  padding: 11px 16px; border-radius: 10px; cursor: pointer; font-weight: 600; font-size: 0.9rem; transition: all 0.2s; 
}
.btn-danger:hover { background: #e11d48; color: white; border-color: #e11d48; box-shadow: 0 4px 12px rgba(225, 29, 72, 0.15); }

/* Misafir Kartları Grid Yapısı */
.list-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: 24px; }

.item-card { 
  background: #ffffff; border: 1px solid #e2e8f0; border-radius: 18px; padding: 24px; 
  display: flex; flex-direction: column; justify-content: space-between; gap: 20px;
  transition: all 0.3s ease; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
}
.item-card:hover { border-color: #bae6fd; transform: translateY(-4px); box-shadow: 0 12px 20px -5px rgba(0, 0, 0, 0.06); }

/* Kart Detay İçerikleri */
.guest-name-row { display: flex; align-items: center; gap: 12px; }
.avatar-icon { background: #f1f5f9; color: #475569; width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.item-card h3 { font-size: 1.3rem; font-weight: 700; color: #0f172a; margin: 0; }

.voucher-box { display: inline-flex; align-items: center; gap: 6px; background: #f0f9ff; padding: 6px 12px; border-radius: 8px; margin-top: 4px; border: 1px dashed #bae6fd; }
.voucher-text { color: #0284c7; font-weight: 700; font-family: monospace; font-size: 1rem; }

.meta-rows { display: flex; flex-direction: column; gap: 10px; margin-top: 5px; }
.meta { display: flex; align-items: center; gap: 8px; color: #475569; font-size: 0.95rem; margin: 0; }
.meta-icon { color: #94a3b8; }
.meta strong { color: #0f172a; }

/* Yükleniyor ve Boş Liste Alanları */
.state-container { text-align: center; padding: 60px 20px; color: #64748b; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.spinner-icon { animation: spin 1s linear infinite; margin-bottom: 15px; color: #0ea5e9; }
.empty-icon { color: #cbd5e1; margin-bottom: 15px; }
.empty-text { font-size: 1.1rem; color: #475569; font-weight: 500; }

/* Animasyon Geçişleri */
.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-15px); } to { opacity: 1; transform: translateY(0); } }
@keyframes spin { to { transform: rotate(360deg); } }

/* Mobil Uyumluluk */
@media (max-width: 600px) { 
  .search-bar { flex-direction: column; align-items: stretch; }
  .input-group { width: 100%; }
}

/* Modal Tasarımı */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(15, 23, 42, 0.3);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease;
}
.modal-content {
  width: 90%;
  max-width: 600px;
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 15px;
}
.modal-header h2 {
  font-size: 1.4rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}
.btn-close {
  background: transparent;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-close:hover {
  background: #f1f5f9;
  color: #0f172a;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 25px;
  padding-top: 15px;
  border-top: 1px solid #e2e8f0;
}
.btn-secondary {
  padding: 10px 20px;
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  color: #475569;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}
.btn-secondary:hover {
  background: #e2e8f0;
  color: #0f172a;
}

/* Edit Reservation Button */
.btn-manage-res {
  display: flex; align-items: center; justify-content: center; gap: 8px; width: 100%;
  padding: 11px 16px;
  background: #f0f9ff; 
  border: 1px solid #bae6fd; 
  color: #0284c7; 
  border-radius: 10px; 
  cursor: pointer; 
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.2s; 
}
.btn-manage-res:hover { background: #0ea5e9; color: white; border-color: #0ea5e9; }

/* Form Group Grid */
.form-row {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}
.form-group.half {
  flex: 1;
}
.form-group {
  margin-bottom: 20px;
}
label {
  display: block;
  margin-bottom: 8px;
  color: #475569;
  font-size: 0.95rem;
  font-weight: 600;
}
.required {
  color: #e11d48;
}

/* Sub Sections for Guests */
.guests-section {
  background: #f8fafc;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  margin-top: 20px;
}
.section-header-sub {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.section-header-sub h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}
.btn-add-guest {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #16a34a;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-add-guest:hover {
  background: #16a34a;
  color: white;
}

.guest-input-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.guest-num {
  font-weight: 700;
  color: #64748b;
  min-width: 25px;
}
.guest-input {
  flex: 1;
}
.btn-remove-guest {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background: #fff1f2;
  border: 1px solid #fecdd3;
  color: #e11d48;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-remove-guest:hover:not(:disabled) {
  background: #e11d48;
  color: white;
  border-color: #e11d48;
}
.btn-remove-guest:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Error Banner */
.error-banner {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #ef4444;
  padding: 12px 16px;
  border-radius: 10px;
  margin-bottom: 20px;
}
.error-banner p {
  margin: 0;
  font-weight: 600;
  font-size: 0.95rem;
}

@keyframes scaleUp {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>