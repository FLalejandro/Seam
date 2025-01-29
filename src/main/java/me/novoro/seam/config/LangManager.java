package me.novoro.seam.config;

import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.api.configuration.VersionedConfig;
import me.novoro.seam.utils.ColorUtil;
import me.novoro.seam.utils.StringUtil;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class LangManager extends VersionedConfig {
    private static final Map<String, String> LANG = new HashMap<>();

    @Override
    protected void reload(Configuration config) {
        super.reload(config);
        LangManager.LANG.clear();
        Configuration langSection = config.getSection("Lang");
        if (langSection == null) return;
        for (String key : langSection.getKeys()) LangManager.LANG.put(key, langSection.getString(key));
    }

    public static @Nullable String getLang(String langKey) {
        String lang = LangManager.LANG.get(langKey);
        if (lang == null || lang.isEmpty() || lang.isBlank()) return null;
        return lang;
    }

    public static void sendLang(Audience audience, String key) {
        LangManager.sendLang(audience, key, null);
    }

    public static void sendLang(Audience audience, String key, @Nullable Map<String, String> replacements) {
        String lang = LangManager.getLang(key);
        if (lang == null) return;
        lang = StringUtil.replaceReplacements(lang, replacements);
        String prefix = LangManager.getLang("Prefix");
        if (prefix != null) lang = prefix + lang;
        audience.sendMessage(ColorUtil.parseColour(lang));
    }

    @Override
    protected double getCurrentConfigVersion() {
        return 1.0;
    }

    @Override
    protected String getConfigFileName() {
        return "lang.yml";
    }
}

