package com.zetaplugins.lifestealz.util;

import com.zetaplugins.lifestealz.LifeStealZ;
import com.zetaplugins.lifestealz.config.BypassPermissionConfig;
import com.zetaplugins.lifestealz.platform.Player;

public final class BypassManager {
    private final LifeStealZ plugin;

    public BypassManager(LifeStealZ plugin) {
        this.plugin = plugin;
    }

    public BypassPermissionConfig getConfig() {
        return plugin.getConfig().getBypassPermission();
    }

    public boolean isEnabled() {
        return getConfig().isEnabled();
    }

    public boolean hasBypass(Player player) {
        if (!isEnabled() || player == null) return false;
        return player.hasPermission("lifestealz.bypass");
    }
}
