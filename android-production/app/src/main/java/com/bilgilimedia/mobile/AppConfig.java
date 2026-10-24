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

    private static final int SUPPORTED_NAV_VERSION = 1;
    private static final int MAX_NAV_DEPTH = 4;
    private static final int MAX_NAV_ITEMS = 100;
    private static final int MAX_SECTIONS = 100;
    private static final int MAX_CATEGORIES = 100;
    private static final int MAX_EXTRA_PAGES = 50;
    private static final int MAX_SPONSORS = 50;

    private static final Pattern STABLE_ID_PATTERN =
            Pattern.compile("[a-z0-9][a-z0-9_-]{0,63}");
    private static final Pattern TOPIC_PATTERN =
            Pattern.compile("[A-Za-z0-9\\-_.~%]{1,900}");
    private static final Set<String> NAV_TYPES =
            new HashSet<>(Arrays.asList("group", "category", "screen", "page"));
    private static final Set<String> ALLOWED_SCREENS = new HashSet<>(Arrays.asList(
            "home", "saved", "history", "accounts", "notifications", "about"));

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

        config.schemaVersion = root.optInt("schemaVersion", 1);
        if (config.schemaVersion < 1) {
            throw new IllegalArgumentException("Invalid config schemaVersion");
        }
        config.updatedAt = root.optString("updatedAt", "").trim();

        JSONObject compatibility = root.optJSONObject("compatibility");
        if (compatibility != null) {
            int minimumClient = compatibility.optInt("minimumClientSchemaVersion", 1);
            if (minimumClient < 1 || minimumClient > CLIENT_SCHEMA_VERSION) {
                throw new IllegalArgumentException(
                        "Config requires unsupported client schema: " + minimumClient);
            }
            int navigationSchema = compatibility.optInt(
                    "navigationSchemaVersion", SUPPORTED_NAV_VERSION);
            if (config.schemaVersion >= 2 && navigationSchema != SUPPORTED_NAV_VERSION) {
                throw new IllegalArgumentException(
                        "Unsupported navigation schema: " + navigationSchema);
            }
        }

        parseSections(root.optJSONArray("sections"), config);
        parseCategories(root.optJSONArray("categories"), config);
        parseExtraPages(root.optJSONArray("extraPages"), config);
        parseNavigation(root.optJSONObject("navigation"), config);
        parseSponsors(root.optJSONObject("sponsorAds"), config);

        if (config.categories.isEmpty()) {
            throw new IllegalArgumentException("Config must contain enabled categories");
        }
        if (config.schemaVersion >= 2 && config.sections.isEmpty()) {
            throw new IllegalArgumentException("Schema v2 config must contain enabled sections");
        }
        if (config.schemaVersion >= 2 && config.navigation.items.isEmpty()) {
            throw new IllegalArgumentException("Schema v2 config must contain navigation");
        }

        config.sections.sort(Comparator.comparingInt(item -> item.order));
        config.categories.sort(Comparator.comparingInt(item -> item.order));
        config.extraPages.sort(Comparator.comparing(item -> item.title));
        config.navigation.items.sort(Comparator.comparingInt(item -> item.order));
        return config;
    }

    private static void parseSections(JSONArray array, AppConfig config) {
        if (array == null) return;
        if (array.length() > MAX_SECTIONS) {
            throw new IllegalArgumentException("Too many sections");
        }

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) throw new IllegalArgumentException("Invalid section");
            if (!object.optBoolean("enabled", true)) continue;

            Models.Section section = new Models.Section();
            section.id = stableId(object.optString("id", ""), "section id");
            section.title = object.optString("title", "").trim();
            section.order = nonNegativeOrder(object.optInt("order", i * 10), "section");

            if (section.title.isEmpty()) {
                throw new IllegalArgumentException("Enabled section is missing title");
            }
            if (!ids.add(section.id)) {
                throw new IllegalArgumentException("Duplicate section id: " + section.id);
            }
            config.sections.add(section);
        }
    }

    private static void parseCategories(JSONArray array, AppConfig config) {
        if (array == null) return;
        if (array.length() > MAX_CATEGORIES) {
            throw new IllegalArgumentException("Too many categories");
        }

        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        Set<String> topics = new HashSet<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) throw new IllegalArgumentException("Invalid category");
            if (!object.optBoolean("enabled", true)) continue;

            Models.Category category = new Models.Category();
            category.name = object.optString("name", "").trim();
            if (category.name.isEmpty()) {
                throw new IllegalArgumentException("Enabled category is missing name");
            }

            String rawId = object.optString("id", "").trim();
            if (rawId.isEmpty()) rawId = slug(category.name);
            category.id = stableId(rawId, "category id");

            String rawSectionId = object.optString("sectionId", "").trim();
            if (config.schemaVersion >= 2) {
                category.sectionId = stableId(rawSectionId, "category sectionId");
                if (findSection(config, category.sectionId) == null) {
                    throw new IllegalArgumentException(
                            "Unknown section for category: " + category.id);
                }
            } else {
                category.sectionId = rawSectionId.isEmpty()
                        ? ""
                        : stableId(rawSectionId, "category sectionId");
            }

            category.order = nonNegativeOrder(
                    object.optInt("order", i * 10), "category");

            String topic = object.optString("topic", "").trim();
            if (topic.isEmpty()) topic = slug(category.name);
            if (!TOPIC_PATTERN.matcher(topic).matches()) {
                throw new IllegalArgumentException("Invalid FCM topic: " + topic);
            }
            category.topic = topic;

            if (!ids.add(category.id)) {
                throw new IllegalArgumentException("Duplicate category id: " + category.id);
            }
            if (!names.add(category.name)) {
                throw new IllegalArgumentException("Duplicate category name: " + category.name);
            }
            if (!topics.add(category.topic)) {
                throw new IllegalArgumentException("Duplicate FCM topic: " + category.topic);
            }
            config.categories.add(category);
        }
    }

    private static void parseExtraPages(JSONArray array, AppConfig config) {
        if (array == null) return;
        if (array.length() > MAX_EXTRA_PAGES) {
            throw new IllegalArgumentException("Too many extra pages");
        }

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) throw new IllegalArgumentException("Invalid extra page");
            if (!object.optBoolean("enabled", true)) continue;

            Models.ExtraPage page = new Models.ExtraPage();
            page.id = stableId(
                    object.optString("id", "page-" + i).trim(), "extra page id");
            page.title = object.optString("title", "BilgiliMedia").trim();
            page.subtitle = object.optString("subtitle", "").trim();
            page.url = object.optString("url", "").trim();

            if (page.title.isEmpty()) {
                throw new IllegalArgumentException("Enabled extra page is missing title");
            }
            if (!isTrustedBilgiliUrl(page.url)) {
                throw new IllegalArgumentException("Untrusted extra page URL");
            }
            if (!ids.add(page.id)) {
                throw new IllegalArgumentException("Duplicate extra page id: " + page.id);
            }
            config.extraPages.add(page);
        }
    }

    private static void parseNavigation(JSONObject object, AppConfig config) {
        if (object == null) {
            if (config.schemaVersion >= 2) {
                throw new IllegalArgumentException("Schema v2 navigation is missing");
            }
            return;
        }

        int version = object.optInt("version", 0);
        if (version != SUPPORTED_NAV_VERSION) {
            throw new IllegalArgumentException("Unsupported navigation version: " + version);
        }
        config.navigation.version = version;

        int maxDepth = object.optInt("maxDepth", MAX_NAV_DEPTH);
        if (maxDepth < 1 || maxDepth > MAX_NAV_DEPTH) {
            throw new IllegalArgumentException("Invalid navigation maxDepth");
        }
        config.navigation.maxDepth = maxDepth;

        JSONArray items = object.optJSONArray("items");
        if (items == null || items.length() < 1) {
            throw new IllegalArgumentException("Navigation items are missing");
        }

        int[] count = {0};
        Set<String> navIds = new HashSet<>();
        Set<String> categoryRefs = new HashSet<>();
        for (int i = 0; i < items.length(); i++) {
            Models.NavItem item = parseNavItem(
                    items.optJSONObject(i),
                    1,
                    maxDepth,
                    count,
                    navIds,
                    categoryRefs,
                    config);
            if (item != null) config.navigation.items.add(item);
        }

        if (config.schemaVersion >= 2) {
            for (Models.Category category : config.categories) {
                if (!categoryRefs.contains(category.id)) {
                    throw new IllegalArgumentException(
                            "Enabled category missing from navigation: " + category.id);
                }
            }
        }
    }

    private static Models.NavItem parseNavItem(
            JSONObject object,
            int depth,
            int maxDepth,
            int[] count,
            Set<String> navIds,
            Set<String> categoryRefs,
            AppConfig config) {
        if (object == null) throw new IllegalArgumentException("Invalid navigation item");
        if (!object.optBoolean("enabled", true)) return null;
        if (depth > maxDepth) {
            throw new IllegalArgumentException("Navigation exceeds maxDepth");
        }
        if (count[0] >= MAX_NAV_ITEMS) {
            throw new IllegalArgumentException("Too many navigation items");
        }

        String type = object.optString("type", "").trim().toLowerCase(Locale.ROOT);
        if (!NAV_TYPES.contains(type)) {
            throw new IllegalArgumentException("Unsupported navigation type: " + type);
        }

        Models.NavItem item = new Models.NavItem();
        item.id = stableId(object.optString("id", "").trim(), "navigation id");
        item.type = type;
        item.title = object.optString("title", "").trim();
        item.order = nonNegativeOrder(
                object.optInt("order", count[0] * 10), "navigation");

        if (item.title.isEmpty()) {
            throw new IllegalArgumentException("Navigation item is missing title");
        }
        if (!navIds.add(item.id)) {
            throw new IllegalArgumentException("Duplicate navigation id: " + item.id);
        }

        if ("screen".equals(type)) {
            item.screen = object.optString("screen", "").trim();
            if (!ALLOWED_SCREENS.contains(item.screen)) {
                throw new IllegalArgumentException(
                        "Unsupported navigation screen: " + item.screen);
            }
        } else if ("category".equals(type)) {
            item.categoryId = stableId(
                    object.optString("categoryId", "").trim(), "navigation categoryId");
            if (findCategory(config, item.categoryId) == null) {
                throw new IllegalArgumentException(
                        "Unknown navigation category: " + item.categoryId);
            }
            categoryRefs.add(item.categoryId);
        } else if ("page".equals(type)) {
            item.pageId = stableId(
                    object.optString("pageId", "").trim(), "navigation pageId");
            Models.ExtraPage page = findPage(config, item.pageId);
            if (page == null) {
                throw new IllegalArgumentException(
                        "Unknown navigation page: " + item.pageId);
            }
            item.url = page.url;
        }

        count[0]++;

        if ("group".equals(type)) {
            JSONArray children = object.optJSONArray("children");
            if (children == null || children.length() < 1) {
                throw new IllegalArgumentException(
                        "Enabled navigation group must have children: " + item.id);
            }
            for (int i = 0; i < children.length(); i++) {
                Models.NavItem child = parseNavItem(
                        children.optJSONObject(i),
                        depth + 1,
                        maxDepth,
                        count,
                        navIds,
                        categoryRefs,
                        config);
                if (child != null) item.children.add(child);
            }
            item.children.sort(Comparator.comparingInt(child -> child.order));
        }
        return item;
    }

    private static void parseSponsors(JSONObject object, AppConfig config) {
        if (object == null) return;
        config.sponsorEnabled = object.optBoolean("enabled", false);

        JSONArray items = object.optJSONArray("items");
        if (items == null) {
            if (config.sponsorEnabled) {
                throw new IllegalArgumentException("Enabled sponsor system is missing items");
            }
            return;
        }
        if (items.length() > MAX_SPONSORS) {
            throw new IllegalArgumentException("Too many sponsor items");
        }

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject source = items.optJSONObject(i);
            if (source == null) throw new IllegalArgumentException("Invalid sponsor item");
            if (!source.optBoolean("enabled", true)) continue;

            Models.Sponsor sponsor = new Models.Sponsor();
            sponsor.id = stableId(
                    source.optString("id", "sponsor-" + i).trim(), "sponsor id");
            sponsor.advertiser = source.optString("advertiser", "").trim();
            sponsor.headline = source.optString("headline", "").trim();
            sponsor.text = source.optString("text", "").trim();
            sponsor.targetUrl = source.optString("targetUrl", "").trim();
            sponsor.placement = source.optString("placement", "").trim();

            if (sponsor.advertiser.isEmpty() || sponsor.headline.isEmpty()) {
                throw new IllegalArgumentException(
                        "Enabled sponsor is missing advertiser/headline");
            }
            if (!isHttpsUrl(sponsor.targetUrl)) {
                throw new IllegalArgumentException("Invalid sponsor target URL");
            }
            if (!ids.add(sponsor.id)) {
                throw new IllegalArgumentException("Duplicate sponsor id: " + sponsor.id);
            }
            config.sponsors.add(sponsor);
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

    private static String stableId(String value, String label) {
        if (value == null || !STABLE_ID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid " + label + ": " + value);
        }
        return value;
    }

    private static int nonNegativeOrder(int value, String label) {
        if (value < 0) {
            throw new IllegalArgumentException("Invalid " + label + " order");
        }
        return value;
    }

    private static Models.Section findSection(AppConfig config, String sectionId) {
        for (Models.Section section : config.sections) {
            if (sectionId.equals(section.id)) return section;
        }
        return null;
    }

    private static Models.Category findCategory(AppConfig config, String categoryId) {
        for (Models.Category category : config.categories) {
            if (categoryId.equals(category.id)) return category;
        }
        return null;
    }

    private static Models.ExtraPage findPage(AppConfig config, String pageId) {
        if (pageId == null || pageId.isEmpty()) return null;
        for (Models.ExtraPage page : config.extraPages) {
            if (pageId.equals(page.id)) return page;
        }
        return null;
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
            URL url = new URL(value);
            return "https".equalsIgnoreCase(url.getProtocol())
                    && url.getHost() != null
                    && !url.getHost().isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }
}
