package com.gmail.necnionch.myplugin.smfbextension;

import com.github.nova_27.mcplugin.servermanager.core.config.ConfigData;
import com.github.nova_27.mcplugin.servermanager.core.config.Server;
import com.gmail.necnionch.myplugin.smfbextension.utils.Argument;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.Timer;
import java.util.TimerTask;

public class SMFBExtCommand extends Command {
    private SMFBExtension pl;

    public SMFBExtCommand(SMFBExtension plugin) {
        super("smfbext", "smfbextension.command.smfbext");
        this.pl = plugin;
    }


    @Override
    public void execute(CommandSender sender, String[] argument) {
        Argument args = new Argument(argument);

        if (args.equal(0, "info") && args.length >= 2) {
            Server server = pl.getServer(args.get(1, null));
            if (server == null) {
                sender.sendMessage(fmt("&cサーバーが見つかりません。"));
                return;
            }

            sender.sendMessage(fmt("&6サーバーID: &f&l" + server.ID + "&7(" + server.Name + ")"));
            String tmp = "&onull";
            if (server.Process != null)
                tmp = (server.Process.isAlive())? "&aisAlive" : "&cExit=" + server.Process.exitValue();
            sender.sendMessage(fmt("  &7process   : &f" + tmp));
            sender.sendMessage(fmt("  &7started   : &f" + server.Started));
            sender.sendMessage(fmt("  &7switching : &f" + server.Switching));
            sender.sendMessage(fmt("  &7enabled   : &f" + server.Enabled));
            TimerTask task = null;
            tmp = "&onull";
            try {
                task = pl.getServerTimerTask(server);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                tmp = "&c&o" + e.getClass().getSimpleName();
            }
            if (task != null) {
                tmp = "scheduledExecutionTime=" + task.scheduledExecutionTime();
            }
            sender.sendMessage(fmt("  &7task      : &f" + tmp));

            Timer timer = null;
            tmp = "&onull";
            try {
                timer = pl.getServerTimer(server);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                tmp = "&c&oAccessError";
            }
            if (timer != null) {
                tmp = "&o" + timer.toString();
            }
            sender.sendMessage(fmt("  &7timer     : &f" + tmp));

//            if (server.requests.isEmpty()) {
//                sender.sendMessage(fmt("  &7requests  : &f&onone"));
//            } else {
//                sender.sendMessage(fmt("  &7requests  : &f" + server.requests.size()));
//            }

        } else if (args.equal(0, "started") && args.length >= 3) {
            Server server = pl.getServer(args.get(1, null));
            if (server == null) {
                sender.sendMessage(fmt("&cサーバーが見つかりません。"));
                return;
            }

            Boolean yes = null;
            switch (args.get(2, "").toLowerCase()) {
                case "true":
                case "yes":
                case "y":
                case "on":
                case "enable":
                    yes = true;
                    break;
                case "false":
                case "no":
                case "n":
                case "off":
                case "disable":
                    yes = false;
                    break;
            }

            if (yes == null) {
                sender.sendMessage(fmt("&c'true' 'false' を使って指定してください。"));
                return;
            }

            server.Started = yes;
            sender.sendMessage(fmt("変更しました。"));

        } else if (args.equal(0, "switching") && args.length >= 3) {
            Server server = pl.getServer(args.get(1, null));
            if (server == null) {
                sender.sendMessage(fmt("&cサーバーが見つかりません。"));
                return;
            }

            Boolean yes = null;
            switch (args.get(2, "").toLowerCase()) {
                case "true":
                case "yes":
                case "y":
                case "on":
                case "enable":
                    yes = true;
                    break;
                case "false":
                case "no":
                case "n":
                case "off":
                case "disable":
                    yes = false;
                    break;
            }

            if (yes == null) {
                sender.sendMessage(fmt("&c'true' 'false' を使って指定してください。"));
                return;
            }

            server.Switching = yes;
            sender.sendMessage(fmt("変更しました。"));

        } else if (args.equal(0, "lobbyCheck")) {
            Server lobby = ConfigData.Lobby;
            if (lobby != null) {
                sender.sendMessage(fmt("Lobby: " + lobby.ID + " (" + lobby.Name + ")"));
                sender.sendMessage(fmt("  started: " + lobby.Started));
                sender.sendMessage(fmt("  switching: " + lobby.Switching));

                for (Server s : ConfigData.Servers) {
                    if (lobby.equals(s)) {
                        sender.sendMessage(fmt("  equal: " + s.ID));
                    }
                }

            } else {
                sender.sendMessage(fmt("Lobby: null"));
            }

        } else if (args.equal(0, "setLobby") && args.length >= 2) {
            Server server = Server.getServerByID(args.get(1, null));
            if (server != null) {
                ConfigData.Lobby = server;
                sender.sendMessage(fmt("設定しました。"));
            } else {
                sender.sendMessage(fmt("&cサーバーが見つかりません。"));
            }

        } else if (args.equal(0, "freeMemory")) {
            double free = pl.getSystemFreeMemory();
            double total = pl.getSystemTotalMemory();
            double freeText = Math.round(free / 1024d * 10) / 10d;
            sender.sendMessage(fmt("&cシステムの空きメモリ: &6" + freeText + " GB &7(" + (Math.round(free / total * 1000) / 10) + "%)"));

        } else if (args.equal(0, "restart") && args.length >= 2) {
            Server server = Server.getServerByID(args.get(1, null));
            if (server == null) {
                sender.sendMessage(fmt("&cサーバーが見つかりません。"));
                return;
            }

            if (server.Started) {
                if (SMFBExtension.RESTART_FLAGS.add(server)) {
                    sender.sendMessage(fmt("&6" + server.ID + " サーバーを再起動します。"));

                    if (!server.Switching)
                        server.StopServer();
                } else {
                    SMFBExtension.RESTART_FLAGS.remove(server);
                    sender.sendMessage(fmt("&e" + server.ID + " サーバーの再起動フラグを解除しました。"));
                }

            } else {
                pl.getProxy().getPluginManager().dispatchCommand(sender, "smfb start " + server.ID);
            }

        } else if (args.equal(0, "reload")) {
            boolean result = pl.reloadServerConfig();
            if (result) {
                sender.sendMessage(fmt("再読み込みしました。"));
            } else {
                sender.sendMessage(fmt("&c失敗しました。"));
            }

        } else {
            sender.sendMessage(fmt("/smfbext info (serverId)"));
            sender.sendMessage(fmt("/smfbext <started/switching> (serverId) <true/false>"));
            sender.sendMessage(fmt("/smfbext lobbyCheck"));
            sender.sendMessage(fmt("/smfbext setLobby (serverId)"));
            sender.sendMessage(fmt("/smfbext freeMemory"));
            sender.sendMessage(fmt("/smfbext restart (serverId)"));
            sender.sendMessage(fmt("/smfbext reload"));
        }

    }

    private BaseComponent[] fmt(String message) {
        String prefix = SMFBExtension.PREFIX;
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                prefix + message
        ));
    }


}
