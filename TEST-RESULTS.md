# Dingin Sudoku — ilk sürüm ve test raporu

Tarih: 13 Ekim 2026 (Türkiye). Sürüm: 0.1.0 / versionCode 1. Proje: `C:\APK-Lab\my-android-app`.

## Sonuç

Debug APK başarıyla derlendi, Android 15 / API 35 emülatörüne kuruldu ve gerçek arayüz üzerinden çalıştırıldı. **17 JVM testi ve 4 Android arayüz testi geçti; başarısız veya atlanan son test yok.** Lint: **0 hata, 7 sürüm güncelleme uyarısı**. Uyarılar yeni Gradle/Compose/Activity/Lifecycle/Coroutines sürümleriyle ilgilidir; sabitlenen uyumlu sürümlerle derleme başarılıdır. Kotlin ayrıca bazı Material ikonları için AutoMirrored API'ye geçiş uyarısı verir. SDK XML sürüm farkı uyarısı derlemeyi engellemedi.

Uçak modu açıkken yeni bulmaca üretme, not alma, ipucu kullanma, zorla kapatmadan sonra devam etme ve tam bir oyun bitirme ek olarak doğrulandı. Kaynak kodu, marka, launcher çizimi ve ekran tasarımı bağımsızdır; LibreSudoku kaynakları veya varlıkları kopyalanmadı.

## APK kimliği

| Alan | Doğrulanan değer |
|---|---|
| Tam konum | `C:\APK-Lab\my-android-app\app\build\outputs\apk\debug\app-debug.apk` |
| Paket | `com.dingin.sudoku` |
| Uygulama adı | Dingin Sudoku |
| minSdk / targetSdk / compileSdk | 26 / 36 / 36 |
| Boyut | 18.685.758 bayt |
| SHA-256 | `37024f6c0577690f2f9c2ca5b7d693dd566f91261d9e21d70e0543cdcfefa048` |
| İmza | apksigner verify başarılı; APK Signature Scheme v2 |
| Kurulum | adb install Success; MainActivity Status: ok |
| Çalışan APK eşitliği | Emülatörden çekilen base.apk SHA-256 ile derlenen dosya birebir aynı |
| Ağ | Derlenmiş manifestte INTERNET izni yok; yalnız AndroidX'in kendi signature receiver izni mevcut |

Bu bir **debug imzalı test sürümüdür**. Release anahtarıyla imzalama veya mağaza yayını yapılmadı. Android 8.0 ve üzeri için derlendi; fiilen doğrulanan Android sürümü aşağıda belirtilir.

## Windows ortamı ve kurulum

Başlangıçta PATH'teki Java 1.8.0_381 idi; JAVA_HOME/ANDROID_HOME tanımlı değildi. Buna rağmen JDK 17, Android Studio ve SDK zaten kuruluydu. Derleme komutları sistem PATH'ini kalıcı değiştirmeden şu JDK'ya yönlendirildi:

`C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`

SDK: `C:\Users\Dia13\AppData\Local\Android\Sdk`. Platform 36 ve Build Tools 35.0.0 mevcut. Android Studio 2025.3.4 ailesi, bundled JBR 21.0.10. Gradle 8.14.3 ve AGP 8.13.0; Kotlin/Compose compiler 2.2.10; Compose BOM 2025.09.01. Gerekli Maven bağımlılıkları ve standart Gradle wrapper indirildi.

Cihaz/AVD başlangıçta yoktu. Resmi Google SDK kaynağından Command-line Tools ve Android 35 Google APIs x86_64 sistem imajı kuruldu. `Dingin_Test_API35` AVD'si `C:\APK-Lab\work\avds` altında, WHPX, 2 GB RAM ve 2 CPU ile headless çalıştırıldı. API 35 / Android 15 / Pixel 6 profili, 1080×2400, 420 dpi kullanıldı. İlk emülatör boot'u yaklaşık 167 saniye sürdü ve System UI bir ANR diyaloğu gösterdi; ortam hazırlandıktan sonraki son testlerin tümü geçti. Bu olay uygulama çökmesi olarak sınıflandırılmadı.

