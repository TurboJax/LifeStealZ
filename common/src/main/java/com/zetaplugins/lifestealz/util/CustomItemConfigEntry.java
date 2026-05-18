package com.zetaplugins.lifestealz.util;

import java.util.List;
import java.util.Map;

public class CustomItemConfigEntry {
    String name;
    List<String> lore;
    String material;
    boolean enchanted;
    int customModelId;
    String customItemType;
    int customHeartValue;
    int minHearts;
    int maxHearts;
    boolean requirePermission;
    boolean craftable;
    Map<String,CustomItemRecipeEntry> recipes;
    boolean invulnerable;
    boolean despawnable;
    List<String> whitelistedWorlds;
    CustomItemSoundEntry sound;
}