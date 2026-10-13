# Dingin Sudoku 0.1.1 — görsel iyileştirme

Tarih: 13 Ekim 2026. İşlevsel referans: `reports/07-VISUAL-COMPARISON.md`. Dingin’in yeşil/krem renkleri, serif başlıkları, sade premium görünümü ve Türkçe metinleri korunmuştur. LibreSudoku’dan kaynak kodu, marka, ikon veya görsel varlık aktarılmamıştır. Bu sürüm üç görsel öncelikle sınırlıdır.

## Git ile güvence

Değişikliklerden önce yerel Git deposu oluşturuldu: `75c50835c904c015c37b360f4ae35bf9b323c617`, `baseline-v0.1.0` etiketi. Geliştirme `design-refinement` dalında yapıldı. Eski APK ayrıca `C:/APK-Lab/work/design-refinement/baseline-v0.1.0.apk` konumunda korundu. Bu güvence yereldir; uzak depoya gönderilmedi.

## 1. Özgün adaptif ikon

Yeşil zemin üzerinde krem, yuvarlatılmış D monogramı ve iç boşlukta üç küçük sıcak altın işaret. Harf uygulamanın adını, küçük işaretler sakin bir ritmi temsil eder; bu açıklama tasarım niyetidir. Geometri projede bağımsız vektör yolları olarak çizildi. Sistem grid ikonu kaldırıldı. Aynı işaret ana menüde kullanılarak marka bütünlüğü sağlandı.

108×108 vektörün ön planı merkezdeki 66×66 güvenli alan içindedir. Adaptif arka/ön plan ve Android 13+ temalı ikonlar için monochrome katmanı vardır. Sistem launcher maskesini ve monochrome renklerini belirler; teslim edilen daire, yuvarlatılmış kare ve superellipse önizlemeleri test çizimleridir, her üretici launcher’ının görünümü olduğu iddia edilmez. Android 26’nın bilinmeyen katmanı atlaması kaynak üzerinden kontrol edildi; API26 cihaz testi yapılmadı.

[Android adaptif ikon kılavuzu](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive) ve [Android 8 AdaptiveIconDrawable kaynak kodu](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-8.0.0_r1/graphics/java/android/graphics/drawable/AdaptiveIconDrawable.java).

## 2. Tutarlı tahta vurguları

Renk rolleri `BoardStyle.kt` içinde tek yerde tanımlandı. Öncelik: **hata → seçili hücre → aynı rakam → ilgili satır/sütun/3×3 blok → taban**. Boş hücreler aynı rakam olarak işaretlenmez.

| Durum | Açık tema | Koyu tema | Renk dışı işaret |
|---|---|---|---|
| Taban | `#FEFDF9` | `#182823` | Normal hücre |
| Satır / sütun / blok | `#EEF3EE` | `#20362E` | Türkçe erişilebilirlik açıklaması |
| Aynı rakam | `#F4E5CA` | `#4E4030` | Küçük sıcak alt çizgi |
| Seçim | `#D7EAE3` | `#234D42` | İki yeşil köşe işareti |
| Hata | `#FBE2DC` | `#4D2829` | Ünlem ve “yanlış” açıklaması |

Hatalı hücre seçildiğinde kırmızı zemin korunur; köşe işaretleri seçimi gösterir. Sabit rakamlar daha kalın, kullanıcı rakamları yeşil; notlar 9,5sp orta ağırlıktadır. Hücre ve blok çizgileri opak renklerle ayrılır. Görsel vurgu oyun kurallarını veya kayıt verisini değiştirmez; hata kontrolü kapalıysa hataların görsel rolü de kapanır.

## 3. Minimal araç çubuğu

Dört dolu kart yerine tek, ince çerçeveli yüzey; saydam dört alan, küçük 20dp çizgisel Material ikonları ve 11sp Türkçe etiketler. Alanların minimum yüksekliği 64dp. Geri al / Sil / Notlar / İpucu sırası ve işlevleri korundu. Dikey ince ayırıcılar kullanıldı. Not modu “Not açık” etiketi, yeşil alt çizgi ve erişilebilir “Açık/Kapalı” durumuyla gösterilir; dolu kapsül kullanılmaz. Standart Material ikonları Compose bağımlılığından gelir; LibreSudoku varlığı değildir.

## Kimlik, motor ve kayıt

