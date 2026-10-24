# BilgiliMedia — Play Console cevap taslağı

Bu belge Play Console'daki güncel soru metniyle birlikte kullanılmalıdır. Form metni değişirse Play Console'daki güncel seçenekler önceliklidir.

## Temel uygulama bilgisi

- Uygulama adı: BilgiliMedia
- Production package: `com.bilgilimedia.mobile`
- Varsayılan dil: Türkçe (Türkiye)
- Uygulama türü: App
- Fiyat: Ücretsiz
- Login/account: Yok
- Privacy URL: `https://bilgilimedia-60d6f.web.app/privacy/`
- Destek e-postası: `sukrubatuhanbilgili@gmail.com`

## App access

Önerilen cevap:
- Uygulamanın tüm temel özellikleri giriş yapmadan kullanılabilir.
- Reviewer için kullanıcı adı/parola gerekmez.

## Ads

Sponsor sistemi kapalı ve reklam SDK'sı yokken:
- Contains ads: **No**.

Sponsor sistemi etkinleştirilmeden önce bu cevap yeniden değerlendirilir.

## Target audience

**Nihai yaş aralığı uygulama sahibi tarafından final içerik kataloğu görülerek seçilmelidir.**

Mevcut ürün:
- genel bilgi/kısa okuma uygulaması,
- çocuklara özel tasarım/karakter/oyun mekaniği içermez,
- hesap veya sosyal kullanıcı etkileşimi içermez.

Çocuk yaş grupları seçilirse Families politikaları ayrıca devreye girebilir. Bu nedenle gerçek hedef kitle kararı verilmeden yaş seçimi otomatik yapılmamalıdır.

## News declaration

BilgiliMedia'nın temel ürün tanımı kısa, kaynaklı bilgi/keşif uygulamasıdır. Gerçek zamanlı gazetecilik/haber yayıncılığı ürünü olarak konumlandırılmıyorsa Play Console'daki news-app sorusu buna göre cevaplanmalıdır. Final içerik stratejisi değişirse yeniden değerlendirilir.

## Data Safety

Çalışma kaynağı: `PLAY_DATA_SAFETY_DRAFT.md`.

Final production AAB taraması sonrası:
- Firebase Installation ID nedeniyle Device or other IDs beyanı.
- Amaç: app functionality.
- FCM/Firebase Installations dışında ek SDK varsa form tekrar değerlendirilir.
- Analytics/Crashlytics/AdMob eklenmediği doğrulanır.

## Content rating

IARC formu yalnız final içerik kataloğuna göre doldurulur.

Ön kontrolde incelenecek alanlar:
- şiddet/grafik içerik,
- korku,
- cinsellik,
- kumar,
- uyuşturucu/alkol,
- kullanıcı etkileşimi,
- konum paylaşımı,
- satın alma.

BilgiliMedia içerik editoryal standardı, mağaza derecelendirmesiyle çelişen içerik yayımlanmamasını sağlamalıdır.

## Testing / production access

Kişisel hesap 13 Kasım 2023 sonrası açılmışsa:
- en az 12 tester,
- 14 gün kesintisiz opted-in closed test,
- ardından production access başvurusu.

Ayrıntılı plan: `PLAY_CLOSED_TEST_PLAN.md`.

## Kimlik doğrulaması

Kimlik belgesi gelene kadar yayın/production access adımı beklemede tutulur. Kimlik ve Google Payments profilindeki yasal ad/adres bilgileri birbiriyle tutarlı olmalıdır.
