# BilgiliMedia Sponsorlu Reklam Altyapısı

Uygulama doğrudan şirket sponsorlukları için uzaktan yönetilebilir bir sponsor kartı altyapısına sahiptir.

## Mevcut durum

`hosting/app-config.json` içinde:

```json
"sponsorAds": {
  "enabled": false,
  "items": []
}
```

olduğu için kullanıcılar şu anda hiçbir sponsorlu reklam görmez.

## Örnek sponsor kartı

```json
{
  "id": "ornek-kampanya-2026",
  "enabled": true,
  "placement": "home_after_featured",
  "advertiser": "Örnek Marka",
  "headline": "Kampanya başlığı",
  "text": "Kısa sponsor açıklaması.",
  "targetUrl": "https://example.com/kampanya"
}
```

Tüm reklam kartları uygulamada açıkça **SPONSORLU** etiketiyle gösterilir.

## Güvenlik

- Reklam hedef URL'si yalnız HTTPS olabilir.
- Reklamlar uzaktan kapatılabilir.
- Sponsor sistemi kapalıyken uygulama reklam bileşeni göstermez.
- AdMob veya başka üçüncü taraf reklam SDK'sı bu altyapının parçası değildir; ileride ayrıca eklenirse gizlilik politikası ve Google Play Data Safety beyanı güncellenmelidir.
