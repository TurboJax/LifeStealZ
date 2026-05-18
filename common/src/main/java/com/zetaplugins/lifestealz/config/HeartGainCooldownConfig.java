package com.zetaplugins.lifestealz.config;

import lombok.Getter;

@Getter
public class HeartGainCooldownConfig {
    private boolean enabled; // false
    private int cooldown; // 120000
    private boolean dropOnCooldown; // true
    private boolean preventPickup; // true
}