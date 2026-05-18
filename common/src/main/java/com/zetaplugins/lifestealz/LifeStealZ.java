package com.zetaplugins.lifestealz;


import com.zetaplugins.lifestealz.caches.EliminatedPlayersCache;
import com.zetaplugins.lifestealz.caches.OfflinePlayerCache;
import com.zetaplugins.lifestealz.config.MainConfig;
import com.zetaplugins.lifestealz.platform.Player;
import com.zetaplugins.lifestealz.platform.Server;
import com.zetaplugins.lifestealz.storage.MariaDBStorage;
import com.zetaplugins.lifestealz.storage.MySQLStorage;
import com.zetaplugins.lifestealz.storage.SQLiteStorage;
import com.zetaplugins.lifestealz.storage.Storage;
import com.zetaplugins.lifestealz.util.AsyncTaskManager;
import com.zetaplugins.lifestealz.util.BypassManager;
import com.zetaplugins.lifestealz.util.ConfigManager;
import com.zetaplugins.lifestealz.util.GracePeriodManager;
import com.zetaplugins.lifestealz.util.LanguageManager;
import com.zetaplugins.lifestealz.util.PapiExpansion;
import com.zetaplugins.lifestealz.util.VersionChecker;
import com.zetaplugins.lifestealz.util.WebHookManager;
import com.zetaplugins.lifestealz.util.customblocks.ReviveBeaconEffectManager;
import com.zetaplugins.lifestealz.util.customitems.recipe.RecipeManager;
import com.zetaplugins.lifestealz.util.geysermc.GeyserManager;
import com.zetaplugins.lifestealz.util.geysermc.GeyserPlayerFile;
import com.zetaplugins.lifestealz.util.revive.ReviveTaskManager;
import com.zetaplugins.lifestealz.util.scheduler.BukkitRunnable;
import com.zetaplugins.lifestealz.util.scheduler.BukkitScheduler;
import com.zetaplugins.lifestealz.util.worldguard.WorldGuardManager;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class LifeStealZ {
    public static final Logger LOGGER = LoggerFactory.getLogger("LifeStealZ");
    private static final String PACKAGE_PREFIX = "com.zetaplugins.lifestealz";
    @Getter private static LifeStealZ instance;

    @Getter private final Server server;
    @Getter private final File dataFolder;
    private final BukkitScheduler scheduler;
    @Getter private MainConfig config;

    private VersionChecker versionChecker;
    private Storage storage;
    private WorldGuardManager worldGuardManager;
    private LanguageManager languageManager;
    private ConfigManager configManager;
    private RecipeManager recipeManager;
    private GeyserManager geyserManager;
    private GeyserPlayerFile geyserPlayerFile;
    private WebHookManager webHookManager;
    private GracePeriodManager gracePeriodManager;
    private BypassManager bypassManager;
    private EliminatedPlayersCache eliminatedPlayersCache;
    private OfflinePlayerCache offlinePlayerCache;
    private AsyncTaskManager asyncTaskManager;
    private ReviveBeaconEffectManager reviveBeaconEffectManager;
    private ReviveTaskManager reviveTaskManager;
    private final boolean hasWorldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    private final boolean hasPlaceholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    private final boolean hasGeyser = Bukkit.getPluginManager().getPlugin("floodgate") != null;

    public LifeStealZ(Server server, File dataFolder, BukkitScheduler scheduler) {
        if (instance != null) {
            throw new IllegalStateException("Cannot make multiple instances of LifeStealZ");
        }

        instance = this;

        this.server = server;
        this.dataFolder = dataFolder;
        this.scheduler = scheduler;

        reloadConfig();
    }

    public void reloadConfig() {
        File file = new File(dataFolder, "config.yml");

        // Loading the default config.yml if it doesn't exist
        if (!file.exists()) {
            dataFolder.mkdirs();

            try {
                InputStream is = ClassLoader.getSystemResourceAsStream("config.yml");
                FileOutputStream os = new FileOutputStream(file);

                assert is != null;

                int b = is.read();
                while (b != -1) {
                    os.write(b);
                    b = is.read();
                }

                is.close();
                os.close();
            } catch (FileNotFoundException e) {
                LOGGER.error("Could not open {}.", file.getAbsolutePath(), e);
            } catch (IOException e) {
                LOGGER.error("Error while writing to {}", file.getAbsolutePath(), e);
            }
        }

        // Parsing the yaml data
        Yaml yml = new Yaml();
        try (FileInputStream fis = new FileInputStream(file)) {
            config = yml.load(fis);
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not open {}.", file.getAbsolutePath(), e);
        } catch (IOException e) {
            LOGGER.error("Error while reading {}", file.getAbsolutePath(), e);
        }
    }

    public static BukkitScheduler getScheduler() {
        return getInstance().scheduler;
    }

    @Override
    public void onLoad() {
        LOGGER.info("Loading LifeStealZ...");

        if (hasWorldGuard()) {
            LOGGER.info("WorldGuard found! Enabling WorldGuard support...");
            worldGuardManager = new WorldGuardManager();
            LOGGER.info("WorldGuard found! Enabled WorldGuard support!");
        }
    }

    @Override
    public void onEnable() {
        if (hasGeyser()) {
            LOGGER.info("Geyser found, enabling Bedrock player support.");
            geyserPlayerFile = new GeyserPlayerFile();
            geyserManager = new GeyserManager();
        }

        getConfig().options().copyDefaults(true);

        saveDefaultConfig();

        asyncTaskManager = new AsyncTaskManager();
        reviveBeaconEffectManager = new ReviveBeaconEffectManager(this);
        reviveTaskManager = new ReviveTaskManager();

        languageManager = new LanguageManager(this);
        configManager = new ConfigManager(this);

        storage = createPlayerDataStorage();
        storage.init();

        recipeManager = new RecipeManager(this);
        recipeManager.registerRecipes();

        versionChecker = new VersionChecker(this, "l8Uv7FzS");
        gracePeriodManager = new GracePeriodManager(this);
        bypassManager = new BypassManager(this);
        webHookManager = new WebHookManager(this);

        eliminatedPlayersCache = new EliminatedPlayersCache(this);
        offlinePlayerCache = new OfflinePlayerCache(this);

        List<String> registeredCommands = new AutoCommandRegistrar(this, PACKAGE_PREFIX).registerAllCommands();
        LOGGER.info("Registered {} commands", registeredCommands.size());

        List<String> registeredEvents = new AutoEventRegistrar(this, PACKAGE_PREFIX).registerAllListeners();
        LOGGER.info("Registered {} event listeners", registeredEvents.size());

        initializeBStats();

        if (hasPlaceholderApi()) {
            PapiExpansion papiExpansion = new PapiExpansion(this);
            if (papiExpansion.canRegister()) {
                papiExpansion.register();
                LOGGER.info("PlaceholderAPI found! Enabled PlaceholderAPI support!");
            }
        }

        LOGGER.info("LifeStealZ enabled!");
    }

    @Override
    public void onDisable() {
        LOGGER.info("Canceling all running tasks...");
        asyncTaskManager.cancelAllTasks();
        reviveBeaconEffectManager.clearAllEffects();
        LOGGER.info("LifeStealZ disabled!");
    }

    public static LifeStealZAPI getAPI() {
        return new LifeStealZAPIImpl(getInstance());
    }

    public AsyncTaskManager getAsyncTaskManager() {
        return asyncTaskManager;
    }

    public ReviveBeaconEffectManager getReviveBeaconEffectManager() {
        return reviveBeaconEffectManager;
    }

    public ReviveTaskManager getReviveTaskManager() {
        return reviveTaskManager;
    }

    public VersionChecker getVersionChecker() {
        return versionChecker;
    }

    public Storage getStorage() {
        return storage;
    }

    public EliminatedPlayersCache getEliminatedPlayersCache() {
        return eliminatedPlayersCache;
    }

    public OfflinePlayerCache getOfflinePlayerCache() {
        return offlinePlayerCache;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public GracePeriodManager getGracePeriodManager() {
        return gracePeriodManager;
    }

    public BypassManager getBypassManager() {
        return bypassManager;
    }

    public GeyserManager getGeyserManager() {
        return geyserManager;
    }

    public GeyserPlayerFile getGeyserPlayerFile() {
        return geyserPlayerFile;
    }

    public boolean hasWorldGuard() {
        return hasWorldGuard;
    }

    public boolean hasPlaceholderApi() {
        return hasPlaceholderApi;
    }

    public boolean hasGeyser() {
        return hasGeyser;
    }

    public WebHookManager getWebHookManager() {
        return webHookManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    private Storage createPlayerDataStorage() {
        switch (getConfigManager().getStorageConfig().getString("type").toLowerCase()) {
            case "mysql":
                LOGGER.info("Using MySQL storage");
                return new MySQLStorage(this);
            case "sqlite":
                LOGGER.info("Using SQLite storage");
                return new SQLiteStorage(this);
            case "mariadb":
                LOGGER.info("Using MariaDB storage");
                return new MariaDBStorage(this);
            default:
                LOGGER.warn("Invalid storage type in config.yml! Using SQLite storage as fallback.");
                return new SQLiteStorage(this);
        }
    }

    public static void setMaxHealth(Player player, double maxHealth) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(maxHealth);
        }
    }

    private void initializeBStats() {
        int pluginId = 18735;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(new Metrics.SimplePie("storage_type", () -> getConfigManager().getStorageConfig().getString("type")));
        metrics.addCustomChart(new Metrics.SimplePie("language", () -> getConfig().getString("lang")));
    }
}