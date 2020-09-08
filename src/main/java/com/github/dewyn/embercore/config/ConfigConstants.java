package com.github.dewyn.embercore.config;

public enum ConfigConstants {
    DEBUG("debug");

    private String path;

    ConfigConstants(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
