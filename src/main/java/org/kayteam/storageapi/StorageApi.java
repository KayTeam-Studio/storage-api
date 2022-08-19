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
        System.out.println(settings.getInt("test", 11, true));
        System.out.println(settings.getItemStack("removePoint.item", null, true));
    }

    @Override
    public void onDisable() {
        BrandSender.onDisable(this);
    }

}