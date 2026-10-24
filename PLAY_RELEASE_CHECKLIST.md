# BilgiliMedia — Google Play yayın kontrol listesi

Bu dosya 1.4.8 golden referansını ve production 1.4.9 release adayını kapsayan yayın kapısıdır. **PR #22 final CI + fiziksel golden regresyon tamamlanmadan `main`e merge/release yapılmaz.**

## 1. Altın referans / arayüz

- [x] Orijinal arayüzlü 1.4.8 APK referans olarak sabitlendi.
- [x] Referans SHA-256: `dca81609a1df783de5606ee646af4bd672121aed0188a84886db1c0652ccae76`.
- [x] Preview paket: `com.bilgilimedia.mobile.media.preview`.
- [x] Preview imza sertifikası App Links kaydıyla eşleşiyor.
- [x] Yeniden üretilebilir native production kaynak adayı PR #22 altında oluşturuldu.
- [x] Golden ekran/renk/metin/back davranışı kaynak sözleşmesi CI validator'a bağlandı.
- [ ] Final Play build'i 1.4.8 golden ile gerçek cihazda görsel ve işlevsel regresyon testinden geçti.

## 2. Android build / Play AAB

- [x] Production package: `com.bilgilimedia.mobile`.
- [x] Release adayı: versionCode `23`, versionName `1.4.9`.
- [x] compileSdk/targetSdk: 36.
- [x] minSdk: 26.
- [x] CI Android lint + debug/release APK + release AAB üretimini zorunlu kılıyor.
- [x] CI package/version/minSdk/targetSdk değerlerini artifact üzerinde doğruluyor.
- [x] CI APK ZIP bütünlüğünü ve `zipalign -P 16` kontrolünü yapıyor.
- [x] CI AAB'de `PAGE_ALIGNMENT_16K` kontrolünü bundletool ile yapacak şekilde sabitlendi.
- [x] CI arm64-v8a/x86_64 ELF LOAD ve GNU_RELRO hizasını Android NDK LLVM araçlarıyla denetliyor.
- [ ] PR #22 **final head** Android artifact audit'i tamamen yeşil.
- [ ] Final AAB upload/production anahtarıyla imzalandı.
- [ ] Final AAB/Play-generated APK seti 16 KB page-size emulator/device veya Play raporuyla da doğrulandı.

## 3. Firebase / bildirim

- [x] Firebase Console'da production paket `com.bilgilimedia.mobile` kayıtlı.
- [x] Production Firebase app/project/sender yapılandırması final Android kaynakta kullanılıyor; preview Firebase kimliği production build'de kullanılmıyor.
- [x] Backend FCM gönderimi OIDC/WIF ile anahtarsız çalışıyor.
- [x] FCM payload `category_id` taşıyor.
- [x] FCM topic subscribe/unsubscribe yalnız Firebase başarı sonucundan sonra local state'e yazılıyor.
- [x] Hızlı tercih değişimlerinde son kullanıcı isteğine geri yakınsama mantığı var.
- [x] Kategori tercihleri görünen ada değil stable category ID'ye migrate edildi.
- [x] Feed'deki mevcut içerikler stable `categoryId` ile migrate edildi.
- [x] Explicit boş kategori seçimi “tüm kategoriler” olarak yorumlanmıyor.
- [x] Bildirim gerçekten gösterilmediyse içerik “bildirildi” sayılmıyor.
- [ ] Android 13+ bildirim izni gerçek cihazda test edildi.
- [ ] FCM token yenileme/topic subscribe-unsubscribe gerçek cihazda test edildi.
- [ ] AI/HIGGS dahil uzaktan kategori ekleme/yeniden adlandırma testi yapıldı.
- [ ] Reboot/update sonrası job/topic testi yapıldı.

## 4. Remote content / cache / genişletilebilirlik

- [x] Feed/config yalnız HTTPS ve BilgiliMedia trusted host üzerinden alınıyor.
- [x] Redirect, boyut sınırı ve JSON/şema doğrulama kontrolleri var.
- [x] Feed + config tek mantıksal snapshot olarak birlikte doğrulanmadan cache'e yazılmıyor.
- [x] Bozuk/uyumsuz remote veri last-known-good cache -> local prefs -> bundled fallback sırasıyla güvenli biçimde ele alınıyor.
- [x] Cache yazımı `AtomicFile` ile korunuyor.
- [x] Schema v2 navigation depth/item count ve referansları bounded/strict.
- [x] Remote navigation hidden developer ekranını açamıyor.
- [x] Server validator ve Android parser kategori/page/navigation sözleşmeleri hizalandı.
- [x] Yeni içeriklerde stable `categoryId` repository validator tarafından zorunlu.
- [ ] Remote malformed/mismatched feed-config gerçek cihaz ağ senaryosunda test edildi.

## 5. App Links

