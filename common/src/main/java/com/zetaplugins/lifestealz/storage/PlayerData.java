package com.zetaplugins.lifestealz.storage;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    @Getter private final String name;
    @Getter private final String uuid;
    @Getter private double maxHealth = 20;
    @Getter private int craftedHearts;
    @Getter private int craftedRevives;
    @Getter private int hasBeenRevived;
    @Getter private int killedOtherPlayers;
    @Getter private long firstJoin;

    private final Set<String> modifiedFields = new HashSet<>(); // Track modified fields

    public PlayerData(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid.toString();
    }

    public void setMaxHealth(double maxHealth) {
        if (this.maxHealth != maxHealth) {
            this.maxHealth = maxHealth;
            modifiedFields.add("maxhp");
        }
    }

    public void setCraftedHearts(int craftedHearts) {
        if (this.craftedHearts != craftedHearts) {
            this.craftedHearts = craftedHearts;
            modifiedFields.add("craftedHearts");
        }
    }

    public void setCraftedRevives(int craftedRevives) {
        if (this.craftedRevives != craftedRevives) {
            this.craftedRevives = craftedRevives;
            modifiedFields.add("craftedRevives");
        }
    }

    public void setHasBeenRevived(int hasBeenRevived) {
        if (this.hasBeenRevived != hasBeenRevived) {
            this.hasBeenRevived = hasBeenRevived;
            modifiedFields.add("hasbeenRevived");
        }
    }

    public void setKilledOtherPlayers(int killedOtherPlayers) {
        if (this.killedOtherPlayers != killedOtherPlayers) {
            this.killedOtherPlayers = killedOtherPlayers;
            modifiedFields.add("killedOtherPlayers");
        }
    }

    public void setFirstJoin(long firstJoin) {
        if (this.firstJoin != firstJoin) {
            this.firstJoin = firstJoin;
            modifiedFields.add("firstJoin");
        }
    }

    public boolean hasChanges() {
        return !modifiedFields.isEmpty();
    }

    public Set<String> getModifiedFields() {
        return new HashSet<>(modifiedFields);
    }

    public void clearModifiedFields() {
        modifiedFields.clear();
    }
}