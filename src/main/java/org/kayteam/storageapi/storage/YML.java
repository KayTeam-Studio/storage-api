package org.kayteam.storageapi.storage;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTType;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.PatternSyntaxException;

public class YML extends Storage{

    private FileConfiguration defaultFileConfiguration;
    private FileConfiguration fileConfiguration;

    public YML(JavaPlugin javaPlugin, String directory, String fileName) {
        super(javaPlugin, directory, fileName);
    }

    public YML(JavaPlugin javaPlugin, String fileName) {
        super(javaPlugin, fileName);
    }

    public YML(String directory, String fileName) {
        super(directory, fileName);
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }
    public void setFileConfiguration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    @Override
    public void register() {
        if (fileConfiguration == null) reload();
    }

    @Override
    public void reload() {

        File directory = new File(getDirectory());

        if (!directory.exists()) {
            if (directory.mkdirs()) {
                Bukkit.getLogger().log(Level.SEVERE, "Error: Fail in directory creation.");
            }
        }

        setFile(new File(getDirectory(), getFileName() + ".yml"));

        if (!getFile().exists()) {
            try {
                if (getFile().createNewFile()) {
                    if (getJavaPlugin() != null) {
                        String localDirectory = "";
                        if (!getDirectory().equals(getJavaPlugin().getDataFolder().getPath())) {
                            localDirectory = getDirectory().replaceAll(getJavaPlugin().getDataFolder().getPath(), "");
                            localDirectory = localDirectory.replaceAll(File.separator, "/");
                            localDirectory = localDirectory.replaceFirst("/", "");
                            localDirectory = localDirectory + "/";
                        }
                        InputStream inputStream = getJavaPlugin().getResource(  localDirectory + getFileName() + ".yml");

                        if (inputStream != null) {
                            getJavaPlugin().saveResource(localDirectory + getFileName() + ".yml", true);
                        }
                    }
                }
            } catch (IOException | IllegalArgumentException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error: Local file failed in load.");
            }
        }
        if (getJavaPlugin() != null) {
            loadDefaultFileConfiguration();
        }

        fileConfiguration = YamlConfiguration.loadConfiguration(getFile());

        if (contains("replacements.global")) {
            if (fileConfiguration.isConfigurationSection("replacements.global")) {
                Set<String> keys = fileConfiguration.getConfigurationSection("replacements.global").getKeys(false);
                for (String key:keys) {
                    Storage.getGlobalReplacements().put(key, getString("replacements.global." + key));
                }
            }
        }

        if (contains("replacements.local")) {
            if (fileConfiguration.isConfigurationSection("replacements.local")) {
                Set<String> keys = fileConfiguration.getConfigurationSection("replacements.local").getKeys(false);
                for (String key: keys) {
                    getReplacements().put(key, getString("replacements.local." + key));
                }
            }
        }
    }

    @Override
    public void save() {
        try {
            fileConfiguration.save(getFile());
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error: File do not saved.");
        }
    }

    @Override
    public boolean delete() {
        File file = new File(getDirectory(), getFileName() + ".yml");
        if (file.exists()) return file.delete();
        return false;
    }

    @Override
    public void createBackup() {
        YML backup = new YML(getDirectory(), getFileName() + "-backup");
        backup.register();
        backup.setFileConfiguration(fileConfiguration);
        backup.save();
    }

