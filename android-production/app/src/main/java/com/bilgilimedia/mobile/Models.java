package com.bilgilimedia.mobile;

import java.util.ArrayList;
import java.util.List;

final class Models {
    static final class Source { String title = "Kaynak"; String url = ""; }
    static final class Article {
        String id=""; String categoryId=""; String category="Genel"; String title=""; String summary=""; String body="";
        int minutes=0; String imageUrl=""; String imageCredit=""; String imageSource=""; String videoUrl=""; String videoCredit=""; String videoSource="";
        final List<Source> sources=new ArrayList<>();
        boolean hasVideo(){return videoUrl!=null&&!videoUrl.isEmpty();}
    }
    static final class Category { String id=""; String name=""; String topic=""; String sectionId=""; int order=0; boolean enabled=true; }
    static final class Section { String id=""; String title=""; int order=0; boolean enabled=true; }
    static final class ExtraPage { String id=""; String title=""; String subtitle=""; String url=""; boolean enabled=true; }
    static final class Sponsor { String id=""; String advertiser=""; String headline=""; String text=""; String targetUrl=""; String placement=""; boolean enabled=true; }
    static final class SocialAccount {
        final String platform,name,handle,url;
        SocialAccount(String platform,String name,String handle,String url){this.platform=platform;this.name=name;this.handle=handle;this.url=url;}
    }
}
