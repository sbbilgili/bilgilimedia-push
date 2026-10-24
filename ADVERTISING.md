# BilgiliMedia Sponsorlu Reklam Altyapısı

Uygulamada doğrudan şirket sponsorlukları için sponsor kartı altyapısı vardır; ancak Google Play'e çıkacak mevcut release hattında bu özellik **iki ayrı kapıyla kilitlidir**.

## Mevcut release durumu

1. `hosting/app-config.json` içinde `sponsorAds.enabled=false`.
2. Android production kaynakta `SPONSOR_FEATURE_ENABLED=false`.

Bu nedenle yalnız uzaktan config değiştirilerek reklam açılamaz. Mevcut release build kullanıcıya sponsor kartı göstermez.

## Gelecekte sponsor açma prosedürü

Sponsorlu içerik ancak aşağıdaki değişiklikler aynı release sürecinde yapıldıktan sonra açılabilir:

- Google Play Console Ads beyanı güncellenir.
- Gizlilik politikası ve Data Safety yeniden değerlendirilir/güncellenir.
- Uygulama içindeki sponsor kartı açıkça **SPONSORLU** etiketi taşır.
- Hedef URL HTTPS olarak doğrulanır.
- `SPONSOR_FEATURE_ENABLED` bilinçli kod değişikliğiyle açılır ve PR/CI'dan geçirilir.
- Ardından `sponsorAds.enabled` ve onaylı sponsor item'ları uzaktan yönetilebilir.

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

## Güvenlik ve politika kapıları

- Reklam hedef URL'si yalnız HTTPS olabilir.
- Remote config tek başına reklamı production release'te açamaz.
- Repository validator, mevcut release'te `sponsorAds.enabled=true` değerini reddeder.
- AdMob veya başka üçüncü taraf reklam SDK'sı entegre değildir.
- İleride reklam SDK'sı eklenirse SDK/veri taraması, gizlilik politikası ve Google Play Data Safety yeniden yapılmalıdır.