- [x] Preview `assetlinks.json` kaydı mevcut.
- [x] Production paket `com.bilgilimedia.mobile` için mevcut production sertifikası kaydı mevcut.
- [ ] Play Console > App integrity > **App signing certificate** SHA-256 ile production `assetlinks.json` birebir karşılaştırıldı.
- [ ] Gerekirse birden fazla dağıtım fingerprint'i eklendi.
- [ ] App Link -> doğru içerik ve tarayıcı fallback gerçek cihazda test edildi.

## 6. İmza ve güncelleme sürekliliği

- [x] Mevcut üretim imza yedeği sertifika SHA-256: `F6:84:6E:CB:EC:19:CC:99:DB:68:2A:37:4F:47:8F:D1:9B:59:E6:96:54:2E:C5:0F:16:B5:33:0E:DD:DF:CF:6A`.
- [x] Özel anahtar/parola GitHub'a eklenmedi.
- [x] Root `.gitignore` keystore/PKCS#12/service-account materyalini engelliyor.
- [x] CI tracked signing/private-key materyalini reddediyor.
- [x] `scripts/sign-android-aab.sh` environment tabanlı secret-safe signing + strict verify + opsiyonel cert SHA-256 gate sağlıyor.
- [ ] Play App Signing mevcut production imza sürekliliği gözetilerek yapılandırıldı.
- [ ] Upload key/app signing key ayrımı Play Console'da finalleştirildi.
- [ ] Final upload-signed AAB üretildi ve SHA-256 kaydedildi.

## 7. Gizlilik / Data Safety / reklam

- [x] Public privacy policy güncel.
- [x] FCM/Firebase Installations ve Firebase Hosting teknik işleme açıklanıyor.
- [x] Konum/kamera/mikrofon/rehber/SMS/genel depolama izni yok.
- [x] Analytics, Crashlytics ve AdMob yok.
- [x] Sponsor sistemi `SPONSOR_FEATURE_ENABLED=false` + `sponsorAds.enabled=false` ile çift kilitli.
- [x] Repository validator mevcut release'te uzaktan sponsor açılmasını reddediyor.
- [ ] Play Data Safety final formu final AAB davranışıyla birebir dolduruldu.
- [ ] Device or other IDs / amaç / sharing / encryption seçenekleri Play Console'un güncel metniyle doğrulandı.
- [ ] Privacy URL Play Console'a girildi.
- [ ] Sponsor ileride açılmadan önce Contains ads + privacy + Data Safety güncellendi.

## 8. Play Console / geliştirici hesabı

- [ ] Geliştirici kimlik doğrulaması tamamlandı. **Kimlik belgesi bekleniyor.**
- [ ] `com.bilgilimedia.mobile` paket kaydı/doğrulaması tamamlandı.
- [ ] App access doğru beyan edildi.
- [ ] Ads declaration mevcut locked release ile uyumlu dolduruldu.
- [ ] IARC içerik derecelendirme tamamlandı.
- [ ] Hedef kitle tamamlandı.
- [ ] News declaration tamamlandı.
- [ ] Hesap türüne göre gerekiyorsa kapalı test/production access şartı tamamlandı.

## 9. Mağaza varlıkları

- [x] 512×512 ikon mevcut.
- [x] 1024×500 feature graphic mevcut.
- [x] Türkçe store listing metni mevcut.
- [x] Support email ve privacy URL hazır.
- [ ] Ekran görüntüleri final production build'den yeniden çekildi.
- [ ] Store metni final kategori/özellik setiyle son kez karşılaştırıldı.

## 10. Fiziksel cihaz QA

- [ ] Temiz kurulum.
- [ ] Eski `com.bilgilimedia.mobile` üzerine güncelleme.
- [ ] API 26.
- [ ] Android 13+ notification permission.
- [ ] Android 16 / API 36.
- [ ] 16 KB page-size ortamı.
- [ ] Wi-Fi / mobil / offline / yavaş ağ.
- [ ] Geri tuşu + gesture back + ana sayfada kapanmama.
- [ ] Yeniden açma + rotation.
- [ ] Büyük font / erişilebilirlik.
- [ ] Favoriler/geçmiş/tema/bildirim tercihleri update sonrası korunuyor.
- [ ] Sosyal link app/browser fallback.
- [ ] App Links.
- [ ] Remote yeni kategori/rename/topic değişimi.
- [ ] Remote bozuk feed/config güvenli fallback.

## 11. CI / kaynak güvenliği

- [x] `main` protected; PR zorunlu.
- [x] Required `validate` status check.
- [x] Force push ve branch silme engelli.
- [x] Required validate Android release Java'yı gerçek derliyor.
- [x] Android workflow lint + APK/AAB + artifact identity + 16 KB denetliyor.
- [x] CodeQL Java/Kotlin + JavaScript.
- [x] Dependabot npm + Gradle.
- [x] GitHub Actions bağımlılıkları commit SHA'larına pinli.
- [x] `package-lock.json` + `npm ci`.
- [x] Google Cloud OIDC/WIF; service account JSON key yok.
- [x] Hosting CSP ve temel güvenlik başlıkları.
