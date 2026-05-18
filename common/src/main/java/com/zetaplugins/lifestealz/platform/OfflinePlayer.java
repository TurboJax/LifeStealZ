package com.zetaplugins.lifestealz.platform;

import java.util.UUID;

public interface OfflinePlayer {
    UUID getUUID();
    String getName();

    public Location getLocation();

    public Player getPlayer();
}