## Çalıştırılan komutlar

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
cd C:\APK-Lab\my-android-app
.\gradlew.bat :core:test :app:assembleDebug :app:lintDebug :app:connectedDebugAndroidTest --console=plain
```

Son birleşik komut `BUILD SUCCESSFUL` ile bitti. Son JVM XML raporu 17 test / 0 failure / 0 error; Android XML raporu 4 test / 0 failure / 0 error / 0 skipped. Android test gövdelerinin toplam süresi 31,442 saniye; birleşik build/test komutu yaklaşık 1 dakika 54 saniye. Cache nedeniyle son komutta core test görevi UP-TO-DATE; aynı core kaynaklarının önceki başarılı çalıştırma XML'i korunur.

İlk cihaz testindeki iki başarısızlık Activity recreation'ın ana menüye dönmesini bekleyen testlerden kaynaklandı. MVVM yapı mevcut oyun ekranını koruyordu. Testler doğru lifecycle beklentisine güncellendi; son çalıştırmada dördü de geçti. Bu sonuç, gerçek süreç ölümü testi yerine kullanılmadı; force-stop kontrolü ayrıca yapıldı.

## Motor ve kayıt testleri — 17/17

- Bilinen Sudoku'nun doğru çözümü ve başlangıç sayılarının korunması.
- Satır/sütun/blok çatışması, aralık ve tahta boyutu doğrulaması.
- Anında duplicate oluşturmayan çözümsüz bulmaca; boş tahtada birden fazla çözüm ve ikide durma.
- Dört seviyede dört seed: **16 bulmaca** için tek çözüm, çözüm eşitliği, givens korunması ve beklenen teknik sınıfı.
- Seed tekrar üretimi ve üretim deadline'ı.
- Kilitli hücre, not toggle, sayı/not ayrımı, sil ve tamamlanma.
- Geri alma ile board/not restore; toplam hata ve ipucu sayaçlarının geri alınmaması.
- Yanlış rakamın komşu notları silmemesi.
- Monotonic zaman farkı, pause/resume ve restore'da arka plan süresinin eklenmemesi.
- Kayıt codec roundtrip: ayarlar, geçmiş, notlar, süre ve istatistikler.
- Truncated/corrupt belge ve bilinmeyen şema reddi.
- Dosya restore, eski revision reddi ve bozuk ana kayıttan güvenli yedek recovery.
- 50 concurrent kayıtta en yeni revision'ın korunması.
- Gerçek filesystem IO hatasının raporlanması.

Bu örnek seti profesyonel zorluk kalibrasyonu veya bütün olası Sudoku'lar için kapsamlı ispat değildir. Zor sınıfı naked pairs / locked candidates gerektirir. Uzman sınıfı mevcut çıplak/gizli tekli, pair ve locked teknik setiyle çözülemeyen bulmacadır; insan seviyesine ilişkin bağımsız corpus validasyonu yapılmadı.

## Android arayüz testleri — 4/4

| Test | Doğrulanan akış |
|---|---|
| notesUndoEraseHintsAndSave | Hücre seçimi, not, sayı, geri al, sil, ipucu, diske kayıt, configuration recreation |
| errorsThemePauseAndBackground | Yanlış sayı vurgusu, hata sayacı, koyu tema, kontrol ayarı, pause ve arka planda süre durması |
| newPuzzleAndDifficultySelection | Uzman seçimi, yeni oyun onayı, gerçek üretim, tek çözüm, ana menü/devam |
| completionRecordsStatisticsOnce | Son hücre girişinden bitişe, istatistikte tek kayıt, recreation sonrası duplicate olmaması |

## Ek cihaz senaryoları

**Uçak modu / cold restart:** Uçak modu 1; Wi-Fi ve veri kapalı. Yeni oyun, not ve ipucu arayüz üzerinden uygulandı. `am force-stop` ile uygulama kapatıldı. Yeni süreç açıldığında kalıcı snapshot byte-for-byte korundu; aynı oyun kimliği, tahta, ipucu/hata ve geri alma geçmişi geri geldi. İpucu sayısı 1, korunan history 2. Kayıt işaretinin ardından süreç öldürme doğrulandı; henüz tamamlanmamış IO için sıfır kayıp garantisi verilmez.

**Tam oyun:** Üreticinin gerçek Kolay bulmacasında ipucundan sonra kalan **40 hücre** UI dokunuşlarıyla girildi. Bitiş ekranı gözlemlendi; 81/81 dolu hücre, 1 ipucu, 0 hata, 03:02 test süresi, **tek istatistik kaydı**. İstatistikte en iyi ve ortalama süre aynı oyunla eşleşti. Bütün bu akış uçak modunda geçti. Bu süre performans veya insan çözüm süresi ölçümü değildir.

Ana menü, açık/koyu oyun ekranı, notlar, restored oyun, bitiş ve istatistik ekran görüntüleri alınıp görsel olarak incelendi. İlk açılışa ait System UI ANR görüntüsü teslim ekran görüntülerine dahil edilmedi.

## Tamamlanan özellikler

| No | Özellik | Durum / kanıt |
|---|---|---|
| 1 | Ana menü ve yeni oyun | Tamamlandı; UI testi + uçak modu yeni oyun |
| 2 | 9x9 tahta | Tamamlandı; görsel inceleme ve UI hücreleri |
| 3 | Kolay, orta, zor, uzman | Tamamlandı; 16 generator örneği ve uzman UI seçimi |
| 4 | Kurallı üretim ve çözüm doğrulama | Tamamlandı; JVM uniqueness/solver testleri |
| 5 | Sayı girişi / seçim | Tamamlandı; UI testleri ve 40 gerçek hücre |
| 6 | Not modu | Tamamlandı; UI + kayıt codec roundtrip |
| 7 | Geri al ve sil | Tamamlandı; UI + core kuralları, 100 hamle sınırı |
| 8 | İpucu | Tamamlandı; doğru rakamı verir; süre cezası yok |
| 9 | Hata kontrolü | Tamamlandı; çözüme göre vurgulama, ayarlanabilir |
| 10 | Süre sayacı | Tamamlandı; monotonic clock + pause/background testleri |
| 11 | Otomatik kayıt / devam | Tamamlandı; atomik yerel dosya + force-stop senaryosu |
| 12 | Bitiş / istatistik | Tamamlandı; adet, en iyi/ortalama süre; tek kayıt doğrulaması |
| 13 | Açık / koyu tema | Tamamlandı; ayar persistence ve ekran incelemesi |
| 14 | Türkçe metinler | Tamamlandı; ürün UI'si Türkçe |

## Sınırlar ve sonraki aşamalar

İstenen 14 temel özellik uygulanmıştır. İleri açıklamalı ipuçları, farklı tahta boyutları/Killer, export/cloud backup, release imzası ve mağaza dağıtımı sonraki aşamadır. Yerel kayıt Room yerine sürümlü/checksum'lı atomik dosya deposudur; tam olarak oyun ve ayar dokümanına uygundur. İstatistikler son 1.000 tamamlanan oyunu, undo geçmişi son 100 hamleyi tutar.

Fiziksel telefon, Android API 26, tablet/katlanır cihaz, geniş font ölçeği, tam TalkBack kullanıcı testi, pil/uzun oturum performansı ve her olası enerji kesintisi davranışı doğrulanmadı. Bu alanlar çalışıyor olarak raporlanmıyor. Release öncesinde daha geniş cihaz/erişilebilirlik ve zorluk corpus testi gerekir.

## Kanıt dosyaları

- JVM XML/HTML: `core/build/test-results/test` ve `core/build/reports/tests/test`.
- Android XML/HTML: `app/build/outputs/androidTest-results/connected/debug` ve `app/build/reports/androidTests/connected/debug`.
- Lint HTML: `app/build/reports/lint-results-debug.html`.
- Son build çıktısı: `C:/APK-Lab/work/verified-build-tests.log`.
- Ek senaryolar: `C:/APK-Lab/work/device-smoke-results.json`, `completion-smoke-results.json`, `apk-verification.json`.

Kaynak ZIP'i build/cache/SDK ve makineye özgü local.properties içermez. Android Studio ZIP açılışında SDK yolu yeniden oluşturulmalıdır. Kanıt raporları ayrı verification ZIP'inde yer alır.
