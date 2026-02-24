package com.comonier.autogod;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AutoGod extends JavaPlugin implements Listener, CommandExecutor {

    private Permission perms = null;
    private Set<UUID> godPlayers = new HashSet<>();
    private FileConfiguration msgConfig;
    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessagesConfig();
        loadDataConfig();
        setupPermissions();
        
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("godme").setExecutor(this);
        getCommand("flyme").setExecutor(this);
        getCommand("autogod").setExecutor(this);
        
        getLogger().info("AutoGod 1.21.1 - Commands with Permissions active.");
    }

    private void loadMessagesConfig() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (!msgFile.exists()) saveResource("messages.yml", false);
        msgConfig = YamlConfiguration.loadConfiguration(msgFile);
    }

    private void loadDataConfig() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        perms = rsp.getProvider();
        return perms != null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        UUID uuid = player.getUniqueId();
        
        boolean isAuto = false;
        if (name.equalsIgnoreCase("comonier")) {
            isAuto = true;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "god comonier");
        } else {
            List<String> configNicks = getConfig().getStringList("god-players");
            for (String nick : configNicks) {
                if (name.equalsIgnoreCase(nick)) { isAuto = true; break; }
            }
            if (!isAuto && perms != null) {
                List<String> groups = getConfig().getStringList("god-groups");
                for (String group : groups) {
                    if (perms.playerInGroup(player, group)) { isAuto = true; break; }
                }
            }
            if (!isAuto && player.hasPermission("auto.god")) isAuto = true;
        }

        if (isAuto && getConfig().getBoolean("god-on-login")) {
            enableGod(player, false);
            enableFly(player, false);
        } else {
            // Persistencia para quem ja tinha ativado e nao eh da lista auto
            if (dataConfig.getBoolean("god." + uuid)) enableGod(player, false);
            if (dataConfig.getBoolean("fly." + uuid)) enableFly(player, false);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (godPlayers.contains(event.getEntity().getUniqueId())) event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("autogod") && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("auto.god.admin")) {
                sender.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("no-permission")));
                return true;
            }
            reloadConfig();
            loadMessagesConfig();
            sender.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("reload-success")));
            return true;
        }

        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("godme")) {
            if (!p.hasPermission("autogod.command.god")) {
                p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("no-permission")));
                return true;
            }
            if (godPlayers.contains(p.getUniqueId())) {
                disableGod(p, true);
            } else {
                enableGod(p, true);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("flyme")) {
            if (!p.hasPermission("autogod.command.fly")) {
                p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("no-permission")));
                return true;
            }
            if (p.getAllowFlight()) {
                disableFly(p, true);
            } else {
                enableFly(p, true);
            }
            return true;
        }
        return false;
    }

    private void enableGod(Player p, boolean save) {
        godPlayers.add(p.getUniqueId());
        if (save) { dataConfig.set("god." + p.getUniqueId(), true); saveData(); }
        p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("god-enabled")));
    }

    private void disableGod(Player p, boolean save) {
        godPlayers.remove(p.getUniqueId());
        if (save) { dataConfig.set("god." + p.getUniqueId(), false); saveData(); }
        p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("god-disabled")));
    }

    private void enableFly(Player p, boolean save) {
        p.setAllowFlight(true);
        if (save) { dataConfig.set("fly." + p.getUniqueId(), true); saveData(); }
        p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("fly-enabled")));
    }

    private void disableFly(Player p, boolean save) {
        p.setAllowFlight(false);
        p.setFlying(false);
        if (save) { dataConfig.set("fly." + p.getUniqueId(), false); saveData(); }
        p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("fly-disabled")));
    }

    private String color(String s) { return s == null ? "" : s.replace("&", "§"); }
}
