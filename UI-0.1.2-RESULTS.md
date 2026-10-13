# Dingin Sudoku 0.1.2 — arayüz değişiklikleri ve test raporu

13 Ekim 2026. Referans: kullanıcının gerçek Redmi telefonundan gönderdiği 0.1.1 ana menü, iki oyun ve istatistik JPEG görüntüsü. Fotoğraflar görsel referans olarak kullanıldı; yeni sürümün aşağıdaki doğrulaması API35 emülatöründe yapıldı. Yeşil/krem palet, serif başlıklar, D monogramı ve Türkçe tasarım kimliği korunmuştur.

## Sürüm güvencesi ve kapsam

Değişiklikten önce temiz 0.1.1 commit’i `b3ba9ea12c7c3c6fbd00af01c4f3ac79a9cb1b10`, mevcut `v0.1.1` etiketine ek olarak `baseline-v0.1.1` ile korundu. Yerel Git bundle yedeği alındı. Çalışma dalı `responsive-v0.1.2`.

Paket `com.dingin.sudoku`, sürüm adı **0.1.2**, versionCode **3**. Önceki debug sertifikası korunmuştur: SHA-256 `b8f2ce01db4f5d421b834ecdbff861dc445a4eb3b7711194bef8d3eb621469d0`. APK `apksigner verify --print-certs` ile doğrulandı; `aapt2 dump badging` paket ve sürümü doğruladı. Kaynak arşivi özel imza anahtarını içermez; başka makinede aynı imzayla derlemek mevcut anahtarın güvenli aktarımını gerektirir.

`core/`, `GameViewModel.kt`, `MainActivity.kt` baseline ile aynıdır. Çözücü, üretim, tek çözüm, hamleler, ipuçları, undo, süre, kayıt kuyruğu, ProfileCodec/ProfileStore ve kayıt şeması 1 değiştirilmedi. İstatistik ve ilerleme hesapları arayüz katmanına eklendi; yeni kayıt alanı veya veri göçü yok.

## Uygulanan iyileştirmeler

1. **Dengeli oyun düzeni:** oyun içeriği kullanılabilir yüksekliği doldurur; tahtanın ve kontrollerin iki grubu kalan alanı paylaşır. Alt açıklama uzun telefon ekranında ekranın altına yaklaşır. Kompakt oyun başlığı ve daha az ara boşluk kullanıldı. Tahta kare kalır; genişlik en fazla 450dp, yükseklik ve yazı ölçeğiyle hesaplanan boyutta 270dp alt hedefi vardır. Daha dar pencere genişliği bu hedefi sınırlayabilir. Kısa ekranda kontrolleri kesmek yerine dikey kaydırma vardır.
2. **Dokunulabilir sayı tuşları:** dar ekranda 5+4, yeterince geniş ekranda 9’lu sıra. Tuşların minimum yüksekliği 56dp. Dört araç çubuğu alanının 64dp minimum yüksekliği korundu; etiketler 12sp oldu. Eylem sırası ve davranış değişmedi.
3. **Yumuşak tahta:** seçim, aynı rakam ve ilgili satır/sütun/blok zeminleri iki temada birbirine yaklaştırıldı. Hata zemini, ünlem ve seçim köşeleri korunur. Öncelik hâlâ hata → seçim → aynı rakam → ilgili hücre → taban. Başlangıç/sabit rakamlar ve kullanıcı sayıları ayrı yazı ağırlığıyla gösterilir.
4. **Duyarlı ana menü:** dar genişlikte veya >%120 yazı boyutunda hero başlığı 28sp, diğer durumda 34sp; önceki 38sp’ye göre daha kompakt. İç boşluklar azaltıldı, açıklamalar opak ve daha okunabilir oldu. Üst küçük metin kalan genişliği kullanıp sarılır. Dar durumda dekoratif grid kaldırılır. 320dp’den dar içerikte veya >%130 yazı boyutunda zorluk kartları tek sütuna geçer. Ayarlardaki tema seçenekleri gerektiğinde satıra sarılır. İçerik dikey kaydırılabilir; tüm menünün her küçük ekranda aynı anda görünmesi amaçlanmaz.
5. **İnce ilerleme çubuğu:** 3dp yükseklik. Başlangıç rakamları, düzenlenebilir toplam ve doğru doldurulanlar ayrı sayılır. Oran = doğru doldurulan düzenlenebilir hücre / başlangıçta boş hücre. Yeni oyunda 40 başlangıç rakamı olsa bile ilerleme %0’dır. Yanlış rakamlar ve aday notları ilerleme sayılmaz; silme/geri alma güncel duruma yansır. İpucuyla doğru doldurulan hücreler dahildir; bunların elle girildiği iddia edilmez. Hata kontrolü kapalıyken de ilerleme doğru hücre hesabıdır.
6. **Daha bilgilendirici istatistikler:** en iyi ve ortalama süre, kayıtta bulunan tamamlanmış oyunlardan; toplam süre aynı kayıtlar ve mevcut oyundan hesaplanır. Mevcut oyunun kimliği tamamlanmış kayıtlarda varsa süresi ikinci kez eklenmez. Veri yoksa en iyi/ortalama “—” olur. Süre biçimi dakika:saniyedir; toplam dakika 59’u aşabilir.

