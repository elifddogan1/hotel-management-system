
# Modern Hotel Management System

Bu proje, acentalar ve otel yöneticileri için tasarlanmış, modern ve dinamik bir Otel ve Rezervasyon Yönetim Panelidir. Arayüzü ve glassmorphism tasarım detayları ile kullanıcı dostu bir deneyim sunarken, güçlü arka plan mimarisi ile operasyonel veri bütünlüğünü sağlar.

## Öne Çıkan Özellikler

Otel ve Oda Yönetimi: Sisteme yeni oteller ekleme, otellere özel odalar tanımlama ve detaylı kapasite/tip (Deluxe, Suite vb.) yapılandırması.

Gelişmiş Rezervasyon Sistemi: Otomatik Voucher (Örn: VCH-XXXXX) üretimi, odaya özel misafir atama ve hızlı iptal yönetimi.

Akıllı Doluluk Takvimi: Seçili odanın rezervasyon geçmişini ve gelecek doluluk durumunu görsel takvim üzerinde izleme.

Müsaitlik Arama Paneli: Giriş-çıkış tarihleri ve kişi sayısına göre tüm sistemde veya filtrelenmiş belirli bir otelde müsait oda sorgulama.

Tam Docker Entegrasyonu: Tek komutla tüm mimariyi (Veritabanı, Backend, Frontend) bağımlılık sorunu olmadan ayağa kaldırma.

## Kullanılan Teknolojiler

Frontend (Kullanıcı Arayüzü):

* Vue 3 (Composition API) ve TypeScript
* Vite
* V-Calendar
* Lucide Icons
* Nginx (Docker üzerinde statik sunum)

Backend (Sunucu ve API):

* Java 21 ve Spring Boot
* Spring Data JPA ve Hibernate
* PostgreSQL

DevOps ve Altyapı:

* Docker ve Docker Compose

## Kurulum ve Çalıştırma (Docker)

Sistemi yerel ortamınızda ayağa kaldırmak için bilgisayarınızda yalnızca Docker ve Docker Compose kurulu olması yeterlidir. İlave bir Node.js veya Java SDK kurulumuna gerek yoktur.

### 1. Projeyi Klonlama

Terminalinizi açın ve depoyu yerel bilgisayarınıza indirin:

git clone <repo linki>
cd hotel

### 2. Konteynerleri İnşa Etme ve Başlatma

Projenin ana dizininde (docker-compose.yml dosyasının bulunduğu konumda) aşağıdaki komutu çalıştırın. Bu komut; veritabanı, arka uç ve ön yüzü sıfırdan derleyerek başlatacaktır:

docker-compose up --build

Not: Konteynerleri terminali meşgul etmeden arka planda çalıştırmak isterseniz komutun sonuna -d bayrağını ekleyebilirsiniz: docker-compose up --build -d

### 3. Sisteme Erişim

Veritabanı bağlantıları sağlanıp Nginx ayağa kalktığında (log akışı tamamlandığında), tarayıcınız üzerinden aşağıdaki adreslerle sisteme erişebilirsiniz:

Kullanıcı Paneli (Frontend): http://localhost:5173
Backend API: http://localhost:8080

### Sistemi Durdurma

Çalışan konteynerleri durdurmak ve oluşturulan ağı temizlemek için terminalinizde şu komutu çalıştırabilirsiniz:

docker-compose down

Geliştirici: elifddogan1
