package fr.xen0xys.multimc.papermc;

import fr.xen0xys.multimc.papermc.listeners.OnPlayerLogin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MmcPlugin extends JavaPlugin {

    public static JavaPlugin INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;
        this.registerEvents();
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    private void registerEvents(){
        Bukkit.getPluginManager().registerEvents(new OnPlayerLogin(this), this);
    }
}
