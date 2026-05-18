package com.zetaplugins.lifestealz.config;

import lombok.Getter;

@Getter
public class StorageConfig {
    private String type; // "SQLite"
    private String host; // "localhost"
    private int port; // 3306
    private String database; // "lifestealz
    private String username; // "root"
    private String password; // "password"
}