Paket `com.dingin.sudoku`; versionCode 2, versionName 0.1.1. Debug sertifikası SHA-256: `b8f2ce01db4f5d421b834ecdbff861dc445a4eb3b7711194bef8d3eb621469d0` — önceki APK ile aynı.

Git karşılaştırmasında `core/`, `GameViewModel.kt` ve `MainActivity.kt` değişmedi. Bulmaca üretimi, tek çözüm doğrulaması, hamleler, ipucu, süre, istatistikler ve ProfileCodec/ProfileStore biçimi korunmuştur. Kaynak arşivi özel debug anahtarını içermez; başka makinede aynı imzayla üretim ayrıca mevcut anahtarın güvenli aktarımını gerektirir. Teslim edilen APK mevcut anahtarla imzalıdır.

## Doğrulama

- Debug APK derlemesi başarılı. Son Gradle çalıştırması: `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:connectedDebugAndroidTest`. Motor testleri ayrıca `:core:test --rerun-tasks` ile yeniden çalıştırıldı.
- **17/17 motor ve kayıt JVM testi**, **8/8 görsel durum/kontrast JVM testi**, **8/8 API35 Android emülatör testi** geçti; toplam **33 test, 0 hata**. Android suite: 5 oyun/arayüz testi ve 3 adaptif ikon testi.
- Üretim/tek çözüm/hamle/kayıt testleri mevcut bağımsız motoru kapsar. Yeni testler seçili hata önceliği, satır/sütun/blok rolü, boş seçim, aynı sayı rolü, açık/koyu kontrast ve not anahtarının durumunu denetler. Eski UI testlerinin açıklama beklentileri, yeni erişilebilirlik eklerini kabul edecek şekilde güncellendi.
- Sabit/kullanıcı rakamı ve not mürekkepleri tüm normal vurgu zeminlerinde ≥4,5:1; hata metni hata zemininde ≥4,5:1; seçimin hata üzerindeki köşe işaretleri ve çizgiler için ilgili testlerde ≥3:1. Bu, test edilen renk çiftlerine aittir; tüm ekranın veya tüm cihazların genel uygunluk iddiası değildir.
- İkonun adaptif/monochrome katmanları ve ön planın güvenli alanda kalması geçti. Önizleme üretimi için aynı 3 ikon testi ayrıca çalıştırıldı; toplam 33 farklı teste ikinci kez eklenmedi.
- **Lint: 0 hata, 7 uyarı**. Altı sabit Gradle/AndroidX bağımlılığı sürüm uyarısı ve bir coroutines yeni sürüm uyarısı; ikon/erişilebilirlik uyarısı yok. Bu turda bağımlılıklar yükseltilmedi.
- API35 emülatöründe 0.1.0 APK kuruldu, yeni oyun ve not kaydı oluşturuldu, 0.1.1 `install -r` ile üzerine kuruldu. `profile.bin` güncelleme ve ilk soğuk açılış sonrasında bayt bazında aynı kaldı; devam ekranında notlar korundu. Yeni sürümde zorla durdurma ve yeniden açma kontrolü de geçti.
- Açık/koyu oyun ve ana menü görüntüleri incelendi; seçili hata, aynı rakam, satır/sütun/blok tonları, not ve aktif araç çubuğu görünümü kontrol edildi. Paket/version ve aynı debug sertifikası `aapt2`/`apksigner` ile doğrulandı.

## Sınırlar

Yeni sürüm fiziksel telefonda kurulmadı veya test edilmedi. Bağlı Redmi üzerindeki uygulamaya ve kayda dokunulmadı. Ekran görüntüleri API35 emülatöründeki gerçek Compose ekranlarıdır; hata vurgusunu göstermek için bilerek yanlış rakam girildi. Renk kontrastı testleri erişilebilirlik sertifikası değildir; TalkBack, büyük yazı, küçük ekran, renk görme farklılıkları ve farklı launcher/Android sürümleri bu turda cihaz üzerinde doğrulanmadı.

## Teslimatlar

Kaynak proje: `C:/APK-Lab/my-android-app`. Derleme çıktısı: `C:/APK-Lab/my-android-app/app/build/outputs/apk/debug/app-debug.apk`. Teslim klasöründe 0.1.1 debug APK, kaynak ZIP, açık/koyu oyun ve ana menü PNG’leri, ikon önizlemeleri, test/derleme kanıtları ZIP ve bu rapor bulunur. Önceki 0.1.0 teslim dosyaları korunmuştur.
