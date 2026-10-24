package com.bilgilimedia.mobile;

import java.util.*;

final class SocialData {
    private SocialData(){}

    static List<Models.SocialAccount> all(){
        List<Models.SocialAccount> x = new ArrayList<>();

        x.add(a("Instagram","Genel","bilgilimedia_bayn","https://www.instagram.com/bilgilimedia_bayn/"));
        x.add(a("Instagram","Belgesel","bilgilimedia_belgesel","https://www.instagram.com/bilgilimedia_belgesel/"));
        x.add(a("Instagram","Tarih","bilgilimedia_tarih","https://www.instagram.com/bilgilimedia_tarih/"));
        x.add(a("Instagram","Spor","bilgilimedia_spor","https://www.instagram.com/bilgilimedia_spor/"));
        x.add(a("Instagram","Yemek","bilgilimedia_yemek","https://www.instagram.com/bilgilimedia_yemek/"));
        x.add(a("Instagram","Doğa","bilgilimedia_doga","https://www.instagram.com/bilgilimedia_doga/"));
        x.add(a("Instagram","Animasyon","bilgilimedia_animasyon","https://www.instagram.com/bilgilimedia_animasyon/"));
        x.add(a("Instagram","Çizgi Film","bilgilimedia_cizgifilm","https://www.instagram.com/bilgilimedia_cizgifilm/"));
        x.add(a("Instagram","Yapay Zekâ","bilgilimedia_ai","https://www.instagram.com/bilgilimedia_ai/"));
        x.add(a("Instagram","Müzik","bilgilimedia_muzik","https://www.instagram.com/bilgilimedia_muzik/"));

        x.add(a("YouTube","Genel","@bilgilimedia_bayn","https://www.youtube.com/@bilgilimedia_bayn"));
        x.add(a("YouTube","Belgesel","@bilgilimedia_belgesel","https://www.youtube.com/@bilgilimedia_belgesel"));
        x.add(a("YouTube","Tarih","@bilgilimedia_tarih","https://www.youtube.com/@bilgilimedia_tarih"));
        x.add(a("YouTube","Spor","@bilgilimedia_spor","https://www.youtube.com/@bilgilimedia_spor"));
        x.add(a("YouTube","Yemek","@bilgilimedia_yemek","https://www.youtube.com/@bilgilimedia_yemek"));
        x.add(a("YouTube","Doğa","@bilgilimedia_doga","https://www.youtube.com/@bilgilimedia_doga"));
        x.add(a("YouTube","Animasyon","@bilgilimedia_animasyon","https://www.youtube.com/@bilgilimedia_animasyon"));
        x.add(a("YouTube","Çizgi Film","@bilgilimedia_cizgifilm","https://www.youtube.com/@bilgilimedia_cizgifilm"));

        x.add(a("TikTok","Genel","@bilgilibaynmedia","https://www.tiktok.com/@bilgilibaynmedia"));
        x.add(a("TikTok","Belgesel","@bilgilimedia_belgesel","https://www.tiktok.com/@bilgilimedia_belgesel"));
        x.add(a("TikTok","Tarih","@bilgilimedia_tarih","https://www.tiktok.com/@bilgilimedia_tarih"));
        x.add(a("TikTok","Spor","@bilgilimedia_spor","https://www.tiktok.com/@bilgilimedia_spor"));
        x.add(a("TikTok","Yemek","@bilgilimedia_yemek","https://www.tiktok.com/@bilgilimedia_yemek"));
        x.add(a("TikTok","Doğa","@bilgilimedia_doa","https://www.tiktok.com/@bilgilimedia_doa"));
        x.add(a("TikTok","English","@bilgilimedia_english","https://www.tiktok.com/@bilgilimedia_english"));
        x.add(a("TikTok","Müzik","@bilgilimedia_muzik","https://www.tiktok.com/@bilgilimedia_muzik"));

        x.add(a("Facebook","Genel","bilgilimedia","https://www.facebook.com/bilgilimedia/"));
        x.add(a("Facebook","SB Media","bilgilisbmedia","https://www.facebook.com/bilgilisbmedia"));
        x.add(a("Facebook","Belgesel","Bilgilimedia-Belgesel","https://www.facebook.com/people/Bilgilimedia-Belgesel/61594717468413/"));
        x.add(a("Facebook","Tarih","BilgiliMedia-Tarih","https://www.facebook.com/people/BilgiliMedia-Tarih/61594312784284/"));
        x.add(a("Facebook","Şükrü Batuhan Bilgili","Sukru-Batuhan-Bilgili","https://www.facebook.com/people/Sukru-Batuhan-Bilgili/61594884595578/"));
        x.add(a("Facebook","Yemek","BilgiliMedia-Yemek","https://www.facebook.com/people/BilgiliMedia-Yemek/61594650695544/"));
        x.add(a("Facebook","Doğa","BilgiliMedia-Doğa","https://www.facebook.com/people/BilgiliMedia-Doğa/61594535117641/"));
        return x;
    }

    private static Models.SocialAccount a(String platform,String category,String handle,String url){
        return new Models.SocialAccount(platform,category,handle,url);
    }
}
