package com.bilgilimedia.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class AppConfig {
    static final int CLIENT_SCHEMA_VERSION = 2;

    private static final int MAX_NAV_DEPTH = 4;
    private static final int MAX_NAV_ITEMS = 100;
    private static final Pattern TOPIC_PATTERN =
            Pattern.compile("[A-Za-z0-9\\-_.~%]{1,900}");
    private static final Set<String> NAV_TYPES =
            new HashSet<>(Arrays.asList("group", "category", "screen", "page"));
    private static final Set<String> ALLOWED_SCREENS = new HashSet<>(Arrays.asList(
            "home", "search", "saved", "accounts", "more", "history",
            "notifications", "settings", "privacy", "about"));

    int schemaVersion = 1;
    String updatedAt = "";
    final List<Models.Section> sections = new ArrayList<>();
    final List<Models.Category> categories = new ArrayList<>();
    final List<Models.ExtraPage> extraPages = new ArrayList<>();
    final List<Models.Sponsor> sponsors = new ArrayList<>();
    final Models.Navigation navigation = new Models.Navigation();
    boolean sponsorEnabled = false;

    static AppConfig parse(String raw) throws Exception {
        JSONObject root = new JSONObject(raw);
        AppConfig config = new AppConfig();

        config.schemaVersion = Math.max(1, root.optInt("schemaVersion", 1));
        config.updatedAt = root.optString("updatedAt", "");

        JSONObject compatibility = root.optJSONObject("compatibility");
        if (compatibility != null) {
            int minimumClient = Math.max(
                    1, compatibility.optInt("minimumClientSchemaVersion", 1));
            if (minimumClient > CLIENT_SCHEMA_VERSION) {
                throw new IllegalArgumentException(
                        "Config requires newer client schema: " + minimumClient);
            }
        }

        parseSections(root.optJSONArray("sections"), config);
        parseCategories(root.optJSONArray("categories"), config);
        parseExtraPages(root.optJSONArray("extraPages"), config);
        parseNavigation(root.optJSONObject("navigation"), config);
        parseSponsors(root.optJSONObject("sponsorAds"), config);

        config.sections.sort(Comparator.comparingInt(item -> item.order));
        config.categories.sort(Comparator.comparingInt(item -> item.order));
        config.extraPages.sort(Comparator.comparing(item -> item.title));
        config.navigation.items.sort(Comparator.comparingInt(item -> item.order));
        return config;
    }

    private static void parseSections(JSONArray array, AppConfig config) {
        if (array == null) return;
        int limit = Math.min(array.length(), 100);
        for (int i = 0; i < limit; i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null || !object.optBoolean("enabled", true)) continue;

            Models.Section section = new Models.Section();
            section.id = safeId(object.optString("id", ""));
            section.title = object.optString("title", "").trim();
            section.order = object.optInt("order", i * 10);
            if (!section.id.isEmpty() && !section.title.isEmpty()) {
                config.sections.add(section);
            }
        }
    }

    private static void parseCategories(JSONArray array, AppConfig config) {
        if (array == null) return;
        int limit = Math.min(array.length(), 100);
        for (int i = 0; i < limit; i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null || !object.optBoolean("enabled", true)) continue;

            Models.Category category = new Models.Category();
            category.name = object.optString("name", "").trim();
            category.id = safeId(object.optString("id", slug(category.name)));
            category.sectionId = safeId(object.optString("sectionId", ""));
            category.order = object.optInt("order", i * 10);

            String topic = object.optString("topic", slug(category.name)).trim();
            if (!TOPIC_PATTERN.matcher(topic).matches()) {
                topic = slug(category.name);
            }
            category.topic = topic;

            if (!category.id.isEmpty()
                    && !category.name.isEmpty()
                    && TOPIC_PATTERN.matcher(category.topic).matches()) {
                config.categories.add(category);
            }
        }
    }

    private static void parseExtraPages(JSONArray array, AppConfig config) {
        if (array == null) return;
        int limit = Math.min(array.length(), 50);
        for (int i = 0; i < limit; i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null || !object.optBoolean("enabled", true)) continue;

            Models.ExtraPage page = new Models.ExtraPage();
            page.id = safeId(object.optString("id", "page-" + i));
            page.title = object.optString("title", "BilgiliMedia").trim();
            page.subtitle = object.optString("subtitle", "").trim();
            page.url = object.optString("url", "").trim();

            if (!page.id.isEmpty()
                    && !page.title.isEmpty()
                    && isTrustedBilgiliUrl(page.url)) {
                config.extraPages.add(page);
            }
        }
    }

    private static void parseNavigation(JSONObject object, AppConfig config) {
        if (object == null) return;

        config.navigation.version = Math.max(1, object.optInt("version", 1));
        config.navigation.maxDepth = Math.max(
                1, Math.min(MAX_NAV_DEPTH, object.optInt("maxDepth", MAX_NAV_DEPTH)));

        JSONArray items = object.optJSONArray("items");
        if (items == null) return;

        int[] count = {0};
        for (int i = 0; i < items.length() && count[0] < MAX_NAV_ITEMS; i++) {
            Models.NavItem item = parseNavItem(
                    items.optJSONObject(i), 1, config.navigation.maxDepth, count);
            if (item != null) config.navigation.items.add(item);
        }
    }

    private static Models.NavItem parseNavItem(
            JSONObject object, int depth, int maxDepth, int[] count) {
        if (object == null
                || depth > maxDepth
                || count[0] >= MAX_NAV_ITEMS
                || !object.optBoolean("enabled", true)) {
            return null;
        }

        String type = object.optString("type", "").trim().toLowerCase(Locale.ROOT);
        if (!NAV_TYPES.contains(type)) return null;

        Models.NavItem item = new Models.NavItem();
        item.id = safeId(object.optString("id", ""));
        item.type = type;
        item.title = object.optString("title", "").trim();
        item.order = object.optInt("order", count[0] * 10);
        if (item.id.isEmpty() || item.title.isEmpty()) return null;

        if ("screen".equals(type)) {
            item.screen = object.optString("screen", "").trim();
            if (!ALLOWED_SCREENS.contains(item.screen)) return null;
        } else if ("category".equals(type)) {
            item.categoryId = safeId(object.optString("categoryId", ""));
            if (item.categoryId.isEmpty()) return null;
        } else if ("page".equals(type)) {
            item.url = object.optString("url", "").trim();
            if (!isTrustedBilgiliUrl(item.url)) return null;
        }

        count[0]++;
        if ("group".equals(type) && depth < maxDepth) {
            JSONArray children = object.optJSONArray("children");
            if (children != null) {
                for (int i = 0;
                     i < children.length() && count[0] < MAX_NAV_ITEMS;
                     i++) {
                    Models.NavItem child = parseNavItem(
                            children.optJSONObject(i), depth + 1, maxDepth, count);
                    if (child != null) item.children.add(child);
                }
                item.children.sort(Comparator.comparingInt(child -> child.order));
            }
        }
        return item;
    }

    private static void parseSponsors(JSONObject object, AppConfig config) {
        if (object == null) return;
        config.sponsorEnabled = object.optBoolean("enabled", false);

        JSONArray items = object.optJSONArray("items");
        if (items == null) return;

        int limit = Math.min(items.length(), 50);
        for (int i = 0; i < limit; i++) {
            JSONObject source = items.optJSONObject(i);
            if (source == null || !source.optBoolean("enabled", true)) continue;

            Models.Sponsor sponsor = new Models.Sponsor();
            sponsor.id = safeId(source.optString("id", "sponsor-" + i));
            sponsor.advertiser = source.optString("advertiser", "").trim();
            sponsor.headline = source.optString("headline", "").trim();
            sponsor.text = source.optString("text", "").trim();
            sponsor.targetUrl = source.optString("targetUrl", "").trim();
            sponsor.placement = source.optString("placement", "").trim();

            if (!sponsor.id.isEmpty() && isHttpsUrl(sponsor.targetUrl)) {
                config.sponsors.add(sponsor);
            }
        }
    }

    Map<String, String> categoryTopicMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Models.Category category : categories) {
            map.put(category.name, category.topic);
        }
        return map;
    }

    static String slug(String input) {
        if (input == null) return "";
        String value = input
                .replace('İ', 'I').replace('ı', 'i')
                .replace('Ğ', 'G').replace('ğ', 'g')
                .replace('Ü', 'U').replace('ü', 'u')
                .replace('Ş', 'S').replace('ş', 's')
                .replace('Ö', 'O').replace('ö', 'o')
                .replace('Ç', 'C').replace('ç', 'c');
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_-]+", "");
    }

    private static String safeId(String value) {
        if (value == null) return "";
        return slug(value);
    }

    private static boolean isTrustedBilgiliUrl(String value) {
        try {
            URL url = new URL(value);
            return "https".equalsIgnoreCase(url.getProtocol())
                    && "bilgilimedia-60d6f.web.app".equalsIgnoreCase(url.getHost());
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isHttpsUrl(String value) {
        try {
            return "https".equalsIgnoreCase(new URL(value).getProtocol());
        } catch (Exception ignored) {
            return false;
        }
    }
}