### İstatistiklerin gerçek sınırı

Kayıt biçimi son 1.000 tamamlanan oyunu ve mevcut tek oyunu içerir. Önceki bırakılmış oyunların süreleri ve başlatılan/bırakılan oyun sayısı tutulmaz. Dolayısıyla tüm geçmişin toplam süresi veya gerçek başarı yüzdesi hesaplanamaz.

**Başarı oranının yüzdelik hesabı uygulanmadı.** Arayüz “Hesaplanamıyor” der ve nedenini açıklar. Tamamlanan kayıtları kendi sayısına bölüp sahte %100 üretilmedi. “Kayıtlı toplam oyun süresi” ve kayıt kapsamı metni, gösterilen sürenin tüm geçmiş olmadığını açıklar. Mevcut veriden istatistik üretme şartı bu şekilde korunmuştur.

## Renk ve kontrast

| Rol | Açık | Koyu |
|---|---|---|
| İlgili satır/sütun/blok | `#F1F4EF` | `#1F3029` |
| Aynı rakam | `#F3ECDD` | `#39362B` |
| Seçili hücre | `#E1EDE5` | `#2B4036` |
| Hata zemini — korundu | `#FBE2DC` | `#4D2829` |

Açık temanın küçük ikincil metin rengi `#53665C` oldu. Otomatik testler aktif normal metnin ana arka plan/yüzey/yüzey varyantında, rakam/not metninin tahta vurgu zeminlerinde ve hata yazısının hata zemininde ≥4,5:1 kontrastını doğrular. Belirli seçim/ızgara renk çiftleri için mevcut ≥3:1 kontrolleri de geçti. Devre dışı eylemler ayrı, soluk durumdadır; aktif metin kriteri onlara uygulanmış gibi raporlanmaz.

## Otomatik test ve derleme

- **17/17 motor/kayıt JVM testi**, ayrıca `:core:test --rerun-tasks` ile taze çalıştırıldı.
- **18/18 arayüz JVM testi**: önceki 8 tahta testi ve yeni 10 ilerleme, kayıtlı süre/istatistik, yerleşim ve kontrast testi.
- **11/11 Android emülatör testi**: önceki 8 test, yeni 2 ilerleme/istatistik entegrasyon testi ve 1 gerçek piksel üzerinden tema geçişinde marka görünürlüğü testi. Hatalı girişin ilerlemeyi artırmadığı, ipucunun doğru hücreyi saydığı, iki kaydın 01:00 en iyi / 01:30 ortalama / mevcut 00:30 ile 03:30 toplam gösterdiği ve başarı oranının uydurulmadığı doğrulandı.
- Toplam **46 farklı test geçti; 0 başarısız, 0 atlanan**.
- `:app:assembleDebug` başarılı. `:app:lintDebug`: **0 hata, 7 uyarı**, mevcut sabit Gradle/AndroidX/coroutines sürümlerinin güncelleme uyarıları; bağımlılıklar yükseltilmedi.
- Son birlikte çalıştırılan görevler: `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:connectedDebugAndroidTest`.
- `ANDROID_SERIAL=emulator-5554`; kullanıcı telefonunda kayıt sıfırlayan instrumentation testleri çalıştırılmadı.

## Emülatör güncellemesi ve ekran matrisi

