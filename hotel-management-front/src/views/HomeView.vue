<script setup lang="ts">
import { useRouter } from 'vue-router';
// Emojiler yerine profesyonel ikonlarımızı içe aktarıyoruz
import { Building2, BedDouble, CalendarPlus, Users } from 'lucide-vue-next';

const router = useRouter();

const navigateTo = (routeName: string) => {
  router.push({ name: routeName });
};
</script>

<template>
  <div class="dashboard-layout">
    <!-- Arka plandaki renkli parlamalar için dekoratif elementler -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>
    <div class="bg-shape shape-3"></div>

    <div class="content-wrapper">
      <header class="hero-section">
        <h1>Otel Yönetim Paneli</h1>
        <p class="subtitle">Sistemin tüm modüllerine buradan hızlıca erişebilirsiniz.</p>
      </header>

      <div class="nav-grid">
        <!-- Oteller Kartı -->
        <div class="glass-card nav-card" @click="navigateTo('hotels')">
          <div class="card-icon blue-icon">
            <Building2 :size="36" stroke-width="1.5" />
          </div>
          <h2>Oteller</h2>
          <p>Sisteme kayıtlı otelleri görüntüleyin, yeni otel ekleyin veya mevcut otelleri düzenleyin.</p>
          <span class="action-text">Yönetime Git <span class="arrow">➔</span></span>
        </div>

        <!-- Odalar Kartı -->
        <div class="glass-card nav-card" @click="navigateTo('rooms')">
          <div class="card-icon emerald-icon">
            <BedDouble :size="36" stroke-width="1.5" />
          </div>
          <h2>Oda Rehberi</h2>
          <p>Tüm otellerdeki odaları listeleyin, kapasite ve tiplerine göre filtreleyip detaylarına ulaşın.</p>
          <span class="action-text">Odalara Göz At <span class="arrow">➔</span></span>
        </div>

        <!-- Yeni Rezervasyon Kartı (Vurgulu) -->
        <div class="glass-card nav-card highlight-card" @click="navigateTo('reservation')">
          <div class="badge">Hızlı İşlem</div>
          <div class="card-icon rose-icon">
            <CalendarPlus :size="36" stroke-width="1.5" />
          </div>
          <h2>Yeni Rezervasyon</h2>
          <p>Tarih ve kişi sayısına göre müsait odaları arayın ve hızlıca yeni bir rezervasyon oluşturun.</p>
          <span class="action-text">Rezervasyon Yap <span class="arrow">➔</span></span>
        </div>

        <!-- Misafirler Kartı -->
        <div class="glass-card nav-card" @click="navigateTo('guests')">
          <div class="card-icon amber-icon">
            <Users :size="36" stroke-width="1.5" />
          </div>
          <h2>Misafirler & İptal</h2>
          <p>Mevcut rezervasyonları takip edin, misafir listesinde arama yapın veya iptal işlemlerini gerçekleştirin.</p>
          <span class="action-text">Kayıtları İncele <span class="arrow">➔</span></span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Genel Yerleşim ve Kırık Beyaz Arka Plan */
.dashboard-layout { 
  min-height: 100vh; 
  background-color: #f8fafc; /* Kırık beyaz / Off-white */
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', system-ui, sans-serif; 
  color: #1e293b; /* Koyu antrasit metin */
}

/* Arkadaki Canlı Renk Parlamaları (Glassmorphism hissiyatı için) */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.4;
  animation: float 10s infinite ease-in-out alternate;
}
.shape-1 { top: -10%; left: -5%; width: 400px; height: 400px; background: #38bdf8; }
.shape-2 { bottom: -10%; right: -5%; width: 500px; height: 500px; background: #10b981; animation-delay: -2s; }
.shape-3 { top: 40%; left: 40%; width: 300px; height: 300px; background: #fb7185; animation-delay: -5s; }

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(30px, 50px) scale(1.1); }
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 60px 20px;
}

/* Başlık Alanı */
.hero-section { 
  text-align: center; 
  margin-bottom: 60px; 
  animation: fadeInDown 0.8s ease-out;
}
@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-section h1 { 
  font-size: 2.8rem; 
  font-weight: 800;
  color: #0f172a; 
  margin-bottom: 15px; 
  letter-spacing: -0.5px;
}

.subtitle { 
  color: #64748b; 
  font-size: 1.15rem; 
  max-width: 600px;
  margin: 0 auto;
}

/* Izgara Sistemi */
.nav-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); 
  gap: 30px; 
  max-width: 1200px; 
  margin: 0 auto; 
}

/* Açık Tema Glassmorphism Kartları */
.glass-card { 
  background: rgba(255, 255, 255, 0.7); 
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 1); 
  border-radius: 20px; 
  padding: 40px 30px; 
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05);
}

.nav-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
}

/* Kart Hover Animasyonu */
.nav-card:hover {
  transform: translateY(-10px);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.1);
}

/* İkon Stilleri ve Canlı Renkler */
.card-icon {
  margin-bottom: 20px;
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  transition: transform 0.3s ease;
}
.nav-card:hover .card-icon { transform: scale(1.1) rotate(-5deg); }

.blue-icon { background: #e0f2fe; color: #0284c7; }
.emerald-icon { background: #d1fae5; color: #059669; }
.rose-icon { background: #ffe4e6; color: #e11d48; }
.amber-icon { background: #fef3c7; color: #d97706; }

.nav-card h2 {
  color: #1e293b;
  margin-bottom: 12px;
  font-size: 1.4rem;
  font-weight: 700;
}

.nav-card p {
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 25px;
  font-size: 0.95rem;
  flex-grow: 1;
}

/* Aksiyon Metni ve Ok Animasyonu */
.action-text {
  font-weight: 600;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: color 0.3s ease;
}
.arrow { transition: transform 0.3s ease; }
.nav-card:hover .arrow { transform: translateX(6px); }

.nav-card:nth-child(1) .action-text { color: #0284c7; }
.nav-card:nth-child(2) .action-text { color: #059669; }
.nav-card:nth-child(3) .action-text { color: #e11d48; }
.nav-card:nth-child(4) .action-text { color: #d97706; }

/* Vurgulu Rezervasyon Kartı Özel Stili */
.highlight-card {
  border: 2px solid #fecdd3;
  box-shadow: 0 15px 35px -10px rgba(225, 29, 72, 0.15);
}
.highlight-card:hover { border-color: #fda4af; }

.badge {
  position: absolute;
  top: 15px;
  right: 15px;
  background: #e11d48;
  color: white;
  font-size: 0.75rem;
  font-weight: bold;
  padding: 4px 10px;
  border-radius: 20px;
  letter-spacing: 0.5px;
}

@media (max-width: 768px) {
  .hero-section h1 { font-size: 2.2rem; }
  .nav-grid { grid-template-columns: 1fr; max-width: 450px; }
}
</style>