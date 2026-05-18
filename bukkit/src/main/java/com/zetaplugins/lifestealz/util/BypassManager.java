package com.zetaplugins.lifestealz.util;

import com.zetaplugins.lifestealz.config.BypassPermissionConfig;
import org.bukkit.entity.Player;
import com.zetaplugins.lifestealz.LifeStealZ;

public final class BypassManager {
    private final LifeStealZ plugin;

    public BypassManager(LifeStealZ plugin) {
        this.plugin = plugin;
    }

    public BypassPermissionConfig getConfig() {
        return new BypassConfig(plugin);
    }

    public boolean isEnabled() {
        return getConfig().isEnabled();
    }

    public boolean hasBypass(Player player) {
        if (!isEnabled() || player == null) return false;
        return player.hasPermission("lifestealz.bypass");
    }

    public static class BypassConfig {
        private final LifeStealZ plugin;

        public BypassConfig(LifeStealZ plugin) {
            this.plugin = plugin;
        }

        public boolean isEnabled() {
            return plugin.getConfig().getBoolean("bypassPermission.enabled", false);
        }

        public boolean damageFromPlayers() {
            return plugin.getConfig().getBoolean("bypassPermission.damageFromPlayers", false);
        }

        public boolean damageToPlayers() {
            return plugin.getConfig().getBoolean("bypassPermission.damageToPlayers", false);
        }

        public boolean useHearts() {
            return plugin.getConfig().getBoolean("bypassPermission.useHearts", false);
        }

        public boolean looseHearts() {
            return plugin.getConfig().getBoolean("bypassPermission.looseHearts", false);
        }

        public boolean gainHearts() {
            return plugin.getConfig().getBoolean("bypassPermission.gainHearts", false);
        }
    }
}
