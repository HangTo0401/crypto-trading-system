package com.example.demo.cache;

import lombok.Getter;

public enum CacheName {
    PRICING("pricing");

    @Getter
    private String name;

    private CacheName(String cacheName) {
        this.name = cacheName;
    }

    @Override
    public String toString() {
        return getName();
    }
}
