package com.comonier.autogod;

import net.milkbowl.vault.permission.Permission;
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
    private final Set<UUID> godPlayers = new HashSet<>();
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
        
        getLogger().info("AutoGod 1.1 - System successfully loaded.");
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
        UUID uuid = player.getUniqueId();
        FileConfiguration config = getConfig();
        String prefix = color(msgConfig.getString("prefix"));
        
        // REVERSED LOGIC: Restore manual persistence first
        if (dataConfig.getBoolean("god." + uuid)) {
            enableGod(player, false);
            player.sendMessage(prefix + color(msgConfig.getString("god-restored")));
        }
        if (dataConfig.getBoolean("fly." + uuid)) {
            enableFly(player, false);
            player.sendMessage(prefix + color(msgConfig.getString("fly-restored")));
        }

        // REVERSED LOGIC: Stop if auto-activation system is globally disabled
        if (!config.getBoolean("god-on-login")) {
            return;
        }

        boolean isAuto = false;
        List<String> configNicks = config.getStringList("god-players");

        // Check if player name is in the config list
        if (configNicks.contains(player.getName())) {
            isAuto = true;
        }

        // Check for auto permission
        if (!isAuto && player.hasPermission("auto.god")) {
            isAuto = true;
        }

        // Check for Vault groups
        if (!isAuto && perms != null) {
            List<String> groups = config.getStringList("god-groups");
            for (String group : groups) {
                if (perms.playerInGroup(player, group)) { isAuto = true; break; }
            }
        }

        // REVERSED LOGIC: Stop if player is not authorized for auto-features
        if (!isAuto) {
            return;
        }

        // Apply Auto God
        enableGod(player, false);

        // Apply Auto Flight only if enabled in config
        if (config.getBoolean("auto-fly-enabled")) {
            enableFly(player, false);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        // REVERSED LOGIC: Ignore if the entity is not a player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        // Cancel damage if player is in the god set
        if (godPlayers.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = color(msgConfig.getString("prefix"));

        if (cmd.getName().equalsIgnoreCase("autogod")) {
            if (args.length == 0) return false;
            if (!args[0].equalsIgnoreCase("reload")) return false;
            
            if (!sender.hasPermission("auto.god.admin")) {
                sender.sendMessage(prefix + color(msgConfig.getString("no-permission")));
                return true;
            }
            
            reloadConfig();
            loadMessagesConfig();
            sender.sendMessage(prefix + color(msgConfig.getString("reload-success")));
            return true;
        }

        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("godme")) {
            if (!p.hasPermission("autogod.command.god")) {
                p.sendMessage(prefix + color(msgConfig.getString("no-permission")));
                return true;
            }
            
            if (godPlayers.contains(p.getUniqueId())) {
                disableGod(p, true);
                return true;
            }
            
            enableGod(p, true);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("flyme")) {
            if (!p.hasPermission("autogod.command.fly")) {
                p.sendMessage(prefix + color(msgConfig.getString("no-permission")));
                return true;
            }
            
            if (p.getAllowFlight()) {
                disableFly(p, true);
                return true;
            }
            
            enableFly(p, true);
            return true;
        }
        return false;
    }

    private void enableGod(Player p, boolean save) {
        godPlayers.add(p.getUniqueId());
        if (save) { dataConfig.set("god." + p.getUniqueId(), true); saveData(); }
        if (save) p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("god-enabled")));
    }

    private void disableGod(Player p, boolean save) {
        godPlayers.remove(p.getUniqueId());
        if (save) { dataConfig.set("god." + p.getUniqueId(), false); saveData(); }
        if (save) p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("god-disabled")));
    }

    private void enableFly(Player p, boolean save) {
        p.setAllowFlight(true);
        if (save) { dataConfig.set("fly." + p.getUniqueId(), true); saveData(); }
        if (save) p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("fly-enabled")));
    }

    private void disableFly(Player p, boolean save) {
        p.setAllowFlight(false);
        p.setFlying(false);
        if (save) { dataConfig.set("fly." + p.getUniqueId(), false); saveData(); }
        if (save) p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("fly-disabled")));
    }

    private String color(String s) { 
        if (s == null) return "";
        return s.replace("&", "§"); 
    }
}
