package com.bilgilimedia.mobile;

import java.util.*;

final class SocialData {
    private SocialData(){}
    static List<Models.SocialAccount> all(){
        List<Models.SocialAccount>x=new ArrayList<>();
        x.add(a("Instagram","BilgiliMedia AI","@bilgilimedia_ai","https://www.instagram.com/bilgilimedia_ai/"));
        x.add(a("Instagram","BilgiliMedia Animasyon","@bilgilimedia_animasyon","https://www.instagram.com/bilgilimedia_animasyon/"));
        x.add(a("Instagram","BilgiliMedia","@bilgilimedia_bayn","https://www.instagram.com/bilgilimedia_bayn/"));
        x.add(a("Instagram","BilgiliMedia Belgesel","@bilgilimedia_belgesel","https://www.instagram.com/bilgilimedia_belgesel/"));
        x.add(a("Instagram","BilgiliMedia Çizgi Film","@bilgilimedia_cizgifilm","https://www.instagram.com/bilgilimedia_cizgifilm/"));
        x.add(a("Instagram","BilgiliMedia Doğa","@bilgilimedia_doga","https://www.instagram.com/bilgilimedia_doga/"));
        x.add(a("Instagram","BilgiliMedia Müzik","@bilgilimedia_muzik","https://www.instagram.com/bilgilimedia_muzik/"));
        x.add(a("Instagram","BilgiliMedia Spor","@bilgilimedia_spor","https://www.instagram.com/bilgilimedia_spor/"));
        x.add(a("Instagram","BilgiliMedia Tarih","@bilgilimedia_tarih","https://www.instagram.com/bilgilimedia_tarih/"));
        x.add(a("Instagram","BilgiliMedia Yemek","@bilgilimedia_yemek","https://www.instagram.com/bilgilimedia_yemek/"));
        x.add(a("TikTok","BilgiliMedia","@bilgilibaynmedia","https://www.tiktok.com/@bilgilibaynmedia"));
        x.add(a("TikTok","BilgiliMedia Belgesel","@bilgilimedia_belgesel","https://www.tiktok.com/@bilgilimedia_belgesel"));
        x.add(a("TikTok","BilgiliMedia Doğa","@bilgilimedia_doa","https://www.tiktok.com/@bilgilimedia_doa"));
        x.add(a("TikTok","BilgiliMedia English","@bilgilimedia_english","https://www.tiktok.com/@bilgilimedia_english"));
        x.add(a("TikTok","BilgiliMedia Müzik","@bilgilimedia_muzik","https://www.tiktok.com/@bilgilimedia_muzik"));
        x.add(a("TikTok","BilgiliMedia Spor","@bilgilimedia_spor","https://www.tiktok.com/@bilgilimedia_spor"));
        x.add(a("TikTok","BilgiliMedia Tarih","@bilgilimedia_tarih","https://www.tiktok.com/@bilgilimedia_tarih"));
        x.add(a("TikTok","BilgiliMedia Yemek","@bilgilimedia_yemek","https://www.tiktok.com/@bilgilimedia_yemek"));
        x.add(a("YouTube","BilgiliMedia Animasyon","@bilgilimedia_animasyon","https://www.youtube.com/@bilgilimedia_animasyon"));
        x.add(a("YouTube","BilgiliMedia","@bilgilimedia_bayn","https://www.youtube.com/@bilgilimedia_bayn"));
        x.add(a("YouTube","BilgiliMedia Belgesel","@bilgilimedia_belgesel","https://www.youtube.com/@bilgilimedia_belgesel"));
        x.add(a("YouTube","BilgiliMedia Çizgi Film","@bilgilimedia_cizgifilm","https://www.youtube.com/@bilgilimedia_cizgifilm"));
        x.add(a("YouTube","BilgiliMedia Doğa","@bilgilimedia_doga","https://www.youtube.com/@bilgilimedia_doga"));
        x.add(a("YouTube","BilgiliMedia Spor","@bilgilimedia_spor","https://www.youtube.com/@bilgilimedia_spor"));
        x.add(a("YouTube","BilgiliMedia Tarih","@bilgilimedia_tarih","https://www.youtube.com/@bilgilimedia_tarih"));
        x.add(a("YouTube","BilgiliMedia Yemek","@bilgilimedia_yemek","https://www.youtube.com/@bilgilimedia_yemek"));
        x.add(a("Facebook","BilgiliMedia","BilgiliMedia","https://www.facebook.com/bilgilimedia/"));
        x.add(a("Facebook","Bilgili SB Media","BilgiliSBMedia","https://www.facebook.com/bilgilisbmedia"));
        x.add(a("Facebook","BilgiliMedia Doğa","BilgiliMedia Doğa","https://www.facebook.com/people/BilgiliMedia-Doğa/61594535117641/"));
        x.add(a("Facebook","BilgiliMedia Tarih","BilgiliMedia Tarih","https://www.facebook.com/people/BilgiliMedia-Tarih/61594312784284/"));
        x.add(a("Facebook","BilgiliMedia Yemek","BilgiliMedia Yemek","https://www.facebook.com/people/BilgiliMedia-Yemek/61594650695544/"));
        x.add(a("Facebook","BilgiliMedia Belgesel","BilgiliMedia Belgesel","https://www.facebook.com/people/Bilgilimedia-Belgesel/61594717468413/"));
        x.add(a("Facebook","Şükrü Batuhan Bilgili","Şükrü Batuhan Bilgili","https://www.facebook.com/people/Sukru-Batuhan-Bilgili/61594884595578/"));
        return x;
    }
    private static Models.SocialAccount a(String p,String n,String h,String u){return new Models.SocialAccount(p,n,h,u);}
}
