package com.gmail.necnionch.myplugin.smfbextension;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.logging.Logger;

public class BungeeConfigDriver {
    protected Configuration config;
    private Plugin plugin;
    private File file;
    private String resourceFileName;

    public BungeeConfigDriver(Plugin plugin, String fileName, String resourceFileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);
        this.resourceFileName = resourceFileName;
    }

    public BungeeConfigDriver(Plugin plugin, File file, String resourceFileName) {
        this.plugin = plugin;
        this.file = file;
        this.resourceFileName = resourceFileName;
    }

    public BungeeConfigDriver(Plugin plugin) {
        this(plugin, "config.yml", "bungee-config.yml");
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

    public boolean load() {
        try {
            if (!this.plugin.getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                this.plugin.getDataFolder().mkdir();
            }

            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();

                try (InputStream inputStream = this.plugin.getResourceAsStream(this.resourceFileName); FileOutputStream outputStream = new FileOutputStream(file)) {
                    //noinspection UnstableApiUsage
                    ByteStreams.copy(inputStream, outputStream);
                }
            }

            Configuration config;
            try (InputStreamReader stream = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(stream);
            }

            this.config = config;
            if (config != null) {
                return onLoaded(config);
            } else {
                return false;
            }

        } catch (Exception e) {
            this.getLogger().severe("Unable to load \"" + file.toString() + "\". An error has occurred.");
            this.getLogger().severe(e.getLocalizedMessage());
            return false;
        }
    }

    public boolean save() {
        if (!this.plugin.getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            this.plugin.getDataFolder().mkdir();
        }

        boolean var2;
        try {

            try (OutputStreamWriter var13 = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.config, var13);
            }
            var2 = true;

        } catch (Exception e) {
            this.getLogger().severe("Unable to save \"" + file.toString() + "\". An error has occurred.");
            this.getLogger().severe(e.getLocalizedMessage());
            var2 = false;
        }

        return var2;
    }

    public boolean onLoaded(Configuration config) {
        return true;
    }

}
