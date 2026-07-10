<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { guestService, type GuestQueryDetail, type GuestSearchParams, type GuestCreationRequest } from '../services/guestService';
import { roomService, type RoomQueryDetail } from '../services/roomService';
import { Users, Filter, Ticket, BedDouble, Calendar, TicketX, Loader2, SearchX, ArrowUpDown, Pencil, X, Plus, Trash2 } from 'lucide-vue-next';

const guests = ref<GuestQueryDetail[]>([]);
const isLoading = ref(true);

const searchParams = ref<GuestSearchParams>({ lastname: '', voucherNumber: '', sortBy: 'id', direction: 'asc' });

const isEditModalOpen = ref(false);
const editReservationForm = ref<GuestCreationRequest>({
  roomId: 0,
  checkInDate: '',
  checkOutDate: '',
  guests: []
});

// Voucher numarası request DTO'sunda olmadığı için güncellemelerde route param olarak tutmak için ayrı bir state ekledik.
const currentEditVoucher = ref('');
const hotelRooms = ref<RoomQueryDetail[]>([]);
const editErrorMessage = ref('');

const executeSearch = async () => {
  isLoading.value = true;
  try {
    const response = await guestService.getGuests(searchParams.value);
    // Yeni servisteki PagedResponse yapısına göre content dizisini alıyoruz.
    guests.value = response.content; 
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
    executeSearch(); 
  } catch (error) {
    alert('İptal işlemi başarısız.');
  }
};

