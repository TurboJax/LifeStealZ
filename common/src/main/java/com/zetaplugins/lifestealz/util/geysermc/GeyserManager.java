package com.zetaplugins.lifestealz.util.geysermc;

import com.zetaplugins.lifestealz.LifeStealZ;
import com.zetaplugins.lifestealz.platform.Player;

import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public class GeyserManager {
    private final FloodgateApi geyserApi = FloodgateApi.getInstance();
    private final GeyserPlayerFile geyserPlayerFile = LifeStealZ.getInstance().getGeyserPlayerFile();

    public boolean isBedrockPlayer(Player player) {
        return geyserApi.isFloodgatePlayer(player.getUUID());
    }

    public UUID getOfflineBedrockPlayerUniqueId(String playerName) {
        return geyserPlayerFile.getPlayerUUID(playerName);
    }

    public String getOfflineBedrockPlayerName(UUID playerUniqueId) {
        return geyserPlayerFile.getPlayerName(playerUniqueId);
    }
}