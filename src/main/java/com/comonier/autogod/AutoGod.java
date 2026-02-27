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
import org.bukkit.event.player.PlayerQuitEvent;
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
    private String lang;

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
        
        getLogger().info("AutoGod loaded! Language: " + lang);
    }

    private void loadMessagesConfig() {
        reloadConfig();
        lang = getConfig().getString("language", "en");
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (msgFile.exists() == false) saveResource("messages.yml", false);
        msgConfig = YamlConfiguration.loadConfiguration(msgFile);
    }

    private String getMsg(String key) {
        String fullKey = lang + "." + key;
        return color(msgConfig.getString(fullKey, msgConfig.getString("en." + key, "")));
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
        String prefix = getMsg("prefix");
        
        boolean isAuth = false;
        List<String> nicks = config.getStringList("god-players").stream()
                .map(String::toLowerCase).collect(Collectors.toList());
        if (nicks.contains(player.getName().toLowerCase())) isAuth = true;
        
        if (isAuth == false && perms != null) {
            for (String g : config.getStringList("god-groups")) {
                if (perms.playerInGroup(player, g)) { isAuth = true; break; }
            }
        }

        boolean shouldGod = dataConfig.getBoolean("god." + uuid);
        if (shouldGod == false && config.getBoolean("god-on-login")) {
            if (isAuth || player.hasPermission("auto.god.login")) shouldGod = true;
        }
        if (shouldGod) {
            enableGod(player, false);
            player.sendMessage(prefix + getMsg("god-restored"));
        }

        boolean shouldFly = dataConfig.getBoolean("fly." + uuid);
        if (shouldFly == false && config.getBoolean("auto-fly-enabled")) {
            if (isAuth || player.hasPermission("auto.fly.login")) shouldFly = true;
        }
        if (shouldFly) {
            enableFly(player, false);
            player.sendMessage(prefix + getMsg("fly-restored"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        dataConfig.set("god." + uuid, godPlayers.contains(uuid));
        dataConfig.set("fly." + uuid, player.getAllowFlight());
        saveData();
        godPlayers.remove(uuid);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player == false) return;
        if (godPlayers.contains(event.getEntity().getUniqueId())) event.setCancelled(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = getMsg("prefix");
        
        if (cmd.getName().equalsIgnoreCase("autogod")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("reload") == false) return false;
            if (sender.hasPermission("auto.god.admin") == false) {
                sender.sendMessage(prefix + getMsg("no-permission"));
                return true;
            }
            loadMessagesConfig();
            sender.sendMessage(prefix + getMsg("reload-success"));
            return true;
        }

        if (sender instanceof Player == false) return true;
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("godme")) {
            if (p.hasPermission("autogod.command.god") == false) {
                p.sendMessage(prefix + getMsg("no-permission"));
                return true;
            }
            if (godPlayers.contains(p.getUniqueId())) disableGod(p, true);
            else enableGod(p, true);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("flyme")) {
            if (p.hasPermission("autogod.command.fly") == false) {
                p.sendMessage(prefix + getMsg("no-permission"));
                return true;
            }
            if (p.getAllowFlight()) disableFly(p, true);
            else enableFly(p, true);
            return true;
        }
        return false;
    }

    private void enableGod(Player p, boolean save) {
        godPlayers.add(p.getUniqueId());
        if (save) {
            dataConfig.set("god." + p.getUniqueId(), true);
            saveData();
            p.sendMessage(getMsg("prefix") + getMsg("god-enabled"));
        }
    }

    private void disableGod(Player p, boolean save) {
        godPlayers.remove(p.getUniqueId());
        if (save) {
            dataConfig.set("god." + p.getUniqueId(), false);
            saveData();
            p.sendMessage(getMsg("prefix") + getMsg("god-disabled"));
        }
    }

    private void enableFly(Player p, boolean save) {
        p.setAllowFlight(true);
        p.setFlying(true);
        if (save) {
            dataConfig.set("fly." + p.getUniqueId(), true);
            saveData();
            p.sendMessage(getMsg("prefix") + getMsg("fly-enabled"));
        }
    }

    private void disableFly(Player p, boolean save) {
        p.setAllowFlight(false);
        p.setFlying(false);
        if (save) {
            dataConfig.set("fly." + p.getUniqueId(), false);
            saveData();
            p.sendMessage(getMsg("prefix") + getMsg("fly-disabled"));
        }
    }

    private String color(String s) { 
        return s == null ? "" : s.replace("&", "§"); 
    }
}
