# Dingin Sudoku

Türkçe, çevrimdışı Android Sudoku. Kotlin, Jetpack Compose, Material 3 ve MVVM ile sıfırdan geliştirilmiştir. LibreSudoku kodu, marka veya varlıkları kullanılmamıştır. Analiz raporları yalnız işlevsel referanstır.

## Açma ve derleme

Android Studio → Open → `C:\APK-Lab\my-android-app`. Gradle JDK olarak JDK 17 veya uyumlu Android Studio JBR seçin. Android SDK Platform 36 ve Build Tools 35.0.0 gerekir. `local.properties` makineye özgüdür; SDK yolu Android Studio tarafından yeniden oluşturulabilir.

PowerShell (bu makinede):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
# Yalnız test emülatöründe; bu testler uygulama kayıtlarını sıfırlar!
$env:ANDROID_SERIAL = 'emulator-5554'
.\gradlew.bat :app:connectedDebugAndroidTest
```

APK: `C:\APK-Lab\my-android-app\app\build\outputs\apk\debug\app-debug.apk`.

Sürümler sabitlenmiştir: AGP 8.13.0, Gradle 8.14.3, Kotlin/Compose compiler 2.2.10, Compose BOM 2025.09.01, minSdk 26, target/compileSdk 36. En yeni bağımlılıkları izlemek yerine ilk sürüm için uyumlu bir araç seti kullanılır. Sürüm yükseltme kontrolleri lint uyarısı verebilir.

## Özellikler

- Ana menü, yeni oyun ve devam etme.
- 9x9, dört zorluk, MRV/backtracking çözücü, iki çözümde duran benzersizlik kontrolü.
- Zorluk: çıplak tekli / gizli tekli / çift ve kilitli aday / ek arama gereksinimi. Uzman, bu sınırlı mantıksal yöntem setinin çözemediği bulmacadır; tüm insan Sudoku tekniklerinin bittiği anlamına gelmez.
- Hücre seçimi, sayı, not bitmask'leri, sil ve son 100 hamleye geri alma.
- Seçili veya uygun bir hücreye doğru sayı veren ipucu; süre cezası yok.
- Çözüme göre hata vurgusu; hatalı giriş sayacı geri alma ile düşmez.
- Monotonic saat farkı ile süre; ana menüde, duraklatmada ve arka planda durur.
- Her hamlede ve oynarken saniyede bir yerel kayıt; notlar, sayaçlar ve undo geçmişi saklanır.
- Tamamlanma ekranı; seviyeye göre adet, en iyi/ortalama süre; son 1.000 tamamlanan oyun.
- Sistem/açık/koyu tema ve tüm ürün metinleri Türkçe.

## Yapı

`core/`: Android bağımsız Kotlin motor, oyun kuralları, PlayClock, ProfileCodec/ProfileStore ve JVM testleri.

`app/`: AndroidViewModel + StateFlow, tek kayıt kuyruğu, lifecycle, Compose ekranları ve Android UI testleri.

Kayıt sistemi Room yerine uygun küçük bir yerel dosya deposudur: sürüm 1 binary belge, CRC32, geçici dosya fsync ve atomik replace; önceki doğrulanmış belge `.bak` olarak korunur. Oyun, istatistik ve ayarlar aynı işlemde yazılır. Eski revision yeniyi ezmez. Veri app-private `filesDir/profile.bin` içindedir; depolama/ağ izni gerektirmez. Hem ana hem yedek bozuksa dosyalar korunur ve UI yeni oyunu bloke eder. Otomatik yedek ve cihaz aktarımı kapalıdır.

Kayıt göstergesi tamamlandığında belge diske yazılmıştır. Asenkron kuyruğa yeni alınmış bir hamle hemen process kill ile kesilirse küçük bir kayıp penceresi vardır; tamamlanmamış IO için sıfır kayıp garantisi verilmez. İşletim sistemi/enerji kesintisinin her biçimi için garanti yoktur. Geri alma değer ve notları geri getirir; toplam hata/ipucu/süre geri alınmaz.

## Sonraki aşamalar

İleri açıklamalı ipuçları, dışa aktarma/yedekleme, release imzası, Play dağıtımı, daha kapsamlı erişilebilirlik/cihaz matrisi, profesyonel zorluk corpus kalibrasyonu. Killer ve farklı boyutlar ilk sürüm kapsamında değildir.

## Kaynaklar

- [AGP uyumluluğu](https://developer.android.com/build/releases/agp-8-13-0-release-notes)
- [Compose compiler kurulumu](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)
- [Compose BOM eşleştirmesi](https://developer.android.com/develop/ui/compose/bom/bom-mapping)

Doğrulama sonuçları teslim edilen test raporunda belirtilir. Kaynakta uygulanmış olmak, fiziksel cihazda doğrulanmış olmakla eşdeğer değildir.


## 0.1.1 görsel düzenleme

Yeşil/krem renkler, serif başlıklar ve Türkçe arayüz korunmuştur. Özgün D monogramı adaptif launcher ikonunda ve ana menüde kullanılır; Android 13+ için monochrome katmanı vardır. Tahta rolleri `BoardStyle.kt` içinde açık/koyu tema için ayrı opak renklerle tanımlıdır. Hata > seçim > aynı rakam > ilgili satır/sütun/blok > taban önceliği kullanılır. Hatalı seçim köşe işaretleriyle, hata ünlemle ayrıca anlatılır. Araç çubuğu ortak bir yüzeyde dört saydam dokunma alanı içerir; not modu etiket ve alt çizgiyle belirtilir.

Bu sürümde motor, ViewModel ve kayıt biçimi değiştirilmemiştir. Ayrıntılar `DESIGN-CHANGES.md` içindedir. `TEST-RESULTS.md` ilk 0.1.0 sürümünün tarihsel raporudur.
