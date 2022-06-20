package fun.lewisdev.saverotatingshop;

import fun.lewisdev.saverotatingshop.command.SellCommand;
import fun.lewisdev.saverotatingshop.command.ShopCommand;
import fun.lewisdev.saverotatingshop.config.Messages;
import fun.lewisdev.saverotatingshop.shop.ShopManager;
import fun.lewisdev.saverotatingshop.task.TimerTask;
import me.mattstudios.mf.base.CommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class SaveRotatingShopPlugin extends JavaPlugin {

    private ShopManager shopManager;
    private BukkitTask timerTask;
    private Economy economy;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        Messages.setConfiguration(getConfig());

        (shopManager = new ShopManager(this)).onEnable();
        loadTimerTask();

        CommandManager commandManager = new CommandManager(this, true);
        commandManager.getMessageHandler().register("cmd.no.permission", Messages.NO_PERMISSION::send);
        commandManager.register(new ShopCommand(this));
        commandManager.register(new SellCommand(getShopManager()));
    }

    public void onReload() {
        shopManager.onDisable();

        reloadConfig();
        Messages.setConfiguration(getConfig());

        shopManager.onEnable();
        loadTimerTask();
    }

    @Override
    public void onDisable() {
        if(shopManager != null) shopManager.onDisable();
    }

    private void loadTimerTask() {
        if(timerTask != null) timerTask.cancel();

        if(shopManager.getBuyShop().isRotatingShop()) {
            timerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new TimerTask(this), 20L, 20L);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