    public void loadDefaultFileConfiguration() {
        try {
            if (getFile().createNewFile()) {
                if (getJavaPlugin() != null) {
                    String localDirectory = "";
                    if (!getDirectory().equals(getJavaPlugin().getDataFolder().getPath())) {
                        localDirectory = getDirectory().replaceAll(getJavaPlugin().getDataFolder().getPath(), "");
                        localDirectory = localDirectory.replaceAll(File.separator, "/");
                        localDirectory = localDirectory.replaceFirst("/", "");
                        localDirectory = localDirectory + "/";
                    }

                    InputStream inputStream = getJavaPlugin().getResource(  localDirectory + getFileName() + ".yml");

                    if (inputStream != null) {
                        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
                        defaultFileConfiguration.setDefaults(defConfig);
                        defaultFileConfiguration = YamlConfiguration.loadConfiguration(reader);
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error: Default file failed in the load.");
        }
    }

    @Override
    public boolean contains(String path) {
        return fileConfiguration.contains(path);
    }

    @Override
    public void set(String path, Object value) {
        fileConfiguration.set(path, value);
    }

    @Override
    public Object get(String path) {
        return get(path, null);
    }

    @Override
    public Object get(String path, Object def) {
        return get(path, def, true);
    }

    @Override
    public Object get(String path, Object def, boolean setDefaultIfNoExist) {
        if (!contains(path)) {
            if (defaultFileConfiguration != null) {
                if (defaultFileConfiguration.contains(path)) {
                    return defaultFileConfiguration.get(path);
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    return def;
                }
            }
        }
        return fileConfiguration.get(path);
    }

    @Override
    public boolean isBoolean(String path) {
        return fileConfiguration.isBoolean(path);
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return getBoolean(path, def, true);
    }

    @Override
    public boolean getBoolean(String path, boolean def, boolean setDefaultIfNoExist) {
        if (!contains(path)) {
            if (defaultFileConfiguration != null) {
                if (defaultFileConfiguration.contains(path)) {
                    return defaultFileConfiguration.getBoolean(path);
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    return def;
                }
            }
        }
        return fileConfiguration.getBoolean(path);
    }

    @Override
    public void setBoolean(String path, boolean value) {
        set(path, value);
    }

    @Override
    public boolean isInt(String path) {
        return fileConfiguration.isInt(path);
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(String path, int def) {
        return getInt(path, def, true);
    }

    @Override
    public int getInt(String path, int def, boolean setDefaultIfNoExist) {
        if (!contains(path)) {
            if (defaultFileConfiguration != null) {
                if (defaultFileConfiguration.contains(path)) {
                    return defaultFileConfiguration.getInt(path);
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    return def;
                }
            }
        }
        return fileConfiguration.getInt(path);
    }

    @Override
    public void setInt(String path, int value) {
        set(path, value);
    }

    @Override
    public boolean isLong(String path) {
        return fileConfiguration.isLong(path);
    }

    @Override
    public long getLong(String path) {
        return getLong(path, 0);
    }

    @Override
    public long getLong(String path, long def) {
        return getLong(path, def, true);
    }

    @Override
    public long getLong(String path, long def, boolean setDefaultIfNoExist) {
        if (!contains(path)) {
            if (defaultFileConfiguration != null) {
                if (defaultFileConfiguration.contains(path)) {
                    return defaultFileConfiguration.getLong(path);
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    return def;
                }
            }
        }
        return fileConfiguration.getLong(path);
    }

    @Override
    public void setLong(String path, long value) {
        set(path, value);
    }

    @Override
    public boolean isDouble(String path) {
        return fileConfiguration.isDouble(path);
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0);
    }

    @Override
    public double getDouble(String path, double def) {
        return getDouble(path, def, true);
    }

    @Override
    public double getDouble(String path, double def, boolean setDefaultIfNoExist) {
        if (!contains(path)) {
            if (defaultFileConfiguration != null) {
                if (defaultFileConfiguration.contains(path)) {
                    return defaultFileConfiguration.getDouble(path);
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    return def;
                }
            }
        }
        return fileConfiguration.getDouble(path);
    }

    @Override
    public void setDouble(String path, double value) {
        set(path, value);
    }

    @Override
    public boolean isString(String path) {
        return fileConfiguration.isString(path);
    }

    @Override
    public String getString(String path) {
        return getString(path, new String[][]{});
    }

    public String getString(String path, String[][] replacements) {
        String result = getString(path);

        for (String[] replacement:replacements) {
            String key = replacement[0];
            String value = replacement[1];
            result = result.replaceAll(key, value);
        }

        return result;
    }

    @Override
    public String getString(String path, String def) {
        return getString(path, def, true);
    }

    public String getString(String path, String def, String[][] replacements) {
        return getString(path, def, false, replacements);
    }

    @Override
    public String getString(String path, String def, boolean setDefaultIfNoExist) {

        String result = "";

        if (!contains(path)) {
            if (defaultFileConfiguration != null) {
                if (defaultFileConfiguration.contains(path)) {
                    result = defaultFileConfiguration.getString(path);
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    result = def;
                }
            }
        } else {
            result = fileConfiguration.getString(path);
        }

        for (String key:getGlobalReplacements().keySet()) {
            result = result.replaceAll(key, getGlobalReplacements().get(key));
        }

        for (String key:getReplacements().keySet()) {
            result = result.replaceAll(key, getGlobalReplacements().get(key));
        }

        return result;
    }

    public String getString(String path, String def, boolean setDefaultIfNoExist, String[][] replacements) {
        String result = getString(path, def, setDefaultIfNoExist);

        for (String[] replacement:replacements) {
            String key = replacement[0];
            String value = replacement[1];
            result = result.replaceAll(key, value);
        }

        return result;
    }

    @Override
    public void setString(String path, String value) {
        set(path, value);
    }

    @Override
    public boolean isStringList(String path) {
        return fileConfiguration.isList(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return getStringList(path, new String[][]{});
    }

    public List<String> getStringList(String path, String[][] replacements) {
        List<String> result = getStringList(path);

        for (int i = 0; i < result.size(); i++) {

            String line = result.get(i);

            for (String[] replacement:replacements) {
                String key = replacement[0];
                String value = replacement[1];
                line = line.replaceAll(key, value);
            }

            result.set(i, line);

        }

        return result;
    }

    @Override
    public List<String> getStringList(String path, List<String> def) {
        return getStringList(path, def, false);
    }

    public List<String> getStringList(String path, List<String> def, String[][] replacements) {
        return getStringList(path, def, false, replacements);
    }

    @Override
    public List<String> getStringList(String path, List<String> def, boolean setDefaultIfNoExist) {
        List<String> result = new ArrayList<>();
        if (!contains(path)) {
            if (defaultFileConfiguration != null) {
                if (defaultFileConfiguration.contains(path)) {
                    result = defaultFileConfiguration.getStringList(path);
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    result = def;
                }
            }
        } else {
            result = fileConfiguration.getStringList(path);
        }

        for (int i = 0; i < result.size(); i++) {

            String line = result.get(i);

            for (String key:getGlobalReplacements().keySet()) {
                line = line.replaceAll(key, getGlobalReplacements().get(key));
            }

            for (String key:getReplacements().keySet()) {
                line = line.replaceAll(key, getGlobalReplacements().get(key));
            }

            result.set(i, line);

        }

        return result;
    }

    public List<String> getStringList(String path, List<String> def, boolean setDefaultIfNoExist, String[][] replacements) {
        List<String> result = getStringList(path, def, setDefaultIfNoExist);

        for (int i = 0; i < result.size(); i++) {

            String line = result.get(i);

            for (String[] replacement:replacements) {
                String key = replacement[0];
                String value = replacement[1];
                line = line.replaceAll(key, value);
            }

            result.set(i, line);

        }

        return result;
    }

    @Override
    public void setStringList(String path, List<String> value) {
        set(path, value);
    }

    @Override
    public boolean isLocation(String path) {
        if (isString(path)) {
            String location = getString(path);
            if (location.contains(":")) {
                return location.split(":").length == 6 || location.split(":").length == 4;
            }
        }
        return false;
    }

    @Override
    public Location getLocation(String path) {
        return getLocation(path, null);
    }

    @Override
    public Location getLocation(String path, Location def) {
        return getLocation(path, def, false);
    }

    @Override
    public Location getLocation(String path, Location def, boolean setDefaultIfNoExist) {
        Location location = getLocation(path);

        if (location == null) {

            setLocation(path, def);

            return def;
        }

        return location;
    }

    @Override
    public void setLocation(String path, Location value) {
        String world = value.getWorld().getName();
        double x = value.getX();
        double y = value.getY();
        double z = value.getZ();
        float yaw = value.getYaw();
        float pitch = value.getPitch();
        String result = world + ":" + x + ":" + y + ":" + z + ":" + yaw + ":" + pitch;
        set(path, result);
    }

    @Override
    public boolean isItemStack(String path) {

        if (!fileConfiguration.isConfigurationSection(path)) {
            return false;
        }

        return contains(path + ".material");

    }


    public ItemStack getCustomSkull(String url) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (url.isEmpty()) return head;

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        profile.getProperties().put("textures", new Property("textures", url));

        try {
            Method mtd = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            mtd.setAccessible(true);
            mtd.invoke(skullMeta, profile);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        head.setItemMeta(skullMeta);
        return head;
    }

    @Override
    public ItemStack getItemStack(String path) {
        return getItemStack(path, null);
    }

    @Override
    public ItemStack getItemStack(String path, ItemStack def) {
        ItemStack result = getItemStack(path, def, true);
        if (result == null) {
            return def;
        }
        return result;
    }

    @Override
    public ItemStack getItemStack(String path, ItemStack def, boolean setDefaultIfNoExist) {
        ItemStack result = getItemStack(path);
        if (result == null) {

            setItemStack(path, def);

            return def;
        }
        return result;
    }

    @Override
    public void setItemStack(String path, ItemStack value) {
        set(path + ".material", value.getType().toString());
        set(path + ".amount", value.getAmount());
        ItemMeta itemMeta = value.getItemMeta();
        if (itemMeta != null) {
            // DisplayName
            if (itemMeta.hasDisplayName()) {
                set(path + ".name", itemMeta.getDisplayName());
            }
            // Lore
            if (itemMeta.hasLore()) {
                set(path + ".lore", itemMeta.getLore());
            }
            // ItemFlag
            if (!itemMeta.getItemFlags().isEmpty()) {
                List<String> flags = new ArrayList<>();
                for (ItemFlag flag:itemMeta.getItemFlags()) {
                    flags.add(flag.toString());
                }
                set(path + ".flags", flags);
            }
            // Enchantments
            if (!value.getEnchantments().isEmpty()) {
                for (Enchantment enchantment:value.getEnchantments().keySet()) {
                    set(path + ".enchantments." + enchantment.getName(), value.getEnchantments().get(enchantment));
                }
            }
            if(!Objects.equals(Objects.requireNonNull(value.getData()).toString(), "0")){
                set(path + ".data", value.getData().getData());
            }
            if(value.getType().getMaxDurability() != value.getDurability()){
                set(path + ".durability", value.getDurability());
            }
            // ITEM NBTAPI
            NBTItem nbtItem = new NBTItem(value);
            for(String key : nbtItem.getKeys()){
                if(nbtItem.getType(key).equals(NBTType.NBTTagString)){
                    set(path + ".nbt." + key, nbtItem.getString(key));
                }else if(nbtItem.getType(key).equals(NBTType.NBTTagInt)){
                    set(path + ".nbt." + key, nbtItem.getInteger(key));
                }
            }
            // LEATHER ARMOR ITEM
            if (value.getType().equals(Objects.requireNonNull(XMaterial.matchXMaterial("LEATHER_HELMET").orElse(null)).parseMaterial())
                    || value.getType().equals(Objects.requireNonNull(XMaterial.matchXMaterial("LEATHER_CHESTPLATE").orElse(null)).parseMaterial())
                    || value.getType().equals(Objects.requireNonNull(XMaterial.matchXMaterial("LEATHER_LEGGINGS").orElse(null)).parseMaterial())
                    || value.getType().equals(Objects.requireNonNull(XMaterial.matchXMaterial("LEATHER_BOOTS").orElse(null)).parseMaterial())) {
                // color: "#E5E533"
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) value.getItemMeta();
                int color = leatherArmorMeta.getColor().asRGB();
                String colorString = String.valueOf(color);
                set(path + ".color", colorString.replaceAll("0x", "#"));
            }

            // SKULL TYPE
            if(value.getType().toString().equals("PLAYER_HEAD") || value.getType().toString().equals("LEGACY_SKULL_ITEM")){
                SkullMeta meta = (SkullMeta) value.getItemMeta();
                GameProfile profile = new GameProfile(UUID.randomUUID(), "");

                set(path + ".material", "basehead-" + profile.getProperties().get("textures"));
            }

        }
    }

}