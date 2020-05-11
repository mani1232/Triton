package com.rexcantor64.triton.language.item.serializers;

import com.google.gson.*;
import com.rexcantor64.triton.language.item.LanguageItem;
import com.rexcantor64.triton.language.item.LanguageSign;
import com.rexcantor64.triton.language.item.LanguageText;
import com.rexcantor64.triton.language.item.TWINData;
import lombok.val;

import java.lang.reflect.Type;

public class LanguageItemSerializer implements JsonDeserializer<LanguageItem> {

    static void deserialize(JsonObject json, LanguageItem item, JsonDeserializationContext context) throws JsonParseException {
        val key = json.get("key");
        if (key == null || !key.isJsonPrimitive()) throw new JsonParseException("Translation requires a key");
        item.setKey(key.getAsString());
    }

    static void serialize(LanguageItem item, JsonObject json, JsonSerializationContext context) {
        json.addProperty("key", item.getKey());

        if (item.getTwinData() != null)
            json.add("_twin", context.serialize(item.getTwinData(), TWINData.class));
    }

    @Override
    public LanguageItem deserialize(JsonElement json, Type t, JsonDeserializationContext context) throws JsonParseException {
        val obj = json.getAsJsonObject();
        val type = obj.get("type").getAsString();

        if (type.equalsIgnoreCase("text"))
            return context.deserialize(json, LanguageText.class);
        if (type.equalsIgnoreCase("sign"))
            return context.deserialize(json, LanguageSign.class);

        throw new JsonParseException("Invalid translation type: " + type);
    }

}
