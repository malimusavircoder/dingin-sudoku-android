# Dingin Sudoku 0.1.3 — değişiklik ve doğrulama raporu

13 Ekim 2026. Mevcut 0.1.2 kaynakları, 09 raporu, 46 testin kapsamı ve açık/koyu, kısa ekran/büyük yazı PNG’leri kod değişikliğinden önce incelendi. Kısa teknik değerlendirme kanıt arşivinde bulunur. Yeşil/krem palet, serif başlıklar, Dingin adı ve D monogramı korunmuştur.

## Git ve veri güvencesi

Başlangıç temiz commit `ecc32650c693f7ccbea9341592c25e5fe33ba7a2`, `baseline-v0.1.2` etiketi ve Git bundle ile korundu. Çalışma `polish-v0.1.3` dalında yapıldı. Yeni sürüm versionCode 4, versionName 0.1.3, applicationId/namespace `com.dingin.sudoku`.

`core/`, `GameViewModel.kt`, `MainActivity.kt`, manifest ve tüm res varlıkları baseline ile aynıdır. Üretim, doğrulama, tek çözüm, zorluk değerlendirmesi, hamleler, undo, ipucu, PlayClock, ProfileCodec/ProfileStore ve şema 1 değişmedi. İmza yapılandırması değiştirilmedi, bağımlılık eklenmedi. Debug sertifikası SHA-256 `b8f2ce01db4f5d421b834ecdbff861dc445a4eb3b7711194bef8d3eb621469d0`. Kaynak arşivi özel anahtar içermez; başka makinede aynı imzayla üretim mevcut anahtarı gerektirir.

## Yapılan değişiklikler

- **Tahta:** satır/sütun/bölge içinde aynı değeri taşıyan her iki hücre çakışma olarak gösterilir. Bağlantısız eşit rakamlar çakışmaz; boş hücreler sayılmaz. × çakışmayı, ! yalnız çözüme uymamayı belirtir. Sabit doğru rakam da yanlış kullanıcı girdisiyle çakışan çiftin bir parçası olabilir; bu, sabit rakamın değiştirilebileceği anlamına gelmez.
- **Öncelik:** hata/çakışma → seçim → aynı rakam → satır/sütun/3×3 ilişki → taban. Seçili hatalı hücre kırmızı kalır; yeşil seçim işareti ayrıca korunur. Hata kontrolü kapalıyken hata ve çakışma sunumu kapanır. Motorun hata sayacı ve giriş kabul kuralları değişmez.
- **Renk dışı ayrım:** seçim köşeleri, notlu hücrede ince çevre çerçevesi, eş rakamda küçük alt çizgi, ×/! işaretleri, sabitte daha kalın/nötr ve kullanıcı rakamında normal/yeşil yazı, 3×3 için kalın çizgi. Hücre açıklamaları Türkçe satır/sütun, sabit, yanlış, çakışma ve not durumunu içerir.
- **Sayı araçları:** seçili rakam veya açık not modundaki işaretli adaylar sayı tuşunda çerçeve ve ağırlıkla gösterilir. Not modu etiket/alt çizgiyle açık kalır. Dolu hücrede aday eklemek motor tarafından zaten etkisizdi; tuşlar artık bu durumda devre dışıdır ve dolu değeri yanlışlıkla aday gibi göstermez. Boşluk 6dp’ye çıkarıldı; min yükseklik 56dp, araç çubuğu 64dp korunur. Geri al/sil/ipucu işlevleri ve ikonları korunmuştur.
- **Sonuç:** tahta altında kalan eski kart yerine ayrı panel. Yalnız finished ve gerçek çözüm/kurallarla eşleşen tam tahta için açılır. Süre, zorluk, aynı zorluktaki gerçek kayıtlar arasındaki en iyi süre, hata/ipucu özeti, aynı seviyede yeni oyun ve ana menü butonları sunar. En iyi için kaydı olmayan durumda süre türetilmez.
- **Kutlama:** yalnız onay ikonunda 1→1,06→1 ölçek, 300ms. Konfeti veya parlak renk yok. Sistem animator kapalı ya da Compose motion scale sıfır ise statik gösterilir; yapılandırma yeniden oluşturulmasında saveable durum tekrar hareketi önler. Sonuç/başarı kaydı animasyona bağlı değildir.
- **İstatistik:** en iyi/ortalama kartları normal yazıda yan yana, büyük yazıda alt alta. Kayıtlı ilerleme, zorluk/süre ve oyuna devam/son sonucu gör eklendi. Seviye bazında adet ve süreler korunur. Son 1.000 tamamlanan kayıt ve mevcut oyundan hesaplama yapılır; mevcut tamamlanma iki kez toplanmaz. Başlatılan/bırakılan geçmiş bulunmadığından başarı oranı “Hesaplanamıyor” olarak kalır, açıklaması gösterilir.
- **Uyum:** yeterince geniş yatay pencerede sol tahta, sağ başlık/araçlar/tuşlar. Büyük yazıda tek sütun geri dönüşü vardır. Dikey oyun grupları daha küçük aralıkla ortalanır; kısa ekranda gerekli kaydırma korunur. Ana menü başlık aralığı azaltıldı. Ekranlar arasında kaydırma konumu taşınması giderildi.
- **Hesap yükü:** zaman/geçmiş güncellemelerinden ayrı grid girişleri, remember ile hata/çakışma/vurgu ve ilerleme hesapları. Bu bir FPS/enerji benchmark sonucu değildir; performans sayısı üretilmemiştir.

## Görsel kontrolde giderilen durumlar

