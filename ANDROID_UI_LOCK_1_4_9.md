# BilgiliMedia Android — 1.4.9 UI Lock

Onay tarihi: 25 Ekim 2026

## Onaylanan baseline

Kullanıcı tarafından gerçek cihazda onaylanan son QA artifact:

- Dosya: `BilgiliMedia_149_QA_LOGO_KUCUK_YAZI_TAM.apk`
- SHA-256: `3d7fe84c2dd826539b6eec5858277ef6b04d91d77dedef5c72cc2e58215275db`
- QA package: `com.bilgilimedia.mobile.qa`
- Production source package: `com.bilgilimedia.mobile`
- versionName: `1.4.9`
- versionCode: `23`

## Kilitli UI/UX kararları

- BilgiliMedia üst başlık görünümü.
- Kırmızı oynatıcı sembolü kompakt boyut.
- Alt başlık: `İçerikler • Medya`.
- Hızlı Erişim / İçerik Kanallarımız ana sayfa yerleşimi.
- Bildirimler ekranında teknik Firebase metni gösterilmez.
- Bildirim kategori seçimi tek blok olarak yeniden çizilir.
- Hesaplar ekranında platformlar arası geçiş ekran içinde yapılır.
- Android geri: alt ekrandan ana sayfa; hızlı ikinci geri çıkış onayı.
- İçerik paylaşımı: uygulama varsa app-first; yoksa web fallback.
- Uygulama paylaşımı: app-first; yoksa BilgiliMedia web fallback.

## Değişiklik politikası

Bu baseline `1.4.9` için final UI kabulüdür.

Bundan sonra:
- bug fix ve güvenlik düzeltmeleri UI'yı değiştirmemelidir;
- görsel/akış değişikliği istenirse yeni açık değişiklik kararı ve yeni cihaz QA gerekir;
- Google Play için üretilecek AAB bu baseline ile görsel/işlevsel parity göstermelidir.
