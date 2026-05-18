package com.zetaplugins.lifestealz.util;

import com.zetaplugins.lifestealz.LifeStealZ;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public final class ConfigManager {
    private final LifeStealZ plugin;

    public ConfigManager(LifeStealZ plugin) {
        this.plugin = plugin;
    }

    public Map<String,CustomItemConfigEntry> getCustomItemConfig() {
        File file = new File(plugin.getDataFolder(), "items.yml");

        // Parsing the yaml data
        Yaml yml = new Yaml();
        try (FileInputStream fis = new FileInputStream(file)) {
            return yml.load(fis);
        } catch (FileNotFoundException e) {
            LifeStealZ.LOGGER.error("Could not open {}.", file.getAbsolutePath(), e);
        } catch (IOException e) {
            LifeStealZ.LOGGER.error("Error while reading {}", file.getAbsolutePath(), e);
        }

        return Map.of();
    }
}