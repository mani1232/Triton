package com.rexcantor64.triton.language;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.rexcantor64.triton.Triton;
import com.rexcantor64.triton.api.language.SignLocation;
import com.rexcantor64.triton.api.players.LanguagePlayer;
import com.rexcantor64.triton.config.interfaces.Configuration;
import com.rexcantor64.triton.language.item.LanguageItem;
import com.rexcantor64.triton.language.item.LanguageSign;
import com.rexcantor64.triton.language.item.LanguageText;
import com.rexcantor64.triton.utils.YAMLUtils;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class LanguageManager implements com.rexcantor64.triton.api.language.LanguageManager {

    private List<Language> languages = new ArrayList<>();
    private Language mainLanguage;
    private Multimap<LanguageItem.LanguageItemType, LanguageItem> items = ArrayListMultimap.create();

    public String getText(LanguagePlayer p, String code, Object... args) {
        return getText(p.getLang().getName(), code, args);
    }

    private String getText(String language, String code, Object... args) {
        for (LanguageItem item : items.get(LanguageItem.LanguageItemType.TEXT)) {
            LanguageText text = (LanguageText) item;
            if (!text.getKey().equals(code)) continue;
            String msg = text.getMessage(language);
            if (msg == null) return getTextFromMain(code, args);
            for (int i = 0; i < args.length; i++)
                msg = msg.replace("%" + (i + 1), args[i].toString());
            return ChatColor.translateAlternateColorCodes('&', msg);
        }
        return Triton.get().getMessage("error.message-not-found", "ERROR 404: Message not found: '%1'! Please notify the staff!", code);
    }

    public String getTextFromMain(String code, Object... args) {
        for (LanguageItem item : items.get(LanguageItem.LanguageItemType.TEXT)) {
            LanguageText text = (LanguageText) item;
            if (!text.getKey().equals(code)) continue;
            String msg = text.getMessage(mainLanguage.getName());
            if (msg == null) break;
            for (int i = 0; i < args.length; i++)
                msg = msg.replace("%" + (i + 1), args[i].toString());
            return ChatColor.translateAlternateColorCodes('&', msg);
        }
        return Triton.get().getMessage("error.message-not-found", "ERROR 404: Message not found: '%1'! Please notify the staff!", code);
    }

    public String[] getSign(LanguagePlayer p, SignLocation location) {
        return getSign(p.getLang().getName(), location);
    }

    public String[] getSign(String language, SignLocation location) {
        if (location == null) return null;
        for (LanguageItem item : items.get(LanguageItem.LanguageItemType.SIGN)) {
            LanguageSign sign = (LanguageSign) item;
            if (!sign.hasLocation(location)) continue;
            String[] lines = sign.getLines(language);
            if (lines == null) return getSignFromMain(location);
            return lines;
        }
        return null;
    }

    private String[] getSignFromMain(SignLocation location) {
        if (location == null) return null;
        for (LanguageItem item : items.get(LanguageItem.LanguageItemType.SIGN)) {
            LanguageSign sign = (LanguageSign) item;
            if (!sign.hasLocation(location)) continue;
            String[] lines = sign.getLines(mainLanguage.getName());
            if (lines == null) break;
            return lines;
        }
        return null;
    }

    public Language getLanguageByName(String name, boolean fallback) {
        if (name != null)
            for (Language lang : languages)
                if (lang.getName().equals(name))
                    return lang;
        if (fallback) return mainLanguage;
        return null;
    }

    public Language getLanguageByLocale(String name, boolean fallback) {
        if (name != null)
            for (Language lang : languages)
                if (lang.getMinecraftCodes().contains(name))
                    return lang;
        if (fallback) return mainLanguage;
        return null;
    }

    public List<com.rexcantor64.triton.api.language.Language> getAllLanguages() {
        return new ArrayList<>(languages);
    }

    public List<LanguageItem> getAllItems(LanguageItem.LanguageItemType type) {
        return new ArrayList<>(items.get(type));
    }

    public Language getMainLanguage() {
        return mainLanguage;
    }

    public void setup() {
        languages.clear();
        mainLanguage = null;
        items.clear();
        Triton.get().logDebug("Setting up language manager...");
        Configuration languages = Triton.get().getConf().getLanguages();
        if (languages != null) {
            for (String lang : languages.getKeys())
                this.languages.add(mainLanguage = new Language(lang, languages.getString(lang + ".flag", "pa"), YAMLUtils.getStringOrStringList(languages, lang + ".minecraft-code"), languages.getString(lang + ".display-name", "&4Unknown"), languages.getStringList(lang + ".commands")));
            this.mainLanguage = getLanguageByName(Triton.get().getConf().getMainLanguage(), true);
        } else {
            this.mainLanguage = new Language("temp", "pabk", new ArrayList<>(), "Error", new ArrayList<>());
            this.languages.add(this.mainLanguage);
        }
        for (LanguageItem item : Triton.get().getLanguageConfig().getItems())
            items.put(item.getType(), item);
        Triton.get().logDebug("Successfully setup the language manager! %1 languages and %2 language items loaded!", this.languages.size(), this.items.size());
    }

}
