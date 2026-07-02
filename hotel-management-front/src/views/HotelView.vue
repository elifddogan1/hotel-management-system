<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { hotelService, type Hotel } from '../services/hotelService';

// --- REAKTİF STATE TANIMLARI (Angular'daki Signal gibi düşünebilirsin) ---
const hotels = ref<Hotel[]>([]);
const isLoading = ref(true);
const errorMessage = ref('');

// Yeni otel formu için reaktif nesne
const newHotel = ref<Hotel>({
  name: '',
  location: '',
  contactInfo: ''
});

// --- API FONKSİYONLARI ---
// 1. Otelleri Listeleme
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

// 2. Yeni Otel Ekleme
const handleCreateHotel = async () => {
  if (!newHotel.value.name || !newHotel.value.location) {
    alert('Lütfen gerekli alanları doldurun!');
    return;
  }

  try {
    await hotelService.createHotel(newHotel.value);
    // Formu temizle
    newHotel.value = { name: '', location: '', contactInfo: '' };
    // Listeyi güncelle
    await loadHotels();
  } catch (error) {
    alert('Otel eklenirken bir hata oluştu.');
    console.error(error);
  }
};

// 3. Otel Silme
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

// --- YAŞAM DÖNGÜSÜ (ngOnInit Karşılığı) ---
onMounted(() => {
  loadHotels();
});
</script>

<template>
  <div class="hotel-dashboard">
    <header class="header">
      <h1>Otel Yönetim Paneli</h1>
      <p class="subtitle">Acenta otel entegrasyonu ve yönetim sistemi</p>
    </header>

    <div class="grid-layout">
      <!-- SOL TARAF: OTEL EKLEME FORMU -->
      <section class="glass-card form-section">
        <h2>Yeni Otel Kaydı</h2>
        <!-- Angular'daki (ngSubmit) yerine Vue'da @submit.prevent kullanılır -->
        <form @submit.prevent="handleCreateHotel">
          <div class="form-group">
            <label for="name">Otel Adı *</label>
            <!-- Angular'daki [(ngModel)] karşılığı v-model -->
            <input 
              v-model="newHotel.name" 
              type="text" 
              id="name" 
              placeholder="Örn: Akdeniz Luxury Resort" 
              required
            />
          </div>

          <div class="form-group">
            <label for="location">Konum / Şehir *</label>
            <input 
              v-model="newHotel.location" 
              type="text" 
              id="location" 
              placeholder="Örn: Antalya / Konyaaltı" 
              required
            />
          </div>

          <div class="form-group">
            <label for="contact">İletişim Bilgisi (Backend DTO Güncel Alanı)</label>
            <textarea 
              v-model="newHotel.contactInfo" 
              id="contact" 
              placeholder="Telefon, E-posta veya Adres bilgisi..."
              rows="3"
            ></textarea>
          </div>

          <button type="submit" class="btn-primary">Oteli Sisteme Kaydet</button>
        </form>
      </section>

      <!-- SAĞ TARAF: OTEL LİSTESİ -->
      <section class="glass-card list-section">
        <h2>Kayıtlı Oteller</h2>

        <!-- Yükleniyor Durumu (*ngIf karşılığı v-if) -->
        <div v-if="isLoading" class="loading-state">
          <div class="spinner"></div>
          <p>Veritabanından oteller çekiliyor...</p>
        </div>

        <!-- Hata Durumu -->
        <div v-else-if="errorMessage" class="error-state">
          {{ errorMessage }}
        </div>

        <!-- Boş Liste Durumu -->
        <div v-else-if="hotels.length === 0" class="empty-state">
          Sisteme henüz hiçbir otel tanımlanmamış. Soldaki formdan ilk otelinizi ekleyin!
        </div>

        <!-- Otel Kartları (*ngFor karşılığı v-for) -->
        <div v-else class="hotel-grid">
          <div v-for="hotel in hotels" :key="hotel.id" class="hotel-card">
            <div class="card-body">
              <h3>{{ hotel.name }}</h3>
              <p class="location-tag">📍 {{ hotel.location }}</p>
              <p v-if="hotel.contactInfo" class="contact-tag">📞 {{ hotel.contactInfo }}</p>
            </div>
            <div class="card-actions">
              <!-- Angular'daki (click) karşılığı @click -->
              <button @click="handleDeleteHotel(hotel.id)" class="btn-danger" title="Oteli Sil">
                🗑️
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* --- DARK MODE & SLATE/SKY-BLUE GLASSMORPHISM TEMASI --- */
.hotel-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%); /* Deep Slate */
  color: #f8fafc;
  padding: 40px 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(to right, #38bdf8, #0ea5e9); /* Sky Blue Gradient */
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  color: #94a3b8;
  margin-top: 8px;
}

.grid-layout {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Glassmorphism Efekti */
.glass-card {
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
}

h2 {
  font-size: 1.5rem;
  color: #38bdf8;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(56, 189, 248, 0.2);
  padding-bottom: 10px;
}

/* Form Tasarımları */
.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #cbd5e1;
  font-size: 0.9rem;
}

input, textarea {
  width: 100%;
  padding: 12px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: #fff;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

input:focus, textarea:focus {
  outline: none;
  border-color: #38bdf8;
  box-shadow: 0 0 10px rgba(56, 189, 248, 0.3);
}

.btn-primary {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s, opacity 0.2s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  opacity: 0.9;
}

/* Otel Kartları Tasarımı */
.hotel-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.hotel-card {
  background: rgba(15, 23, 42, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: transform 0.2s;
}

.hotel-card:hover {
  transform: scale(1.02);
  border-color: rgba(56, 189, 248, 0.3);
}

.card-body h3 {
  margin: 0 0 8px 0;
  color: #fff;
}

.location-tag {
  color: #94a3b8;
  font-size: 0.9rem;
  margin: 4px 0;
}

.contact-tag {
  color: #38bdf8;
  font-size: 0.85rem;
  margin: 4px 0;
}

.btn-danger {
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid #ef4444;
  color: #ef4444;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-danger:hover {
  background: #ef4444;
  color: white;
}

/* Durum Mesajları ve Spinner */
.loading-state, .empty-state, .error-state {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-top-color: #38bdf8;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .grid-layout {
    grid-template-columns: 1fr;
  }
  .hotel-grid {
    grid-template-columns: 1fr;
  }
}
</style>