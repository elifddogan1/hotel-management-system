<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { guestService, type GuestQueryDetail, type GuestSearchParams } from '../services/guestService';
import { Users, Filter, Ticket, BedDouble, Calendar, TicketX, Loader2, SearchX, ArrowUpDown } from 'lucide-vue-next';

const guests = ref<GuestQueryDetail[]>([]);
const isLoading = ref(true);

const searchParams = ref<GuestSearchParams>({ lastname: '', voucherNumber: '', sortBy: 'id', direction: 'asc' });

const executeSearch = async () => {
  isLoading.value = true;
  try {
    const response = await guestService.getGuests(searchParams.value);
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
        <p class="subtitle">Sistemdeki tüm konaklama kayıtlarını listeleyin, arayın ve iptal işlemlerini yönetin.</p>
      </header>

      <section class="glass-card top-bar">
        <div class="filter-header">
          <Filter :size="20" class="text-sky" />
          <h2>Arama Seçenekleri & Sıralama</h2>
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
          <p>Misafir listesi ve rezervasyonlar güncelleniyor...</p>
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
                  <span class="hotel-name">{{ guest.hotelName || 'Otel Bilgisi Yok' }}</span>
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
              <button @click="handleCancelVoucher(guest.voucherNumber)" class="btn-danger">
                <TicketX :size="18" />
                <span class="action-text">Rezervasyonu İptal Et</span>
              </button>
            </div>
            
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
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
.top-bar { margin-bottom: 30px; }
.filter-header { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid #e2e8f0; }
.filter-header h2 { font-size: 1.15rem; font-weight: 700; color: #0f172a; margin: 0; }
.text-sky { color: #0ea5e9; }

.search-bar { display: flex; gap: 15px; flex-wrap: wrap; align-items: center; }
.input-group { flex: 1; min-width: 180px; }
input, select { width: 100%; padding: 12px 15px; background: #ffffff; border: 1px solid #cbd5e1; border-radius: 10px; color: #1e293b; font-size: 0.95rem; transition: all 0.3s; box-sizing: border-box; outline: none; }
input:focus, select:focus { border-color: #38bdf8; box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2); }
input::placeholder { color: #94a3b8; }

.btn-primary { display: flex; align-items: center; justify-content: center; gap: 8px; background: #0ea5e9; border: none; border-radius: 10px; color: white; font-weight: 600; font-size: 1rem; padding: 12px 24px; cursor: pointer; transition: all 0.2s; box-shadow: 0 4px 6px -1px rgba(14, 165, 233, 0.2); }
.btn-primary:hover { background: #0284c7; transform: translateY(-1px); }

.btn-danger { display: flex; align-items: center; justify-content: center; gap: 8px; background: #fff1f2; border: 1px solid #fecdd3; color: #e11d48; padding: 10px 16px; border-radius: 10px; cursor: pointer; font-weight: 600; font-size: 0.9rem; transition: all 0.2s; }
.btn-danger:hover { background: #e11d48; color: white; border-color: #e11d48; box-shadow: 0 4px 12px rgba(225, 29, 72, 0.15); }

/* LİSTE DÜZENİ */
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

.state-container { text-align: center; padding: 60px 20px; color: #64748b; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.spinner-icon { animation: spin 1s linear infinite; margin-bottom: 15px; color: #0ea5e9; }
.empty-icon { color: #cbd5e1; margin-bottom: 15px; }
.empty-text { font-size: 1.1rem; color: #475569; font-weight: 500; }

.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-15px); } to { opacity: 1; transform: translateY(0); } }
@keyframes spin { to { transform: rotate(360deg); } }

/* Mobil Uyum */
@media (max-width: 900px) {
  .list-item { flex-direction: column; align-items: flex-start; }
  .item-info-group { gap: 20px; }
  .card-actions { width: 100%; display: flex; }
  .btn-danger { flex: 1; justify-content: center; }
}
@media (max-width: 600px) { 
  .search-bar { flex-direction: column; align-items: stretch; }
  .input-group { width: 100%; }
  .guest-details { flex-direction: column; align-items: flex-start; gap: 12px; }
  .action-text { display: none; }
}
</style>