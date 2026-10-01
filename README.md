# AYS – Apartman Yönetim Sistemi

> **Apartman Yönetim & Banka Bakiye Takip Sistemi**  
> Modern, rol tabanlı bir apartman yönetim portalı. Spring Boot REST API, dosya tabanlı kalıcı H2 veritabanı ve sıfır bağımlılıklı tek sayfalık web arayüzü (Vanilla JS) ile çalışır.

---

## İçindekiler

- [Özellikler](#özellikler)
- [Ekran Görüntüleri](#ekran-görüntüleri)
- [Teknoloji Yığını](#teknoloji-yığını)
- [Proje Yapısı](#proje-yapısı)
- [Kurulum ve Çalıştırma](#kurulum-ve-çalıştırma)
- [Rol Tabanlı Erişim](#rol-tabanlı-erişim)
- [API Dokümantasyonu](#api-dokümantasyonu)
- [Geliştirme Notları](#geliştirme-notları)

---

## Özellikler

### Genel
- 🔐 **Rol Tabanlı Kimlik Doğrulama** — Token tabanlı oturum yönetimi; ilk kayıt olan kullanıcı otomatik olarak **Yönetici (Admin)** olur, sonraki kayıtlar **Site Sakini (User)** rolünü alır.
- 💾 **Kalıcı Dosya Veritabanı** — H2 dosya modu ile sunucu yeniden başlatıldığında veriler silinmez; `data/aysdb.mv.db` dosyasında saklanır.
- 📱 **Tek Sayfalık Uygulama** — Harici framework olmadan Vanilla JS ile yazılmış, localStorage & cookie tabanlı oturum yönetimi.

### Yönetici (Admin) Paneli
- 🏦 **Banka Hesabı & Kasa Yönetimi** — Apartman adına hesap ve kasa tanımlama, IBAN ve bakiye takibi.
- 📋 **Aidat Yönetimi** — Daire bazında aidat tahakkuku ve ödeme durumu takibi.
- 💸 **Gider Yönetimi** — Kategori bazlı gider girişi, seçili hesaptan otomatik bakiye düşümü.
- ✅ **Ödeme Bildirimi Onay Sistemi** — Sakinlerin yaptığı ödeme bildirimlerini **Onayla / Reddet** butonlarıyla tek tıkta işleme alma.
- 🔒 **Swagger UI Koruması** — API dokümantasyonuna sadece oturum açmış admin kullanıcıları erişebilir (çerez tabanlı token doğrulama).

### Kullanıcı (Site Sakini) Paneli
- 📢 **Ödeme Bildirimi Gönderme** — Tutar ve açıklama girerek yönetime ödeme bildirimi iletme.
- 📊 **Aidat Çizelgesini Görüntüleme** — Yönetim tarafından girilen tüm aidat kayıtlarını ve durumlarını izleme.
- 📉 **Giderleri Görüntüleme** — Apartman ortak giderlerini salt okunur olarak izleme.
- 📬 **Bildirim Geçmişi** — Gönderilen bildirimlerin onay durumunu (Bekliyor / Onaylandı / Reddedildi) takip etme.

---

## Teknoloji Yığını

| Katman | Teknoloji |
|---|---|
| **Backend** | Java 17, Spring Boot 3.3.5 |
| **Persistence** | Spring Data JPA, Hibernate, H2 (Dosya Modu) |
| **Güvenlik** | Spring Security Crypto (BCrypt), Token Tabanlı Auth |
| **API Docs** | SpringDoc OpenAPI 3 (Swagger UI) |
| **Doğrulama** | Jakarta Bean Validation |
| **Build** | Apache Maven 3.9 |
| **Frontend** | HTML5, Vanilla JavaScript (ES2020+), CSS3 |

---

## Proje Yapısı

```
AYS/
├── src/
│   └── main/
│       ├── java/com/example/ays/
│       │   ├── config/
│       │   │   └── SwaggerAuthFilter.java      # Swagger'ı admin'e kısıtlayan filtre
│       │   ├── controller/
│       │   │   ├── AuthController.java          # Kayıt & Giriş
│       │   │   ├── BankAccountController.java   # Banka hesabı işlemleri (Admin)
│       │   │   ├── BankIntegrationController.java # Bakiye yükleme (Admin)
│       │   │   ├── DuesController.java          # Aidat yönetimi
│       │   │   ├── ExpenseController.java       # Gider yönetimi
│       │   │   └── PaymentNotificationController.java # Ödeme bildirimleri
│       │   ├── dto/                             # İstek/Yanıt DTO'ları
│       │   ├── entity/                          # JPA Entity sınıfları
│       │   ├── enums/                           # Rol ve durum enum'ları
│       │   ├── exception/                       # Global hata yönetimi
│       │   ├── repository/                      # Spring Data JPA arayüzleri
│       │   └── service/                         # İş mantığı servisleri
│       └── resources/
│           ├── static/
│           │   ├── index.html                   # Tek sayfalık uygulama
│           │   ├── styles.css                   # Kurumsal arayüz stilleri
│           │   └── app.js                       # SPA mantığı
│           └── application.properties           # Uygulama yapılandırması
├── data/                                        # H2 kalıcı veritabanı dosyaları (Git'e dahil değil)
├── pom.xml
└── README.md
```

---

## Kurulum ve Çalıştırma

### Gereksinimler

Proje kendi araçlarını içermektedir, sistemde Java veya Maven kurulu olması **zorunlu değildir**.

### 1. Depoyu Klonlayın

```bash
git clone https://github.com/KULLANICI_ADI/ays.git
cd ays
```

### 2. Uygulamayı Başlatın

**Windows (Dahili JDK ve Maven ile):**

```powershell
$env:JAVA_HOME = "$(Get-Location)\.tools\jdk-17.0.20.1+1"
$env:PATH = "$env:JAVA_HOME\bin;$(Get-Location)\.tools\apache-maven-3.9.16\bin;$env:PATH"
mvn spring-boot:run
```

**Sistem JDK/Maven ile:**

```bash
mvn spring-boot:run
```

### 3. Uygulamaya Erişin

| Adres | Açıklama |
|---|---|
| `http://localhost:8081` | Ana uygulama arayüzü |
| `http://localhost:8081/swagger-ui.html` | API Dokümantasyonu (yalnızca Admin) |
| `http://localhost:8081/h2-console` | Veritabanı yönetim konsolu |

> **Not:** H2 Console bağlantı bilgileri: JDBC URL: `jdbc:h2:file:./data/aysdb`, Kullanıcı: `sa`, Şifre: *(boş)*

---

## Rol Tabanlı Erişim

| İşlem | Yönetici (Admin) | Site Sakini (User) |
|---|:---:|:---:|
| Banka hesabı ekleme | ✅ | ❌ |
| Aidat tahakkuku | ✅ | ❌ |
| Gider girişi | ✅ | ❌ |
| Ödeme bildirimlerini onaylama/reddetme | ✅ | ❌ |
| Swagger UI'a erişim | ✅ | ❌ |
| Aidat çizelgesini görüntüleme | ✅ | ✅ |
| Giderleri görüntüleme | ✅ | ✅ |
| Ödeme bildirimi gönderme | ❌ | ✅ |
| Kendi bildirim geçmişini görüntüleme | — | ✅ |

> 🔑 Sisteme ilk kaydolan kullanıcı otomatik olarak **Yönetici** rolünü alır. Sonraki tüm kayıtlar **Site Sakini** rolüyle sisteme katılır.

---

## API Dokümantasyonu

Swagger UI, oturum açmış yöneticiler için `http://localhost:8081/swagger-ui.html` adresinde mevcuttur.

### Temel Uç Noktalar

```
POST   /api/auth/register            Yeni kullanıcı kaydı
POST   /api/auth/login               Giriş (token döner)

GET    /api/bank-accounts            Banka hesaplarını listele (Admin)
POST   /api/bank-accounts            Yeni hesap ekle (Admin)
POST   /api/bank-integrations/top-up Hesaba para yükle (Admin)

GET    /api/dues                     Aidatları listele (Herkese açık)
POST   /api/dues                     Aidat oluştur (Admin)

GET    /api/expenses                 Giderleri listele (Herkese açık)
POST   /api/expenses                 Gider ekle (Admin)

GET    /api/notifications            Bildirimleri listele
POST   /api/notifications            Ödeme bildirimi gönder (User)
POST   /api/notifications/{id}/status Bildirim durumu güncelle (Admin)
```

---

## Geliştirme Notları

### Varsayılan Yapılandırma (`application.properties`)

```properties
spring.datasource.url=jdbc:h2:file:./data/aysdb;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
server.port=8081
```

### MySQL'e Geçiş (İsteğe Bağlı)

`application.properties` dosyasındaki aşağıdaki satırları değiştirin:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/aysdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=SIFRENIZ
```

`pom.xml`'e MySQL connector bağımlılığını ekleyin:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## Lisans

MIT License — Dilediğiniz gibi kullanabilir ve değiştirebilirsiniz.
