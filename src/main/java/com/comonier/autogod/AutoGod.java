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
import java.util.stream.Collectors;

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
        
        getLogger().info("AutoGod " + getDescription().getVersion() + " - System successfully loaded.");
    }

    private void loadMessagesConfig() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (msgFile.exists() == false) saveResource("messages.yml", false);
        msgConfig = YamlConfiguration.loadConfiguration(msgFile);
    }

    private void loadDataConfig() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (dataFile.exists() == false) {
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
        
        if (dataConfig.getBoolean("god." + uuid)) {
            enableGod(player, false);
            player.sendMessage(prefix + color(msgConfig.getString("god-restored")));
        }
        if (dataConfig.getBoolean("fly." + uuid)) {
            enableFly(player, false);
            player.sendMessage(prefix + color(msgConfig.getString("fly-restored")));
        }

        if (config.getBoolean("god-on-login") == false) {
            return;
        }

        boolean isAuto = false;

        List<String> configNicks = config.getStringList("god-players").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (configNicks.contains(player.getName().toLowerCase())) {
            isAuto = true;
        }

        if (isAuto == false && player.hasPermission("auto.god")) {
            isAuto = true;
        }

        if (isAuto == false && perms != null) {
            List<String> groups = config.getStringList("god-groups");
            for (String group : groups) {
                if (perms.playerInGroup(player, group)) { 
                    isAuto = true; 
                    break; 
                }
            }
        }

        if (isAuto == false) {
            return;
        }

        enableGod(player, false);

        if (config.getBoolean("auto-fly-enabled")) {
            enableFly(player, false);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if ((event.getEntity() instanceof Player) == false) {
            return;
        }
        
        if (godPlayers.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = color(msgConfig.getString("prefix"));

        if (cmd.getName().equalsIgnoreCase("autogod")) {
            // EVITANDO CARACTERE DE MENOR QUE:
            if (args.length == 0) return false; 
            if (args[0].equalsIgnoreCase("reload") == false) return false;
            
            if (sender.hasPermission("auto.god.admin") == false) {
                sender.sendMessage(prefix + color(msgConfig.getString("no-permission")));
                return true;
            }
            
            reloadConfig();
            loadMessagesConfig();
            sender.sendMessage(prefix + color(msgConfig.getString("reload-success")));
            return true;
        }

        if ((sender instanceof Player) == false) return true;
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("godme")) {
            if (p.hasPermission("autogod.command.god") == false) {
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
            if (p.hasPermission("autogod.command.fly") == false) {
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
        if (save == false) return;
        dataConfig.set("god." + p.getUniqueId(), true); 
        saveData(); 
        p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("god-enabled")));
    }

    private void disableGod(Player p, boolean save) {
        godPlayers.remove(p.getUniqueId());
        if (save == false) return;
        dataConfig.set("god." + p.getUniqueId(), false); 
        saveData(); 
        p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("god-disabled")));
    }

    private void enableFly(Player p, boolean save) {
        p.setAllowFlight(true);
        if (save == false) return;
        dataConfig.set("fly." + p.getUniqueId(), true); 
        saveData(); 
        p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("fly-enabled")));
    }

    private void disableFly(Player p, boolean save) {
        p.setAllowFlight(false);
        p.setFlying(false);
        if (save == false) return;
        dataConfig.set("fly." + p.getUniqueId(), false); 
        saveData(); 
        p.sendMessage(color(msgConfig.getString("prefix") + msgConfig.getString("fly-disabled")));
    }

    private String color(String s) { 
        if (s == null) return "";
        return s.replace("&", "§"); 
    }
}
