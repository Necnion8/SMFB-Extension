package com.gmail.necnionch.myplugin.smfbextension;

import com.github.nova_27.mcplugin.servermanager.core.Smfb_core;
import com.github.nova_27.mcplugin.servermanager.core.config.ConfigData;
import com.github.nova_27.mcplugin.servermanager.core.config.Server;
import com.github.nova_27.mcplugin.servermanager.core.events.ServerEvent;
import com.github.nova_27.mcplugin.servermanager.core.events.ServerPreStartEvent;
import com.github.nova_27.mcplugin.servermanager.core.utils.Requester;
import com.gmail.necnionch.myplugin.smfbextension.errors.MemoryArgumentParseError;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class SMFBExtension extends Plugin implements Listener {
    public static final String PREFIX = "&4[&c&lSMFB&cExt&4] &r";
    public static final String PERMS_SHOW_ERRORS = "smfbextension.show-errors";
    private final SMFBExtConfig mainConfig = new SMFBExtConfig(this);
    public static final Set<Server> RESTART_FLAGS = Sets.newHashSet();
    private Smfb_core svrMgr;

    @Override
    public void onEnable() {
        mainConfig.load();

        Plugin tmp = getProxy().getPluginManager().getPlugin("SMFBCore");
        svrMgr = (Smfb_core) tmp;

        getProxy().getPluginManager().registerCommand(this, new SMFBExtCommand(this));
        getProxy().getPluginManager().registerListener(this, this);

    }

    public Server getServer(String id) {
        for (Server server : ConfigData.Servers) {
            if (server.ID.equalsIgnoreCase(id))
                return server;
        }
        return null;
    }

    private Object getValue(Server server, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field f = server.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        try {
            return f.get(server);
        } finally {
            f.setAccessible(false);
        }
    }

    TimerTask getServerTimerTask(Server server) throws NoSuchFieldException, IllegalAccessException {
        return (TimerTask) getValue(server, "task");
    }

    Timer getServerTimer(Server server) throws NoSuchFieldException, IllegalAccessException {
        return (Timer) getValue(server, "timer");
    }

    String getServerDirectory(Server server) throws NoSuchFieldException, IllegalAccessException {
        return (String) getValue(server, "Dir");
    }

    int getServerCloseTime(Server server) throws NoSuchFieldException, IllegalAccessException {
        return (int) getValue(server, "CloseTime");
    }

    public boolean reloadServerConfig() {
        File path = new File(svrMgr.getDataFolder(), "config.yml");

        Configuration config;
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(path);
        } catch (IOException e) {
            getLogger().info("IOException: " + e.getMessage());
            return false;
        }
        

        Field fieldFile, fieldArgs, fieldJavaCmd;
        try {
            fieldFile = Server.class.getDeclaredField("File");
            fieldFile.setAccessible(true);
            fieldArgs = Server.class.getDeclaredField("Args");
            fieldArgs.setAccessible(true);
            try {
                try {
                    fieldJavaCmd = Server.class.getDeclaredField("javaCmd");
                } catch (NoSuchFieldException e) {
                    fieldJavaCmd = Server.class.getDeclaredField("JavaCmd");
                }
                fieldJavaCmd.setAccessible(true);
            } catch (NoSuchFieldException e2) {
                fieldJavaCmd = null;
            }

        } catch (NoSuchFieldException e) {
            getLogger().info("NoSuchFieldException: " + e.getMessage());
            return false;
        }


        List<String> ids = new ArrayList<>();

        for (Server s : ConfigData.Servers) {
            Configuration sConf = config.getSection("Server." + s.ID);
            if (sConf == null)
                continue;

            ids.add(s.ID);

            String name = sConf.getString("Name");
            if (name != null && !name.isEmpty())
                s.Name = name;

            String file = sConf.getString("File");
            if (file != null && !file.isEmpty()) {
                try {
                    fieldFile.set(s, file);
                } catch (IllegalAccessException e) {
                    getLogger().info("IllegalAccessException: " + e.getMessage());
                }
            }

            String args = sConf.getString("Args");
            if (args != null && !args.isEmpty()) {
                try {
                    fieldArgs.set(s, args);
                } catch (IllegalAccessException e) {
                    getLogger().info("IllegalAccessException: " + e.getMessage());
                }
            }

            if (fieldJavaCmd != null) {
                String javaCmd = sConf.getString("JavaCmd");
                if (javaCmd != null && !javaCmd.isEmpty()) {
                    try {
                        fieldJavaCmd.set(s, javaCmd);
                    } catch (IllegalAccessException e) {
                        getLogger().info("IllegalAccessException: " + e.getMessage());
                    }
                }
            }

        }

//        // add new server
//        Configuration section = config.getSection("Server.");
//        List<String> newIds = new ArrayList<>(section.getKeys());
//        newIds.removeAll(ids);
//
//
//        Map<String, Integer> portsMap = getProxy().getServers().values().stream().collect(Collectors.toMap(
//                ServerInfo::getName, s -> s.getAddress().getPort()
//        ));
//
//        for (String newId : newIds) {
//            Integer port = portsMap.getOrDefault(newId, 0);
//            String name = section.getString(newId + ".Name");
//            String dir = section.getString(newId + ".Dir");
//            String file = section.getString(newId + ".File");
//            String args = section.getString(newId + ".Args");
//            String javaCmd = section.getString(newId + ".JavaCmd", null);
//            new Server(newId, name, port, dir, file, args, javaCmd);
//            // まさかの ConfigData.Servers[] が固定長でした。
//        }

        return true;
    }


    @EventHandler
    public void onPreStart(ServerPreStartEvent event) {
        if (event.isCancelled())
            return;

        Server server = event.getServer();
        Requester requester = event.getRequester();

        double checkMemoryMB;
        try {
            checkMemoryMB = checkFreeMemory(server);
        } catch (MemoryArgumentParseError e) {
            getLogger().warning("MemoryArgumentParseError (server: " + server.ID + "):" + e.getMessage());
            return;
        }

        if (checkMemoryMB <= 0) {
            getLogger().warning("Out of memory! cancelling server start");
            event.setCancelled(true);

            if (requester.getObject() instanceof CommandSender) {
                CommandSender sender = (CommandSender) requester.getObject();
                String message = "サーバーを起動できません。";
                message += (sender.hasPermission(PERMS_SHOW_ERRORS))
                        ? "空きメモリが不足しています (" + (Math.round(checkMemoryMB / 1024 * 10)/10d) + "GB)" : "管理者にお知らせください。";
                sender.sendMessage(new ComponentBuilder(message).create());
            }
        }

    }

    @EventHandler
    public void onServerEvent(ServerEvent event) {
        switch (event.getEventType()) {
            case ServerStarted: {
                // waiters
                try {
                    serverConnectWaiters.entrySet().removeIf(e -> {
                        if (e.getValue().getName().equals(event.getServer().ID)) {
                            e.getKey().connect(e.getValue());
                            return true;
                        }
                        return false;
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            case ServerDisabled:
            case ServerStarting:
            case ServerErrorHappened: {
                // remove flag
                RESTART_FLAGS.remove(event.getServer());
                break;
            }
            case ServerStopped: {
                // restart by flag
                if (RESTART_FLAGS.remove(event.getServer())) {
                    getLogger().info("Flagged restart: " + event.getServer().ID);
                    getProxy().getScheduler().schedule(this, () ->
                            event.getServer().StartServer(Requester.of(this)), 10, TimeUnit.MILLISECONDS);
                }
            }
        }

        switch (event.getEventType()) {
            case ServerStarting:
            case ServerStopped: {
                // cancel waiters
                serverConnectWaiters.values().removeIf(s -> s.getName().equals(event.getServer().ID));
            }
        }
    }

    public double checkFreeMemory(Server server) throws MemoryArgumentParseError {
        long maxMemory;
        try {
            maxMemory = parseMaxMemoryMB((String) getValue(server, "Args"));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new MemoryArgumentParseError(e);
        }

        GlobalMemory memory = new SystemInfo().getHardware().getMemory();
        long total = memory.getTotal() / 1024 / 1024;
        // システム搭載メモリの12.5%を確保する
        double available = (memory.getAvailable() / 1024d / 1024) - (total * mainConfig.getFreeValue());

        // 要求: Xmx指定の1.3倍サイズ
        double required = maxMemory * mainConfig.getRequireValue();

        double freeText = Math.round(available / 1024d * 10) / 10d;
        double requireText = Math.round(required / 1024 * 10) / 10d;
        getLogger().info("starting " + server.ID + " server. check freeRAM (free: " + freeText + "GB, require: " + requireText + "GB)");

        return available - required;
    }

    public long parseMaxMemoryMB(String arguments) {
        if (arguments == null || arguments.isEmpty())
            return -1;

        Pattern regex = Pattern.compile("^-xmx(\\d+)([kmg])$");

        for (String value : arguments.split(" ")) {
            Matcher match = regex.matcher(value.toLowerCase(Locale.ROOT));
            if (match.find()) {
                long size;
                String unit;
                try {
                    size = Long.parseLong(match.group(1));
                    unit = match.group(2);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    throw new MemoryArgumentParseError(e);
                }

                switch (unit) {
                    case "g":
                        return size * 1024;
                    case "m":
                        return size;
                    case "k":
                        return size / 1024;
                    default:
                        return -1;
                }
            }
        }
        return -1;
    }

    public double getSystemFreeMemory() {
        GlobalMemory memory = new SystemInfo().getHardware().getMemory();
        long total = memory.getTotal() / 1024 / 1024;
        // システム搭載メモリの12.5%を確保する
        return (memory.getAvailable() / 1024d / 1024) - (total * mainConfig.getFreeValue());
    }

    public double getSystemTotalMemory() {
        return new SystemInfo().getHardware().getMemory().getTotal() / 1024d / 1024;
    }


    private final Map<ProxiedPlayer, ServerInfo> serverConnectWaiters = Maps.newHashMap();

    @EventHandler(priority = EventPriority.LOW)
    public void onConnect(ServerConnectEvent event) {
        if (event.isCancelled() || !mainConfig.isTryReconnectInStartingServer())
            return;

        Server server = getServer(event.getTarget().getName());
        if (server == null || !(server.Started && server.Switching))
            return;

        event.setCancelled(true);
        ProxiedPlayer player = event.getPlayer();

        player.sendMessage(new ComponentBuilder("現在、サーバーを起動しています。完了したら自動的に接続されます。")
                .color(ChatColor.GOLD).create());
        serverConnectWaiters.put(player, event.getTarget());
    }

    @EventHandler
    public void onConnected(ServerConnectedEvent event) {
        serverConnectWaiters.remove(event.getPlayer());
    }

    @EventHandler
    public void onDisconnected(PlayerDisconnectEvent event) {
        serverConnectWaiters.remove(event.getPlayer());
    }

}