Dokuz adayın seçimin köşe işaretiyle karışması, notlu hücrede çevre çerçevesi kullanılarak giderildi. Büyük yazıdaki rakam/not/uyarı boyutları hücre alanına sığacak şekilde sınırlandı; bilgi erişilebilirlik açıklamasında da bulunur. Dolu hücrede not modu için yanlış aday seçimi durumu temizlendi. Sonucun tahtanın altında kalması ve menü kaydırmasının oyun/istatistiğe taşınması giderildi.

## Testler ve derleme

Mevcut 46 test korunmuştur. 16 yeni testle toplam **62/62 geçti**: core 17, uygulama JVM 28, Android emülatör 17. Hata, başarısızlık ve atlama sayısı sıfırdır. Yeni kapsam: satır/sütun/bölge çakışmaları, vurgu önceliği, rakam/adayı seçimi, dolu hücrede not durumu, geçersiz bitiş, gerçek seviye rekoru, aynı seviyede yeni oyun, azaltılmış hareket, döndürmede hamle/not korunması ve bozuk kayıt kurtarma/bloklama. Eski süre/istatistik/undo/kayıt testleri korunur.

Son komut: `:core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:connectedDebugAndroidTest`. Core ayrıca `--rerun-tasks` ile çalıştırıldı. Debug derleme başarılı; lint **0 hata, 7 bağımlılık sürümü uyarısı**. HTML/XML sonuçlar ve tam derleme günlükleri verification ZIP içindedir. Android testleri yalnız emulator-5554 cihazında çalıştırılmıştır.

## Güncelleme ve gerçek ekranlar

API35 emülatöründe gerçek `adb install -r` ile 0.1.2 → son 0.1.3 APK güncellemesi yapıldı. 8.583 baytlık şema 1 test profili, kurulum sonrası ve ilk ana menü açılışı sonrası bayt bazında aynı kaldı. Devamda oyun kimliği, değerler, dokuz aday notu, geçmiş, hata/ipucu ve eski istatistik kaydı korundu. Önceki 4.047 baytlık senaryo da geçti. Bu profiller emülatör test verisidir; telefondan alınmadı.

Üretim motorunun oluşturduğu Kolay oyun, arayüzden 40 hamleyle gerçekten tamamlandı: kayıtlı süre 440.918 ms (07:20), 0 ipucu, 1 hata. Sonuçta eski gerçek seviye rekoru 06:12 gösterildi. İstatistikte iki tamamlanma, ortalama 06:46 ve toplam 13:33 görüntülendi; sahte başarı yüzdesi eklenmedi. Orta ve Zor yeni oyunlar arayüzden üretilip görüntülendi. Test yardımcı betiği çözümü yalnız emülatör test girdilerini seçmek için okudu; uygulamaya çözüm veya sahte ekran eklenmedi.

Ekran matrisi: küçük 1080×1920 / 480 dpi; büyük 1080×2400 / 420 dpi ve font_scale 1,5; yatay 2400×1080 / 420 dpi. Her durumda dokuz giriş tuşu ölçüldü: minimum genişlik ≥59,66 dp ve yükseklik ≥56 dp; 48 dp kontrolü geçti. Kısa/büyük yazı düzeninde gerektiğinde kaydırma, geniş yatay düzende iki sütun kullanıldı. Açık/koyu seçim, çakışma, not, sonuç ve istatistik ekranları ayrıca incelendi.

İlk yatay ölçüm betiği soldaki tahta rakamlarını da tuş olarak saydığı için durdu (22 yerine 9 bekleniyordu). Sağ kontrol alanı filtresi düzeltildi; üç durum yeniden çalıştırılıp geçti. Bu yardımcı betik hatası ve düzeltilmiş betikler kanıt arşivindedir. Bu durum uygulama/test başarısızlığı olarak gizlenmemiştir.

Ekran ZIP’indeki PNG’ler gerçek emülatör screencap çıktılarıdır. Menü, Kolay/Orta/Zor, seçili rakam, hata, dokuz aday notu, sonuç, istatistik, iki tema, küçük ekran, büyük yazı ve yatay görünüm içerir.

## Kalan sınırlar

Yeni sürüm fiziksel telefona kurulmadı veya fiziksel telefonda test edilmedi; kişisel telefon verilerine dokunulmadı. Görüntüler API35 emülatöründen, kontrol edilmiş test oyunu/kayıtlarından alınmıştır. TalkBack ile uçtan uca kullanım, başka üretici/Android sürümleri ve tüm olası pencere/yazı ölçekleri doğrulanmış değildir. Yoğun 9×9 grid her telefonda 48dp hücre sağlayamaz; küçük not yazısı geometriyle sınırlandırılır. Kontrol hedefleri için ölçümü tüm arayüze genellemek doğru değildir.

Başarı yüzdesi ve tüm geçmişin toplam süresi için eksik bırakılmış oyun geçmişi tutulmaz. Kayıtlı en iyi/ortalama ve toplam yalnız mevcut kayıt kapsamını ifade eder. Asenkron kayıt bitmeden process/enerji kesilirse önceki sürümdeki küçük IO kayıp penceresi hâlâ vardır; sıfır kayıp garantisi verilmez. Bozuk ana dosyada doğrulanmış yedek kullanılır; ikisi de okunamazsa dosyalar korunur ve yeni oyun bloke edilir. Bu durumlar test kapsamındadır.

Kaynak proje `C:/APK-Lab/my-android-app`; derleme APK’si `C:/APK-Lab/my-android-app/app/build/outputs/apk/debug/app-debug.apk`. İstenen beş dosya outputs klasörüne teslim edilir. Eski teslimatlar korunur.
