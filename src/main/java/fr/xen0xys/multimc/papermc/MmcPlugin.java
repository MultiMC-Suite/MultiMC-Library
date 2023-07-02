package fr.xen0xys.multimc.papermc;

import org.bukkit.plugin.java.JavaPlugin;

public class MmcPlugin extends JavaPlugin {

    public static JavaPlugin INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }
}
