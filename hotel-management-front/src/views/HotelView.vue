<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { hotelService, type Hotel } from '../services/hotelService';
// Profesyonel ikonları projemize dahil ediyoruz
import { Building, MapPin, Phone, Settings2, Trash2, PlusCircle, Loader2 } from 'lucide-vue-next';

const router = useRouter();
const hotels = ref<Hotel[]>([]);
const isLoading = ref(true);
const errorMessage = ref('');

const newHotel = ref<Hotel>({
  name: '',
  location: '',
  contactInfo: ''
});

const loadHotels = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    hotels.value = await hotelService.getAllHotels();
  } catch (error) {
    errorMessage.value = 'Oteller yüklenirken bir hata oluştu.';
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

const handleCreateHotel = async () => {
  if (!newHotel.value.name || !newHotel.value.location) {
    alert('Lütfen gerekli alanları doldurun!');
    return;
  }

  try {
    await hotelService.createHotel(newHotel.value);
    newHotel.value = { name: '', location: '', contactInfo: '' };
    await loadHotels();
  } catch (error) {
    alert('Otel eklenirken bir hata oluştu.');
    console.error(error);
  }
};

const handleDeleteHotel = async (id: number | undefined) => {
  if (!id) return;
  if (!confirm('Bu oteli silmek istediğinize emin misiniz?')) return;

  try {
    await hotelService.deleteHotel(id);
    await loadHotels();
  } catch (error) {
    alert('Otel silinirken bir hata oluştu.');
    console.error(error);
  }
};

onMounted(() => {
  loadHotels();
});
</script>

<template>
  <div class="hotel-dashboard">
    <!-- Arka plan dekoratif elementleri -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <div class="content-wrapper">
      <header class="header">
        <h1>Otel Yönetim Merkezi</h1>
        <p class="subtitle">Sisteme kayıtlı otelleri yönetin ve yeni tesisler ekleyin</p>
      </header>

      <div class="grid-layout">
        <!-- SOL: YENİ OTEL EKLEME FORMU -->
        <section class="glass-card form-section">
          <div class="section-header">
            <div class="icon-box blue-box">
              <PlusCircle :size="24" />
            </div>
            <h2>Yeni Otel Kaydı</h2>
          </div>
          
          <form @submit.prevent="handleCreateHotel">
            <div class="form-group">
              <label for="name">Otel Adı <span class="required">*</span></label>
              <input 
                v-model="newHotel.name" 
                type="text" 
                id="name" 
                placeholder="Örn: Akdeniz Luxury Resort" 
                required
              />
            </div>

            <div class="form-group">
              <label for="location">Konum / Şehir <span class="required">*</span></label>
              <input 
                v-model="newHotel.location" 
                type="text" 
                id="location" 
                placeholder="Örn: Antalya / Konyaaltı" 
                required
              />
            </div>

            <div class="form-group">
              <label for="contact">İletişim Bilgisi</label>
              <textarea 
                v-model="newHotel.contactInfo" 
                id="contact" 
                placeholder="Telefon, E-posta veya Adres bilgisi..."
                rows="3"
              ></textarea>
            </div>

            <button type="submit" class="btn-primary">
              Oteli Sisteme Kaydet
            </button>
          </form>
        </section>

        <!-- SAĞ: KAYITLI OTELLER LİSTESİ -->
        <section class="glass-card list-section">
          <div class="section-header">
            <div class="icon-box emerald-box">
              <Building :size="24" />
            </div>
            <h2>Kayıtlı Oteller</h2>
          </div>

          <div v-if="isLoading" class="state-container loading-state">
            <Loader2 class="spinner-icon" :size="40" />
            <p>Veritabanından oteller çekiliyor...</p>
          </div>

          <div v-else-if="errorMessage" class="state-container error-state">
            <p>{{ errorMessage }}</p>
          </div>

          <div v-else-if="hotels.length === 0" class="state-container empty-state">
            <Building :size="48" class="empty-icon" />
            <p>Sisteme henüz hiçbir otel tanımlanmamış.<br>Soldaki formdan ilk otelinizi ekleyin!</p>
          </div>

          <div v-else class="hotel-grid">
            <div v-for="hotel in hotels" :key="hotel.id" class="hotel-card">
              <div class="card-content">
                <h3>{{ hotel.name }}</h3>
                <div class="info-row">
                  <MapPin :size="16" class="info-icon" />
                  <p class="location-tag">{{ hotel.location }}</p>
                </div>
                <div v-if="hotel.contactInfo" class="info-row">
                  <Phone :size="16" class="info-icon" />
                  <p class="contact-tag">{{ hotel.contactInfo }}</p>
                </div>
              </div>
              
              <div class="card-actions">
                <button @click="router.push(`/hotels/${hotel.id}`)" class="btn-manage">
                  <Settings2 :size="18" />
                  <span>Yönet</span>
                </button>
                <button @click="handleDeleteHotel(hotel.id)" class="btn-danger" title="Oteli Sil">
                  <Trash2 :size="18" />
                </button>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Genel Yerleşim */
.hotel-dashboard {
  min-height: 100vh;
  background-color: #f8fafc;
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', system-ui, sans-serif;
  color: #1e293b;
}

/* Arka Plan Efektleri */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.3;
  animation: float 10s infinite ease-in-out alternate;
}
.shape-1 { top: -5%; left: -5%; width: 400px; height: 400px; background: #38bdf8; }
.shape-2 { bottom: 10%; right: -5%; width: 500px; height: 500px; background: #10b981; animation-delay: -3s; }

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(20px, 30px) scale(1.05); }
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Başlık */
.header { text-align: center; margin-bottom: 40px; animation: fadeInDown 0.6s ease-out; }
.header h1 { font-size: 2.4rem; font-weight: 800; color: #0f172a; margin-bottom: 8px; letter-spacing: -0.5px; }
.subtitle { color: #64748b; font-size: 1.1rem; }

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-15px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Izgara Tasarımı */
.grid-layout { 
  display: grid; 
  grid-template-columns: 1fr 2fr; 
  gap: 30px; 
  align-items: start;
}

/* Glassmorphism Kartlar */
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 1);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e2e8f0;
}

.section-header h2 { font-size: 1.4rem; font-weight: 700; color: #0f172a; }

.icon-box {
  width: 45px; height: 45px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 12px;
}
.blue-box { background: #e0f2fe; color: #0284c7; }
.emerald-box { background: #d1fae5; color: #059669; }

/* Form Elemanları */
.form-group { margin-bottom: 20px; }
label { display: block; margin-bottom: 8px; color: #475569; font-size: 0.95rem; font-weight: 600; }
.required { color: #e11d48; }

input, textarea { 
  width: 100%; 
  padding: 12px 15px; 
  background: #ffffff; 
  border: 1px solid #cbd5e1; 
  border-radius: 10px; 
  color: #1e293b; 
  font-size: 1rem; 
  transition: all 0.3s ease; 
  box-sizing: border-box; 
}
input:focus, textarea:focus { 
  outline: none; 
  border-color: #38bdf8; 
  box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2); 
}
input::placeholder, textarea::placeholder { color: #94a3b8; }

/* Butonlar */
.btn-primary { 
  width: 100%; 
  padding: 14px; 
  background: #0ea5e9; 
  border: none; 
  border-radius: 10px; 
  color: white; 
  font-size: 1rem;
  font-weight: 600; 
  cursor: pointer; 
  transition: all 0.2s ease; 
  box-shadow: 0 4px 6px -1px rgba(14, 165, 233, 0.2);
}
.btn-primary:hover { background: #0284c7; transform: translateY(-2px); box-shadow: 0 6px 8px -1px rgba(14, 165, 233, 0.3); }

/* Otel Kartları */
.hotel-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }

.hotel-card { 
  background: #ffffff; 
  border: 1px solid #e2e8f0; 
  border-radius: 16px; 
  padding: 20px; 
  display: flex; 
  flex-direction: column; 
  justify-content: space-between; 
  transition: all 0.3s ease; 
}
.hotel-card:hover { 
  transform: translateY(-5px); 
  border-color: #bae6fd; 
  box-shadow: 0 12px 20px -5px rgba(0, 0, 0, 0.05); 
}

.card-content h3 { font-size: 1.25rem; font-weight: 700; color: #0f172a; margin-bottom: 12px; }

.info-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.info-icon { color: #94a3b8; }
.location-tag, .contact-tag { color: #64748b; font-size: 0.95rem; margin: 0; }

.card-actions { 
  display: flex; 
  gap: 10px; 
  margin-top: 20px; 
  padding-top: 15px; 
  border-top: 1px solid #f1f5f9; 
}

.btn-manage { 
  flex: 1;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  padding: 10px; 
  background: #f0f9ff; 
  border: 1px solid #bae6fd; 
  border-radius: 8px; 
  color: #0284c7; 
  font-weight: 600; 
  cursor: pointer; 
  transition: all 0.2s; 
}
.btn-manage:hover { background: #0ea5e9; color: #ffffff; }

.btn-danger { 
  display: flex; align-items: center; justify-content: center;
  padding: 10px 15px;
  background: #fff1f2; 
  border: 1px solid #fecdd3; 
  color: #e11d48; 
  border-radius: 8px; 
  cursor: pointer; 
  transition: all 0.2s; 
}
.btn-danger:hover { background: #e11d48; color: white; }

/* Durum Göstergeleri (Yükleniyor, Boş, Hata) */
.state-container { text-align: center; padding: 60px 20px; color: #64748b; }
.spinner-icon { animation: spin 1s linear infinite; margin: 0 auto 15px auto; color: #0ea5e9; }
.empty-icon { color: #cbd5e1; margin-bottom: 15px; }

@keyframes spin { to { transform: rotate(360deg); } }

/* Mobil Uyumluluk */
@media (max-width: 992px) { 
  .grid-layout { grid-template-columns: 1fr; } 
  .form-section { order: 2; }
  .list-section { order: 1; }
}
</style>