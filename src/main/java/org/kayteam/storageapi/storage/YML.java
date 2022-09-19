package org.kayteam.storageapi.storage;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTType;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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

public class YML extends Storage {

    private final static HashMap<String, String> globalReplacements = new HashMap<>();
    private final HashMap<String, String> replacements = new HashMap<>();

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
                    Object defaultFileValue = defaultFileConfiguration.get(path);
                    set(path, defaultFileValue);
                    save();
                    return defaultFileValue;
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    save();
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
                    boolean defaultFileValue = defaultFileConfiguration.getBoolean(path);
                    set(path, defaultFileValue);
                    save();
                    return defaultFileValue;
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    save();
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
                    int defaultFileValue = defaultFileConfiguration.getInt(path);
                    set(path, defaultFileValue);
                    save();
                    return defaultFileValue;
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    save();
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
                    long defaultFileValue = defaultFileConfiguration.getLong(path);
                    set(path, defaultFileValue);
                    save();
                    return defaultFileValue;
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    save();
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
                    double defaultFileValue = defaultFileConfiguration.getDouble(path);
                    set(path, defaultFileValue);
                    save();
                    return defaultFileValue;
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    save();
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
        return getString( path , "" , replacements );
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
        return getString( path , def , setDefaultIfNoExist , new String[][]{} );
    }

    public String getString(String path, String def, boolean setDefaultIfNoExist, String[][] replacements) {

        if (!contains(path)) {
            if (defaultFileConfiguration != null) {
                if (defaultFileConfiguration.contains(path)) {
                    String defaultFileValue = defaultFileConfiguration.getString(path);
                    set(path, defaultFileValue);
                    save();
                    return defaultFileValue;
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    save();
                    return def;
                }
            }
        }

        String result = fileConfiguration.getString( path );

        for (String[] replacement:replacements) {
            String key = replacement[0];
            String value = replacement[1];
            result = result.replaceAll(key, value);
        }

        for (String key:getGlobalReplacements().keySet()) {
            result = result.replaceAll(key, getGlobalReplacements().get(key));
        }

        for (String key:getReplacements().keySet()) {
            result = result.replaceAll(key, getGlobalReplacements().get(key));
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
        return getStringList(path, new ArrayList<>(), replacements);
    }

    @Override
    public List<String> getStringList(String path, List<String> def) {
        return getStringList(path, def, true);
    }

    public List<String> getStringList(String path, List<String> def, String[][] replacements) {
        return getStringList(path, def, true, replacements);
    }

    @Override
    public List<String> getStringList(String path, List<String> def, boolean setDefaultIfNoExist) {
        return getStringList(path, def, setDefaultIfNoExist, new String[][]{});
    }

    public List<String> getStringList(String path, List<String> def, boolean setDefaultIfNoExist, String[][] replacements) {
        List<String> result = new ArrayList<>();
        if (!contains(path)) {
            if (defaultFileConfiguration != null) {
                if (defaultFileConfiguration.contains(path)) {
                    result = defaultFileConfiguration.getStringList(path);
                    set(path, result);
                    save();
                } else {
                    if (setDefaultIfNoExist) set(path, def);
                    save();
                    result = def;
                }
            }
        } else {
            result = fileConfiguration.getStringList(path);
        }

        for (int i = 0; i < result.size(); i++) {

            String line = result.get(i);

            for ( String[] replacement : replacements ) {
                line = line.replaceAll( replacement[0] , replacement[1] );
            }

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
        return getLocation(path, def, true);
    }

    @Override
    public Location getLocation(String path, Location def, boolean setDefaultIfNoExist) {
        String locationString = getString(path);

        Location location = null;

        if (locationString.split(":").length == 4) {
            try {
                World world = Bukkit.getWorld(locationString.split(":")[0]);
                double x = Double.parseDouble(locationString.split(":")[1]);
                double y = Double.parseDouble(locationString.split(":")[2]);
                double z = Double.parseDouble(locationString.split(":")[3]);
                location = new Location(world, x, y, z);
            } catch (NumberFormatException | NullPointerException | PatternSyntaxException e) {
                e.printStackTrace();
            }
        }

        if (locationString.split(":").length == 6) {
            try {
                World world = Bukkit.getWorld(locationString.split(":")[0]);
                double x = Double.parseDouble(locationString.split(":")[1]);
                double y = Double.parseDouble(locationString.split(":")[2]);
                double z = Double.parseDouble(locationString.split(":")[3]);
                float yaw = Float.parseFloat(locationString.split(":")[4]);
                float pitch = Float.parseFloat(locationString.split(":")[5]);
                location = new Location(world, x, y, z, yaw, pitch);
            } catch (NumberFormatException | NullPointerException | PatternSyntaxException e) {
                e.printStackTrace();
            }
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
        ItemStack result = null;

        if (!contains(path)) {
            if (defaultFileConfiguration.contains(path)) {
                // Amount
                int amount = defaultFileConfiguration.getInt(path + ".amount", 1);

                if (defaultFileConfiguration.getString(path + ".material").startsWith("basehead-")) {

                    XMaterial xMaterial = XMaterial.matchXMaterial("PLAYER_HEAD").orElse(null);

                    Material material = xMaterial.parseMaterial();

                    // MaterialData
                    short data = -1;
                    if (defaultFileConfiguration.contains(path + ".data")) {
                        if (defaultFileConfiguration.isInt(path + ".data")) {
                            data = (short) defaultFileConfiguration.getInt(path + ".data");
                        }
                    } else{
                        data = xMaterial.getData();
                    }

                    if (material != null) {
                        if (data != -1) {
                            result = new ItemStack(material, amount, data);
                        } else {
                            result = new ItemStack(material, amount);
                        }
                    }

                    String value = defaultFileConfiguration.getString(path + ".material").replaceFirst("basehead-", "");

                    SkullMeta skullMeta = (SkullMeta) result.getItemMeta();
                    GameProfile profile = new GameProfile(UUID.randomUUID(), null);

                    profile.getProperties().put("textures", new Property("textures", value));

                    try {
                        Method mtd = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                        mtd.setAccessible(true);
                        mtd.invoke(skullMeta, profile);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                    }

                    result.setItemMeta(skullMeta);

                } else if (defaultFileConfiguration.getString(path + ".material").startsWith("playerhead-")) {

                    XMaterial xMaterial = XMaterial.matchXMaterial("PLAYER_HEAD").orElse(null);
                    assert xMaterial != null;
                    Material material = xMaterial.parseMaterial();

                    // MaterialData
                    short data = -1;
                    if (defaultFileConfiguration.contains(path + ".data")) {
                        if (defaultFileConfiguration.isInt(path + ".data")) {
                            data = (short) defaultFileConfiguration.getInt(path + ".data");
                        }
                    } else{
                        data = xMaterial.getData();
                    }

                    if (material != null) {
                        if (data != -1) {
                            result = new ItemStack(material, amount, data);
                        } else {
                            result = new ItemStack(material, amount);
                        }
                    }

                    SkullMeta skullMeta = (SkullMeta) result.getItemMeta();
                    skullMeta.setOwner(defaultFileConfiguration.getString(path + ".material").replaceFirst("playerhead-", ""));
                    result.setItemMeta(skullMeta);

                } else {
                    XMaterial xMaterial = XMaterial.matchXMaterial(defaultFileConfiguration.getString(path + ".material")).orElse(null);
                    assert xMaterial != null;
                    Material material = xMaterial.parseMaterial();

                    // MaterialData
                    short data = -1;
                    if (defaultFileConfiguration.contains(path + ".data")) {
                        if (defaultFileConfiguration.isInt(path + ".data")) {
                            data = (short) defaultFileConfiguration.getInt(path + ".data");
                        }
                    } else{
                        data = xMaterial.getData();
                    }

                    if (material != null) {
                        if (data != -1) {
                            result = new ItemStack(material, amount, data);
                        } else {
                            result = new ItemStack(material, amount);
                        }
                    }
                }

                if (result != null) {
                    ItemMeta itemMeta = result.getItemMeta();
                    if (itemMeta != null) {
                        // DisplayName
                        if (defaultFileConfiguration.contains(path + ".name")) {
                            if (defaultFileConfiguration.isString(path + ".name")) {
                                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', defaultFileConfiguration.getString(path + ".name")));
                            }
                        }
                        // Lore
                        if (defaultFileConfiguration.contains(path + ".lore")) {
                            if (defaultFileConfiguration.isList(path + ".lore")) {
                                List<String> lore = defaultFileConfiguration.getStringList(path + ".lore");
                                lore.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));
                                itemMeta.setLore(lore);
                            }
                        }
                        // ItemFlag
                        if (defaultFileConfiguration.contains(path + ".flags")) {
                            if (defaultFileConfiguration.isList(path + ".flags")) {
                                List<String> flags = defaultFileConfiguration.getStringList(path + ".flags");
                                for (String flag:flags) {
                                    ItemFlag itemFlag = ItemFlag.valueOf(flag);
                                    itemMeta.addItemFlags(itemFlag);
                                }
                            }
                        }
                    }
                    result.setItemMeta(itemMeta);
                    // Enchantments
                    if (defaultFileConfiguration.contains(path + ".enchantments")) {
                        Set<String> names = Objects.requireNonNull(defaultFileConfiguration.getConfigurationSection(path + ".enchantments")).getValues(false).keySet();
                        for (String name:names) {
                            Enchantment enchantment = Enchantment.getByName(name);
                            if (enchantment != null) {
                                result.addUnsafeEnchantment(enchantment, defaultFileConfiguration.getInt(path + ".enchantments." + name));
                            }
                        }
                    }
                    // Durability
                    if (defaultFileConfiguration.contains(path + ".durability")) {
                        if (defaultFileConfiguration.isInt(path + ".durability")) {
                            result.setDurability((short) defaultFileConfiguration.getInt(path + ".durability"));
                        }
                    }
                    // ITEM NBTAPI
                    if (defaultFileConfiguration.contains(path + ".nbt")){
                        NBTItem nbtItem = new NBTItem(result);
                        for(String key : Objects.requireNonNull(defaultFileConfiguration.getConfigurationSection(path + ".nbt")).getKeys(false)){
                            try{
                                if(defaultFileConfiguration.isString(path + ".nbt." + key)){
                                    nbtItem.setString(key, defaultFileConfiguration.getString(path + ".nbt." + key));
                                }else if(defaultFileConfiguration.isInt(path + ".nbt." + key)){
                                    nbtItem.setInteger(key, defaultFileConfiguration.getInt(path + ".nbt." + key));
                                }else{
                                    Bukkit.getLogger().log(Level.SEVERE, "An error has occurred trying load NBT: "+key+". Please enter a valid type: STRING/INTEGER.");
                                }
                            }catch (Exception e){
                                Bukkit.getLogger().log(Level.SEVERE, "An error has occurred trying load NBT: "+key);
                            }
                        }
                        result = nbtItem.getItem();
                    }
                    // LEATHER ARMOR ITEM
                    if (result.getType().equals(Material.valueOf("LEATHER_HELMET"))
                            || result.getType().equals(Material.valueOf("LEATHER_CHESTPLATE"))
                            || result.getType().equals(Material.valueOf("LEATHER_LEGGINGS"))
                            || result.getType().equals(Material.valueOf("LEATHER_BOOTS"))) {
                        if (contains(path + ".color") && isString(path + ".color")) {
                            // color: "#E5E533"
                            String colorString =defaultFileConfiguration. getString(path + ".color").replaceAll("#", "0x");
                            int color = Integer.parseInt(colorString);
                            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) result.getItemMeta();
                            leatherArmorMeta.setColor(Color.fromRGB(color));
                            result.setItemMeta(leatherArmorMeta);
                        }
                    }
                }
            }
        } else {
            // Amount
            int amount = getInt(path + ".amount", 1);

            if (getString(path + ".material").startsWith("basehead-")) {

                XMaterial xMaterial = XMaterial.matchXMaterial("PLAYER_HEAD").orElse(null);

                Material material = xMaterial.parseMaterial();

                // MaterialData
                short data = -1;
                if (contains(path + ".data")) {
                    if (isInt(path + ".data")) {
                        data = (short) getInt(path + ".data");
                    }
                } else{
                    data = xMaterial.getData();
                }

                if (material != null) {
                    if (data != -1) {
                        result = new ItemStack(material, amount, data);
                    } else {
                        result = new ItemStack(material, amount);
                    }
                }

                String value = getString(path + ".material").replaceFirst("basehead-", "");

                SkullMeta skullMeta = (SkullMeta) result.getItemMeta();
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);

                profile.getProperties().put("textures", new Property("textures", value));

                try {
                    Method mtd = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                    mtd.setAccessible(true);
                    mtd.invoke(skullMeta, profile);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                    ex.printStackTrace();
                }

                result.setItemMeta(skullMeta);

            } else if (getString(path + ".material").startsWith("playerhead-")) {

                XMaterial xMaterial = XMaterial.matchXMaterial("PLAYER_HEAD").orElse(null);
                assert xMaterial != null;
                Material material = xMaterial.parseMaterial();

                // MaterialData
                short data = -1;
                if (contains(path + ".data")) {
                    if (isInt(path + ".data")) {
                        data = (short) getInt(path + ".data");
                    }
                } else{
                    data = xMaterial.getData();
                }

                if (material != null) {
                    if (data != -1) {
                        result = new ItemStack(material, amount, data);
                    } else {
                        result = new ItemStack(material, amount);
                    }
                }

                SkullMeta skullMeta = (SkullMeta) result.getItemMeta();
                skullMeta.setOwner(getString(path + ".material").replaceFirst("playerhead-", ""));
                result.setItemMeta(skullMeta);

            } else {
                XMaterial xMaterial = XMaterial.matchXMaterial(getString(path + ".material")).orElse(null);
                assert xMaterial != null;
                Material material = xMaterial.parseMaterial();

                // MaterialData
                short data = -1;
                if (contains(path + ".data")) {
                    if (isInt(path + ".data")) {
                        data = (short) getInt(path + ".data");
                    }
                } else{
                    data = xMaterial.getData();
                }

                if (material != null) {
                    if (data != -1) {
                        result = new ItemStack(material, amount, data);
                    } else {
                        result = new ItemStack(material, amount);
                    }
                }
            }

            if (result != null) {
                ItemMeta itemMeta = result.getItemMeta();
                if (itemMeta != null) {
                    // DisplayName
                    if (contains(path + ".name")) {
                        if (isString(path + ".name")) {
                            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getString(path + ".name")));
                        }
                    }
                    // Lore
                    if (contains(path + ".lore")) {
                        if (isStringList(path + ".lore")) {
                            List<String> lore = getStringList(path + ".lore");
                            lore.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));
                            itemMeta.setLore(lore);
                        }
                    }
                    // ItemFlag
                    if (contains(path + ".flags")) {
                        if (isStringList(path + ".flags")) {
                            List<String> flags = getStringList(path + ".flags");
                            for (String flag:flags) {
                                ItemFlag itemFlag = ItemFlag.valueOf(flag);
                                itemMeta.addItemFlags(itemFlag);
                            }
                        }
                    }
                }
                result.setItemMeta(itemMeta);
                // Enchantments
                if (contains(path + ".enchantments")) {
                    Set<String> names = Objects.requireNonNull(fileConfiguration.getConfigurationSection(path + ".enchantments")).getValues(false).keySet();
                    for (String name:names) {
                        Enchantment enchantment = Enchantment.getByName(name);
                        if (enchantment != null) {
                            result.addUnsafeEnchantment(enchantment, getInt(path + ".enchantments." + name));
                        }
                    }
                }
                // Durability
                if (contains(path + ".durability")) {
                    if (isInt(path + ".durability")) {
                        result.setDurability((short) getInt(path + ".durability"));
                    }
                }
                // ITEM NBTAPI
                if (contains(path + ".nbt")){
                    NBTItem nbtItem = new NBTItem(result);
                    for(String key : Objects.requireNonNull(fileConfiguration.getConfigurationSection(path + ".nbt")).getKeys(false)){
                        try{
                            if(isString(path + ".nbt." + key)){
                                nbtItem.setString(key, getString(path + ".nbt." + key));
                            }else if(isInt(path + ".nbt." + key)){
                                nbtItem.setInteger(key, getInt(path + ".nbt." + key));
                            }else{
                                Bukkit.getLogger().log(Level.SEVERE, "An error has occurred trying load NBT: "+key+". Please enter a valid type: STRING/INTEGER.");
                            }
                        }catch (Exception e){
                            Bukkit.getLogger().log(Level.SEVERE, "An error has occurred trying load NBT: "+key);
                        }
                    }
                    result = nbtItem.getItem();
                }
                // LEATHER ARMOR ITEM
                if (result.getType().equals(Material.valueOf("LEATHER_HELMET"))
                        || result.getType().equals(Material.valueOf("LEATHER_CHESTPLATE"))
                        || result.getType().equals(Material.valueOf("LEATHER_LEGGINGS"))
                        || result.getType().equals(Material.valueOf("LEATHER_BOOTS"))) {
                    if (contains(path + ".color") && isString(path + ".color")) {
                        // color: "#E5E533"
                        String colorString = getString(path + ".color").replaceAll("#", "0x");
                        int color = Integer.parseInt(colorString);
                        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) result.getItemMeta();
                        leatherArmorMeta.setColor(Color.fromRGB(color));
                        result.setItemMeta(leatherArmorMeta);
                    }
                }
            }

        }

        return result;
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

    public void sendMessage(CommandSender commandSender, String path) {
        sendMessage(commandSender, path, new String[][]{});
    }

    public void sendMessage(CommandSender commandSender, String path, String[][] replacements) {
        List<String> messages = new ArrayList<>();
        if (isStringList(path)) {
            messages = getStringList(path, replacements);
        } else if (isString(path)){
            messages.add(getString(path, replacements));
        }
        for (String message : messages) {
            // Local replacements
            for (String text : this.replacements.keySet()) {
                message = message.replaceAll("%" + text + "%", this.replacements.get(text));
            }
            // Global replacements
            for (String text : YML.globalReplacements.keySet()) {
                message = message.replaceAll("%" + text + "%", YML.globalReplacements.get(text));
            }
            // PlaceholderAPI
            if (commandSender instanceof Player && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                message = PlaceholderAPI.setPlaceholders((Player) commandSender, message);
            }
            // Add color
            message = ChatColor.translateAlternateColorCodes('&', message);
            // Send message
            commandSender.sendMessage(message);
        }
    }

    public static void sendSimpleMessage(CommandSender commandSender, Object message) {
        sendSimpleMessage(commandSender, message, new String[][] {});
    }

    public static void sendSimpleMessage(CommandSender commandSender, Object message, String[][] replacements) {
        List<String> messages = new ArrayList<>();
        if (message.getClass().getSimpleName().equals("ArrayList")) {
            messages = (List<String>) message;
        } else if (message.getClass().getSimpleName().equals("String")){
            messages.add((String) message);
        }
        for (String m : messages) {
            // Replacements
            for (String[] replacement : replacements) {
                m = m.replaceAll(replacement[0], replacement[1]);
            }
            // Global replacements
            for (String text : YML.globalReplacements.keySet()) {
                m = m.replaceAll("%" + text + "%", YML.globalReplacements.get(text));
            }
            // PlaceholderAPI
            if (commandSender instanceof Player && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                m = PlaceholderAPI.setPlaceholders((Player) commandSender, m);
            }
            // Add color
            m = ChatColor.translateAlternateColorCodes('&', m);
            // Send message
            commandSender.sendMessage(m);
        }
    }

}