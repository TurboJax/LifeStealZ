package com.zetaplugins.lifestealz.config;

import lombok.Getter;

@Getter
public class BypassPermissionConfig {
    private boolean enabled; // true
    private boolean damageFromPlayers; // false
    private boolean damageToPlayers; // false
    private boolean useHearts; // false
    private boolean looseHearts; // false
    private boolean gainHearts; // false
}