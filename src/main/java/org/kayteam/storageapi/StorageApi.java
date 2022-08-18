package org.kayteam.storageapi;

import org.bukkit.plugin.java.JavaPlugin;
import org.kayteam.storageapi.storage.YML;
import org.kayteam.storageapi.utils.BrandSender;

public final class StorageApi extends JavaPlugin {

    @Override
    public void onEnable() {
        BrandSender.onEnable(this);
        YML settings = new YML(this, "settings");
        settings.register();
    }

    @Override
    public void onDisable() {
        BrandSender.onDisable(this);
    }

}