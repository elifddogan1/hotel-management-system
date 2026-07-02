<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { guestService, type Guest, type GuestSearchParams } from '../services/guestService';
// Profesyonel ve canlı tasarımlar için ikon setimizi dahil ettik
import { Users, Filter, Ticket, BedDouble, Calendar, TicketX, Loader2, SearchX, ArrowUpDown } from 'lucide-vue-next';

const guests = ref<Guest[]>([]);
const isLoading = ref(true);

const searchParams = ref<GuestSearchParams>({ lastName: '', voucherNumber: '', sortBy: 'id', direction: 'asc' });

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
              <button @click="handleCancelVoucher(guest.voucherNumber)" class="btn-danger">
                <TicketX :size="18" />
                <span>Rezervasyonu İptal Et</span>
              </button>
            </div>
          </div>
        </div>
      </section>
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
</style>