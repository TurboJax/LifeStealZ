package com.zetaplugins.lifestealz.config;

import lombok.Getter;

import java.util.List;

@Getter
public class AntiAltConfig {
    private boolean enabled; // true
    private boolean logAttempt; // true
    private boolean preventKill; // false
    private boolean sendMessage; // false
    private List<String> commands; // new ArrayList<>();
}