package com.trytig.naturalmoney;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.logging.Logger;

public final class NaturalMoney extends JavaPlugin implements Listener {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;

    private static double minMoney;
    private static double maxMoney;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        // Check if plugin needs to be enabled
        if (!getConfig().getBoolean("enabled")) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        minMoney = getConfig().getDouble("min_money");
        maxMoney = getConfig().getDouble("max_money");

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            if (event.getEntity().getKiller() != null) {
                // Player killed mob
                Random random = new Random();
                DecimalFormat df = new DecimalFormat("0.00");
                double amount = Double.parseDouble(df.format(minMoney + (maxMoney - minMoney) * random.nextDouble()));
                Player player = event.getEntity().getKiller();
                Economy economy = getEconomy();
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
                economy.depositPlayer(offlinePlayer, amount);
                player.sendMessage(ChatColor.GREEN + String.format("+$%s for killing a %s", amount, event.getEntity().getName()));
            }
        }
    }

    public static Economy getEconomy() {
        return econ;
    }
}
