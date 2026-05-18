package com.zetaplugins.lifestealz.platform;

public interface Player extends OfflinePlayer, CommandSender {
    @Override
    default boolean isPlayer() {
        return true;
    }

    double getHealth();
    void setHealth(double health);

    double getMaxHealth();
    void setMaxHealth(double health);

    public void playSound(Location location, String sound, float volume, float pitch);
}
