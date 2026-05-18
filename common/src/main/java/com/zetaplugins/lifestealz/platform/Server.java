package com.zetaplugins.lifestealz.platform;

import java.util.Collection;
import java.util.UUID;

public interface Server {
    Collection<OfflinePlayer> getPlayers();

    Collection<Player> getOnlinePlayers();

    OfflinePlayer getPlayer(String name);

    OfflinePlayer getPlayer(UUID uuid);

    CommandSender getConsole();

    void runCommand(CommandSender sender, String cmd);
}