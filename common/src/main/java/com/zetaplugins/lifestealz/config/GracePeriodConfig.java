package com.zetaplugins.lifestealz.config;

import lombok.Getter;

import java.util.List;

@Getter
public class GracePeriodConfig {
    private boolean enabled; // false
    private int duration; // 60
    private boolean announce; // true
    private boolean playSound; // true

    private boolean damageFromPlayers; // false
    private boolean damageToPlayers; // false
    private boolean useHearts; // false
    private boolean looseHearts; // false
    private boolean gainHearts; // false
    private boolean allowWithdraw; // false

    private List<String> startCommands; // new ArrayList<>()
    private List<String> endCommands; // new ArrayList<>()
}