package com.rexcantor64.triton.wrappers.legacy;

import org.bukkit.Material;

public class MaterialWrapper {

    public static Material bannerMaterial = Material.LEGACY_BANNER;

    public static boolean shouldLoad(int mcVersion) {
        return mcVersion <= 12;
    }
}
