package com.gmail.necnionch.myplugin.smfbextension;

import net.md_5.bungee.api.plugin.Plugin;

public class SMFBExtConfig extends BungeeConfigDriver {
    public SMFBExtConfig(Plugin plugin) {
        super(plugin);
    }

    public float getRequireValue() {
        return config.getFloat("require-value", 1.5f);
    }

    public float getFreeValue() {
        return config.getFloat("free-value", 0.125f);
    }

    public boolean isTryReconnectInStartingServer() {
        return config.getBoolean("try-reconnect-in-starting-server", true);
    }

}
