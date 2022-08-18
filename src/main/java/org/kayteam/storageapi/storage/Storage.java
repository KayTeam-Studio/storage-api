package org.kayteam.storageapi.storage;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public abstract class Storage {

    private final JavaPlugin javaPlugin;
    private final String directory;
    private final String fileName;
    private File file;

    private final static HashMap<String, String> globalReplacements = new HashMap<>();
    private final HashMap<String, String> replacements = new HashMap<>();

    public Storage(JavaPlugin javaPlugin, String directory, String fileName) {
        this.javaPlugin = javaPlugin;
        this.directory = directory;
        this.fileName = fileName;
    }
    public Storage(JavaPlugin javaPlugin, String fileName) {
        this.javaPlugin = javaPlugin;
        this.directory = "";
        this.fileName = fileName;
    }
    public Storage(String directory, String fileName) {
        javaPlugin = null;
        this.directory = directory;
        this.fileName = fileName;
    }

    public JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }
    public String getDirectory() {
        return directory;
    }
    public String getFileName() {
        return fileName;
    }
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }

    public abstract void register();
    public abstract void reload();
    public abstract void save();
    public abstract boolean delete();
    public abstract void createBackup();

    public abstract boolean contains(String path);

    public abstract void set(String path, Object value);

    public abstract Object get(String path);
    public abstract Object get(String path, Object def);
    public abstract Object get(String path, Object def, boolean setDefaultIfNoExist);

    public abstract boolean isBoolean(String path);
    public abstract boolean getBoolean(String path);
    public abstract boolean getBoolean(String path, boolean def);
    public abstract boolean getBoolean(String path, boolean def, boolean setDefaultIfNoExist);
    public abstract void setBoolean(String path, boolean value);

    public abstract boolean isInt(String path);
    public abstract int getInt(String path);
    public abstract int getInt(String path, int def);
    public abstract int getInt(String path, int def, boolean setDefaultIfNoExist);
    public abstract void setInt(String path, int value);

    public abstract boolean isLong(String path);
    public abstract long getLong(String path);
    public abstract long getLong(String path, long def);
    public abstract long getLong(String path, long def, boolean setDefaultIfNoExist);
    public abstract void setLong(String path, long value);

    public abstract boolean isDouble(String path);
    public abstract double getDouble(String path);
    public abstract double getDouble(String path, double def);
    public abstract double getDouble(String path, double def, boolean setDefaultIfNoExist);
    public abstract void setDouble(String path, double value);

    public abstract boolean isString(String path);
    public abstract String getString(String path);
    public abstract String getString(String path, String def);
    public abstract String getString(String path, String def, boolean setDefaultIfNoExist);
    public abstract void setString(String path, String value);

    public abstract boolean isStringList(String path);
    public abstract List<String> getStringList(String path);
    public abstract List<String> getStringList(String path, List<String> def);
    public abstract List<String> getStringList(String path, List<String> def, boolean setDefaultIfNoExist);
    public abstract void setStringList(String path, List<String> value);

    public abstract boolean isLocation(String path);
    public abstract Location getLocation(String path);
    public abstract Location getLocation(String path, Location def);
    public abstract Location getLocation(String path, Location def, boolean setDefaultIfNoExist);
    public abstract void setLocation(String path, Location value);

    public abstract boolean isItemStack(String path);
    public abstract ItemStack getItemStack(String path);
    public abstract ItemStack getItemStack(String path, ItemStack def);
    public abstract ItemStack getItemStack(String path, ItemStack def, boolean setDefaultIfNoExist);
    public abstract void setItemStack(String path, ItemStack value);

    public static HashMap<String, String> getGlobalReplacements() {
        return globalReplacements;
    }
    public HashMap<String, String> getReplacements() {
        return replacements;
    }

}