0.1.1 debug APK üzerinde yeni gerçek bulmaca ve aday not kaydı oluşturuldu. 0.1.2 `adb install -r` ile üzerine kuruldu. Kayıt dosyası güncelleme ve ilk soğuk açılış sonrası bayt bazında aynı kaldı; devam edildiğinde notlar korundu. Yeni sürümün force-stop/yeniden açılış kontrolü de geçti. Son monogram düzeltmesi sonrası final APK ile ayrıca 29.319 baytlık tamamlanmış oyun/istatistik profili 0.1.1 üzerinde okunup 0.1.2’ye yükseltildi; eski açılış, kurulum ve yeni soğuk açılış sonrası dosya yine aynı kaldı.

| Senaryo | Piksel / yoğunluk | Yaklaşık dp | Yazı ölçeği | Sayı tuşları |
|---|---|---|---|---|
| Kısa telefon | 1080×1920 / 480 dpi | 360×640 | %100 | yaklaşık 62×56 dp; kaydırma ile tamamı erişildi |
| Uzun telefon | 1080×2400 / 420 dpi | 411×914 | %100 | yaklaşık 69×56 dp; tamamı görünür |
| Büyütülmüş yazı | 1080×2400 / 420 dpi | 411×914 | %130 | yaklaşık 69×56 dp; tamamı görünür |

Dokuz sayı tuşunun gerçek erişilebilirlik düğüm alanları ölçüldü ve ≥48dp hedefi geçti. Her senaryoda açık/koyu oyun ve ana menü PNG’leri üretildi. Kısa ekran için üst ve kaydırılmış kontrol görüntüleri ayrı teslim edilir; bunlar farklı sayfalar değildir. Piksel/yoğunluk değişiklikleri test sonunda sıfırlandı.

Üretilen gerçek bulmaca UI üzerinden tamamlandı: ara görüntüde 40 başlangıç rakamı, 10 doğru doldurulan / 41 düzenlenebilir hücre ve ayrıca bilerek girilmiş bir hata vardı. Kalan 31 doğru hamle sonrası oyun tamamlandı. Kayıt süresi 372284 ms; ekranda en iyi, ortalama ve toplam **06:12** göründü. Aynı tamamlanmış oyun ikinci kez toplama eklenmedi. Bu ekranlar sahte istatistik profili değil, emülatörde uygulama tarafından üretilip hamlelerle tamamlanan bir oyunun gerçek kaydıdır.

Görsel kontrolde tema değiştirildiğinde monogramın emülatörde zaman zaman boş çizildiği gözlendi. Tema değişiminde vektör ressamı/draw katmanı yeniden oluşturularak giderildi. Ek Android testi açık → koyu → açık geçişlerinin her birinde ikonun merkez alanında krem monogram piksellerini doğruladı. Bu düzeltme sonrası APK yeniden derlendi ve 46 testin tamamı geçti.

## Test sınırları ve teslimat

Yeni 0.1.2 fiziksel Redmi telefona kurulmadı ve fiziksel telefonda test edilmedi. Telefondaki 0.1.1 ve kişisel kayıtları bu turda değiştirilmedi. Kullanıcının gönderdiği JPEG’ler yalnız önceki sürümün gözlemleridir.

TalkBack ile uçtan uca kullanım, %130’un üstündeki yazı ölçekleri, yatay/tablet ekranları ve farklı Android/üretici sürümleri bu turda cihaz üzerinde doğrulanmadı. 9×9 tahtadaki yoğun hücre geometrisi tüm telefonlarda 48dp dokunma alanı sağlamaz; sayı/aksiyon tuşlarının ölçüm sonucunu bütün arayüze genellememek gerekir. Görsel inceleme ve kontrast testleri tam erişilebilirlik sertifikası değildir. Notlar yoğun gridde küçük kalır; hücre erişilebilirlik açıklamasında adaylar ayrıca listelenir.

Kaynak proje: `C:/APK-Lab/my-android-app`. Debug derleme çıktısı: `C:/APK-Lab/my-android-app/app/build/outputs/apk/debug/app-debug.apk`. Teslimatta 0.1.2 APK, Android Studio kaynak ZIP, açık/koyu ekranlar, kısa ekran/büyük yazı görüntüleri, test kanıtları ve Git geçmişi bulunur. Önceki sürümlerin teslim dosyaları korunmuştur.