const openEditReservationModal = async (guest: GuestQueryDetail) => {
  editErrorMessage.value = '';
  try {
    const response = await guestService.getGuests({ voucherNumber: guest.voucherNumber });
    const resGuests = response.content;
    
    if (resGuests.length === 0 || !resGuests[0]) return;
    
    const firstGuest = resGuests[0];
    
    // DİKKAT: Yeni GuestQueryDetail DTO'sunda room objesi (ve hotelId/roomId) yok. 
    // Odaları listelemek için geçici olarak tüm odaları çekecek bir servis çağrısı yaptığımızı varsayıyoruz.
    // İdeal olarak DTO'ya hotelId eklenmelidir.
    // hotelRooms.value = await roomService.getRoomsByHotelId(VARSAYILAN_HOTEL_ID); 
    
    currentEditVoucher.value = firstGuest.voucherNumber;

    editReservationForm.value = {
      roomId: 0, // DTO'da gelmediği için kullanıcıdan tekrar seçmesi istenecek veya DTO'yu güncellemelisin.
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
    await guestService.updateReservation(currentEditVoucher.value, editReservationForm.value);
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
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <div class="content-wrapper">
      <header class="header">
        <h1>Misafir & Rezervasyon Yönetimi</h1>
        <p class="subtitle">Sistemdeki aktif konaklama kayıtlarını listeleyin, arayın ve rezervasyon iptallerini yönetin.</p>
      </header>

      <section class="glass-card top-bar">
        <div class="filter-header">
          <Filter :size="20" class="text-sky" />
          <h2>Gelişmiş Filtreleme ve Sıralama</h2>
        </div>
        
        <div class="search-bar">
          <div class="input-group">
            <input v-model="searchParams.lastname" placeholder="Soyada göre ara..." @keyup.enter="executeSearch" />
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

      <section class="list-section">
        <div v-if="isLoading" class="state-container glass-card">
          <Loader2 class="spinner-icon" :size="48" />
          <p>Rezervasyon kayıtları güncelleniyor...</p>
        </div>
        
        <div v-else-if="guests.length === 0" class="state-container glass-card fade-in">
          <SearchX :size="48" class="empty-icon" />
          <p class="empty-text">Aradığınız kriterlere uygun herhangi bir misafir kaydı bulunamadı.</p>
        </div>

        <div v-else class="list-container fade-in">
          <div v-for="guest in guests" :key="guest.id" class="list-item">
            
            <div class="item-info-group">
              
              <div class="guest-primary">
                <div class="avatar-icon"><Users :size="20" /></div>
                <div>
                  <h3>{{ guest.firstname }} {{ guest.lastname }}</h3>
                  <span class="hotel-name">{{ guest.hotelName }}</span>
                </div>
              </div>
              
              <div class="guest-details">
                <div class="voucher-box">
                  <Ticket :size="16" class="text-sky" />
                  <span class="voucher-text">{{ guest.voucherNumber }}</span>
                </div>
                
                <div class="meta">
                  <BedDouble :size="16" class="meta-icon" /> 
                  <span>Oda: <strong>{{ guest.roomNumber || 'Atanmamış' }}</strong></span>
                </div>
                
                <div class="meta">
                  <Calendar :size="16" class="meta-icon" /> 
                  <span><strong>{{ guest.checkInDate }}</strong> ➔ <strong>{{ guest.checkOutDate }}</strong></span>
                </div>
              </div>

            </div>
            
            <div class="card-actions">
              <button @click="openEditReservationModal(guest)" class="btn-manage-res" title="Düzenle">
                <Pencil :size="18" />
                <span class="action-text">Düzenle</span>
              </button>
              <button @click="handleCancelVoucher(guest.voucherNumber)" class="btn-danger" title="İptal Et">
                <TicketX :size="18" />
                <span class="action-text">İptal</span>
              </button>
            </div>

          </div>
        </div>
      </section>
    </div>

    <div v-if="isEditModalOpen" class="modal-overlay" @click.self="closeEditModal">
      <div class="glass-card modal-content" style="max-width: 600px; max-height: 90vh; overflow-y: auto;">
        <div class="modal-header">
          <h2>Rezervasyonu Düzenle ({{ currentEditVoucher }})</h2>
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
              <option value="0" disabled>Lütfen bir oda seçin</option>
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
            <button type="button" @click="closeEditModal" class="btn-secondary">İptal Vazgeç</button>
            <button type="submit" class="btn-primary">Rezervasyonu Güncelle</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Mevcut Genel Stiller (Korundu) */
.dashboard-layout { min-height: 100vh; background-color: #f8fafc; position: relative; overflow: hidden; font-family: 'Segoe UI', system-ui, sans-serif; color: #1e293b; padding: 40px 20px; }
.bg-shape { position: absolute; border-radius: 50%; filter: blur(80px); z-index: 0; opacity: 0.3; animation: float 10s infinite ease-in-out alternate; }
.shape-1 { top: -5%; left: -5%; width: 450px; height: 450px; background: #38bdf8; }
.shape-2 { bottom: -10%; right: 10%; width: 450px; height: 450px; background: #fb7185; animation-delay: -4s; }
@keyframes float { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(20px, 30px) scale(1.05); } }

.content-wrapper { position: relative; z-index: 1; max-width: 1200px; margin: 0 auto; }

.header { text-align: center; margin-bottom: 40px; animation: fadeInDown 0.5s ease-out; }
.header h1 { font-size: 2.4rem; font-weight: 800; color: #0f172a; margin-bottom: 8px; letter-spacing: -0.5px; }
.subtitle { color: #64748b; font-size: 1.1rem; max-width: 700px; margin: 0 auto; }

.glass-card { background: rgba(255, 255, 255, 0.8); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); border: 1px solid rgba(255, 255, 255, 1); border-radius: 20px; padding: 24px; }
.top-bar { margin-bottom: 30px; box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05); }
.filter-header { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid #e2e8f0; }
.filter-header h2 { font-size: 1.15rem; font-weight: 700; color: #0f172a; margin: 0; }
.text-sky { color: #0ea5e9; }

.search-bar { display: flex; gap: 15px; flex-wrap: wrap; align-items: center; }
.input-group { flex: 1; min-width: 180px; }
input, select { width: 100%; padding: 12px 15px; background: #ffffff; border: 1px solid #cbd5e1; border-radius: 10px; color: #1e293b; font-size: 0.95rem; transition: all 0.3s; box-sizing: border-box; outline: none; }
input:focus, select:focus { border-color: #38bdf8; box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2); }

.btn-primary { display: flex; align-items: center; justify-content: center; gap: 8px; background: #0ea5e9; border: none; border-radius: 10px; color: white; font-weight: 600; font-size: 1rem; padding: 12px 24px; cursor: pointer; transition: all 0.2s; box-shadow: 0 4px 6px -1px rgba(14, 165, 233, 0.2); }
.btn-primary:hover { background: #0284c7; transform: translateY(-1px); }

.btn-danger { display: flex; align-items: center; justify-content: center; gap: 8px; background: #fff1f2; border: 1px solid #fecdd3; color: #e11d48; padding: 11px 16px; border-radius: 10px; cursor: pointer; font-weight: 600; font-size: 0.9rem; transition: all 0.2s; }
.btn-danger:hover { background: #e11d48; color: white; border-color: #e11d48; box-shadow: 0 4px 12px rgba(225, 29, 72, 0.15); }

/* --- YENİ LİSTE DÜZENİ (GRID YERİNE) --- */
.list-container { display: flex; flex-direction: column; gap: 16px; }

.list-item { 
  background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px 24px; 
  display: flex; align-items: center; justify-content: space-between; gap: 20px;
  transition: all 0.3s ease; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
}
.list-item:hover { 
  border-color: #bae6fd; 
  transform: translateX(4px); 
  box-shadow: 0 8px 15px -5px rgba(0, 0, 0, 0.06); 
}

.item-info-group { display: flex; align-items: center; gap: 40px; flex: 1; flex-wrap: wrap; }

.guest-primary { display: flex; align-items: center; gap: 16px; min-width: 220px; }
.avatar-icon { background: #f1f5f9; color: #475569; width: 44px; height: 44px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.guest-primary h3 { font-size: 1.15rem; font-weight: 700; color: #0f172a; margin: 0 0 4px 0; }
.hotel-name { font-size: 0.85rem; color: #64748b; font-weight: 500; }

.guest-details { display: flex; align-items: center; gap: 30px; flex-wrap: wrap; flex: 1; }

.voucher-box { display: inline-flex; align-items: center; gap: 6px; background: #f0f9ff; padding: 6px 12px; border-radius: 8px; border: 1px dashed #bae6fd; white-space: nowrap; }
.voucher-text { color: #0284c7; font-weight: 700; font-family: monospace; font-size: 0.95rem; }

.meta { display: flex; align-items: center; gap: 8px; color: #475569; font-size: 0.95rem; margin: 0; white-space: nowrap; }
.meta-icon { color: #94a3b8; }
.meta strong { color: #0f172a; }

.card-actions { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.btn-manage-res { display: flex; align-items: center; justify-content: center; gap: 8px; padding: 11px 16px; background: #f0f9ff; border: 1px solid #bae6fd; color: #0284c7; border-radius: 10px; cursor: pointer; font-weight: 600; font-size: 0.9rem; transition: all 0.2s; }
.btn-manage-res:hover { background: #0ea5e9; color: white; border-color: #0ea5e9; }

/* Responsive Düzenlemeler */
@media (max-width: 900px) {
  .list-item { flex-direction: column; align-items: flex-start; }
  .item-info-group { gap: 20px; }
  .card-actions { width: 100%; display: flex; }
  .btn-manage-res, .btn-danger { flex: 1; }
}
@media (max-width: 600px) {
  .search-bar { flex-direction: column; align-items: stretch; }
  .input-group { width: 100%; }
  .guest-details { flex-direction: column; align-items: flex-start; gap: 12px; }
  .action-text { display: none; } /* Mobilde ikonlar kalsın metin gitsin */
}

/* Modallar ve Yardımcı Sınıflar (Korundu) */
.state-container { text-align: center; padding: 60px 20px; color: #64748b; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.spinner-icon { animation: spin 1s linear infinite; margin-bottom: 15px; color: #0ea5e9; }
.empty-icon { color: #cbd5e1; margin-bottom: 15px; }
.empty-text { font-size: 1.1rem; color: #475569; font-weight: 500; }

.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-15px); } to { opacity: 1; transform: translateY(0); } }
@keyframes spin { to { transform: rotate(360deg); } }

/* Modal CSS */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(15, 23, 42, 0.3); backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px); z-index: 100; display: flex; align-items: center; justify-content: center; animation: fadeIn 0.3s ease; }
.modal-content { width: 90%; max-width: 600px; animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #e2e8f0; padding-bottom: 15px; }
.modal-header h2 { font-size: 1.4rem; font-weight: 700; color: #0f172a; margin: 0; }
.btn-close { background: transparent; border: none; color: #64748b; cursor: pointer; padding: 5px; border-radius: 50%; transition: all 0.2s; display: flex; align-items: center; justify-content: center; }
.btn-close:hover { background: #f1f5f9; color: #0f172a; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 25px; padding-top: 15px; border-top: 1px solid #e2e8f0; }
.btn-secondary { padding: 10px 20px; background: #f1f5f9; border: 1px solid #cbd5e1; color: #475569; border-radius: 10px; cursor: pointer; font-weight: 600; transition: all 0.2s; }
.btn-secondary:hover { background: #e2e8f0; color: #0f172a; }
.form-row { display: flex; gap: 15px; margin-bottom: 20px; }
.form-group.half { flex: 1; }
.form-group { margin-bottom: 20px; }
label { display: block; margin-bottom: 8px; color: #475569; font-size: 0.95rem; font-weight: 600; }
.required { color: #e11d48; }

.guests-section { background: #f8fafc; padding: 20px; border-radius: 12px; border: 1px solid #e2e8f0; margin-top: 20px; }
.section-header-sub { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.section-header-sub h3 { font-size: 1.1rem; font-weight: 700; color: #0f172a; margin: 0; }
.btn-add-guest { display: flex; align-items: center; gap: 6px; padding: 8px 12px; background: #f0fdf4; border: 1px solid #bbf7d0; color: #16a34a; border-radius: 8px; font-weight: 600; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; }
.btn-add-guest:hover { background: #16a34a; color: white; }
.guest-input-row { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.guest-num { font-weight: 700; color: #64748b; min-width: 25px; }
.guest-input { flex: 1; }
.btn-remove-guest { display: flex; align-items: center; justify-content: center; padding: 10px; background: #fff1f2; border: 1px solid #fecdd3; color: #e11d48; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
.btn-remove-guest:hover:not(:disabled) { background: #e11d48; color: white; border-color: #e11d48; }
.btn-remove-guest:disabled { opacity: 0.5; cursor: not-allowed; }

.error-banner { background: #fef2f2; border: 1px solid #fecaca; color: #ef4444; padding: 12px 16px; border-radius: 10px; margin-bottom: 20px; }
.error-banner p { margin: 0; font-weight: 600; font-size: 0.95rem; }

@keyframes scaleUp { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>