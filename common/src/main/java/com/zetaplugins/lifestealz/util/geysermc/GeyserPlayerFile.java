package com.zetaplugins.lifestealz.util.geysermc;

import com.zetaplugins.lifestealz.LifeStealZ;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class GeyserPlayerFile {
    private final LifeStealZ plugin = LifeStealZ.getInstance();
    private final Yaml yml = new Yaml();
    private File file;
    private Map<String,String> config;

    public GeyserPlayerFile() {
        createGeyserPlayerFile();
    }

    private void createGeyserPlayerFile() {
        file = new File(plugin.getDataFolder(), "geyser_players.yml");

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                LifeStealZ.LOGGER.error("Could not create {}.", file.getAbsolutePath());
            }
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            config = yml.load(fis);
        } catch (FileNotFoundException e) {
            LifeStealZ.LOGGER.error("Could not open {}.", file.getAbsolutePath(), e);
        } catch (IOException e) {
            LifeStealZ.LOGGER.error("Error while reading {}", file.getAbsolutePath(), e);
        }
    }

    public void savePlayer(UUID uuid, String name) {
        config.put(uuid.toString(), name);
        saveConfig();
    }

    public String getPlayerName(UUID uuid) {
        return config.get(uuid.toString());
    }

    public UUID getPlayerUUID(String name) {
        for (Entry<String,String> entry : config.entrySet()) {
            if (entry.getValue().equals(name)) {
                return UUID.fromString(entry.getKey());
            }
        }

        return null;
    }

    private void saveConfig() {
        try {
            FileWriter writer = new FileWriter(file);
            yml.dump(config, writer);
            writer.close();
        } catch (IOException e) {
            LifeStealZ.LOGGER.error("Could not write to {}.", file.getName(), e);
        }
    }

    public boolean isPlayerStored(UUID uuid) {
        return config.containsKey(uuid.toString());
    }

    public boolean isPlayerStored(String name) {
        return config.containsValue(name);
    }
}