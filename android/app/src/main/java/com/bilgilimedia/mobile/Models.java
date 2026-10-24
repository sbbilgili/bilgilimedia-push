package com.bilgilimedia.mobile;

import java.util.ArrayList;
import java.util.List;

final class Models {
    static final class Source {
        String title = "Kaynak";
        String url = "";
    }

    static final class Article {
        String id = "";
        String categoryId = "";
        String category = "";
        String title = "";
        String summary = "";
        String body = "";
        int minutes = 0;
        String imageUrl = "";
        String imageCredit = "";
        String imageSource = "";
        String videoUrl = "";
        String videoCredit = "";
        String videoSource = "";
        final List<Source> sources = new ArrayList<>();
    }

    static final class Category {
        String id = "";
        String name = "";
        String topic = "";
        String sectionId = "";
        int order = 0;
        boolean enabled = true;
    }

    static final class ExtraPage {
        String id = "";
        String title = "";
        String subtitle = "";
        String type = "";
        String url = "";
        boolean enabled = true;
    }

    static final class Sponsor {
        String id = "";
        String advertiser = "";
        String headline = "";
        String text = "";
        String targetUrl = "";
        String placement = "";
        boolean enabled = true;
    }

    static final class Feed {
        int version = 1;
        String featuredId = "";
        final List<Article> articles = new ArrayList<>();
    }

    static final class Config {
        int schemaVersion = 1;
        String updatedAt = "";
        final List<Category> categories = new ArrayList<>();
        final List<ExtraPage> extraPages = new ArrayList<>();
        final List<Sponsor> sponsors = new ArrayList<>();
        boolean sponsorAdsEnabled = false;
    }

    static final class SocialAccount {
        final String name;
        final String url;
        final String platform;
        final String category;

        SocialAccount(String name, String url, String platform, String category) {
            this.name = name;
            this.url = url;
            this.platform = platform;
            this.category = category;
        }
    }
}
