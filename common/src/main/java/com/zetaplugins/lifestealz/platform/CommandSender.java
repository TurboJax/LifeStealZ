package com.zetaplugins.lifestealz.platform;

public interface CommandSender {
    boolean isPlayer();

//    World getWorld();

//    Location getLocation();

    boolean hasPermission(String permission);

    void sendMessage(String message);
}
