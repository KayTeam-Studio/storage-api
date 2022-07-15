package org.kayteam.storageapi;

import org.bukkit.plugin.java.JavaPlugin;
import org.kayteam.storageapi.utils.BrandSender;

public final class StorageApi extends JavaPlugin {

    @Override
    public void onEnable() {
        BrandSender.onEnable(this);
    }

    @Override
    public void onDisable() {
        BrandSender.onDisable(this);
    }

}