package com.zetaplugins.lifestealz.config;

import lombok.Getter;

import java.util.List;

@Getter
public class MainConfig {
    // General Settings
    private boolean checkForUpdates; // true
    private String lang; // "en-US"
    private StorageConfig storage; // new StorageConfig()

    // Heart Settings
    private int startHearts; // 10
    private int maxHearts; // 20
    private int reviveHearts; // 1
    private int heartsPerKill; // 1
    private int heartsPerNaturalDeath; // 1
    private int minHearts; // 0
    private boolean enforceMaxHeartsOnAdminCommands; // false
    private HeartItemConfig heartItem; // new HeartItemConfig();

    // Heart Behaviour Settings
    private boolean dropHeartsPlayer; // false
    private boolean dropHeartsNatural; // true
    private boolean dropHeartsIfMax; // true
    private boolean looseHeartsToNature; // true
    private boolean looseHeartsToPlayer; // true
    private boolean announceElimination; // true

    private boolean allowDyingFromWithdraw; // true
    private boolean healOnHeartUse; // false
    private boolean playTotemEffect; // false
    private int heartCooldown; // 0
    private int maxRevives; // -1

    // Disabling features
    private boolean preventTotems; // false
    private boolean preventCrystalPVP; // false
    private boolean preventRespawnAnchors; // false
    private boolean preventBeds; // false
    private boolean preventCustomItemsInItemFrames; // true

    // Extensive Customization
    private boolean disablePlayerBanOnElimination; // false
    private boolean heartRewardOnElimination; // true
    private List<String> eliminationCommands; // new ArrayList<>()
    private List<String> heartuseCommands; // new ArrayList<>()
    private List<String> reviveuseCommands; // new ArrayList<>()
    private List<String> reviveStartCommands; // new ArrayList<>()
    private boolean showBossbar; // false
    private String bossbarColor; // "RED"
    private String bossbarStyle; // "SOLID"
    private GracePeriodConfig gracePeriod; // new GracePeriodConfig()
    private BypassPermissionConfig bypassPermission; // new BypassPermissionConfig()
    private HeartGainCooldownConfig heartGainCooldown; // new HeartGainCooldownConfig()
    private AntiAltConfig antiAlt; // new AntiAltConfig()
    private WebhookConfig webhook; // new WebhookConfig()
}