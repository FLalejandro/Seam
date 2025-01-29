package me.novoro.seam;

import com.mojang.brigadier.CommandDispatcher;
import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.api.configuration.YamlConfiguration;
import me.novoro.seam.api.permissions.DefaultPermissionProvider;
import me.novoro.seam.api.permissions.LuckPermsPermissionProvider;
import me.novoro.seam.api.permissions.PermissionProvider;
import me.novoro.seam.commands.SeamReloadCommand;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.ModuleManager;
import me.novoro.seam.utils.SeamLogger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Seam implements ModInitializer {
    public static final String MOD_PREFIX = "&8&l[<gradient:#96B8C3:#7C93A2>&lSᴇᴀᴍ</gradient>&8&l]&f ";

    private static Seam instance;
    private MinecraftServer server;
    private PermissionProvider permissionProvider;

    private final ModuleManager moduleManager = new ModuleManager();
    private final LangManager langManager = new LangManager();

    @Override
    public void onInitialize() {
        Seam.instance = this;

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            this.checkPermissionProvider();
            this.reloadConfigs();
        });

        // Reloads modules on startup. Needs to be called before commands are registered.
        this.moduleManager.reload();

        // Registers all of Seam's commands.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> this.registerCommands(dispatcher));
    }

    // Reloads Seam's various configs.
    public void reloadConfigs() {
        this.langManager.reload();

        // ToDo: Reload our *other* configs lol
    }

    // Registers Seam's commands. Commands that are disabled are not registered.
    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new SeamReloadCommand().register(dispatcher);

        // ToDo: Register more commands!
    }

    /**
     * Gets Seam's current instance. It is not recommended to use externally.
     */
    public static Seam inst() {
        return Seam.instance;
    }

    /**
     * Gets the current {@link MinecraftServer} Seam is currently running on.
     */
    public static MinecraftServer getServer() {
        return Seam.instance.server;
    }

    /**
     * Gets the {@link PermissionProvider} Seam is currently using.
     */
    public static PermissionProvider getPermissionProvider() {
        return Seam.instance.permissionProvider;
    }

    /**
     * Sets what {@link PermissionProvider} Seam will use to handle all permissions.
     */
    public static void setPermissionProvider(PermissionProvider provider) {
        Seam.instance.permissionProvider = provider;
        SeamLogger.info("Registered " + provider.getName() + " as the server's permission provider.");
    }

    // Checks the server for the built-in permission providers.
    private void checkPermissionProvider() {
        try {
            Class.forName("net.luckperms.api.LuckPerms");
            this.permissionProvider = new LuckPermsPermissionProvider();
            SeamLogger.info("Found LuckPerms! Permission support enabled.");
            return;
        } catch (ClassNotFoundException ignored) {}
        this.permissionProvider = new DefaultPermissionProvider();
        SeamLogger.warn("Couldn't find a built in permission provider.. falling back to permission levels.");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getDataFolder() {
        File folder = FabricLoader.getInstance().getConfigDir().resolve("Seam").toFile();
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getFile(String fileName) {
        File file = new File(this.getDataFolder(), fileName);
        if (!file.exists()) file.getParentFile().mkdirs();
        return file;
    }

    public Configuration getConfig(String fileName, boolean saveResource) {
        File configFile = this.getFile(fileName);
        if (!configFile.exists()) {
            if (!saveResource) return null;
            this.saveResource(fileName, false);
        }
        return this.getConfig(configFile);
    }

    public Configuration getConfig(File configFile) {
        try {
            return YamlConfiguration.loadConfiguration(configFile); // ?
        } catch (IOException e) {
            SeamLogger.error("Something went wrong getting the config: " + configFile.getName() + ".");
            SeamLogger.printStackTrace(e);
        }
        return null;
    }

    public void saveConfig(String fileName, Configuration config) {
        File file = this.getFile(fileName);
        try {
            YamlConfiguration.save(config, file);
        } catch (IOException e) {
            SeamLogger.warn("Something went wrong saving the config: " + fileName + ".");
            SeamLogger.printStackTrace(e);
        }
    }

    @SuppressWarnings("resource")
    public void saveResource(String fileName, boolean overwrite) {
        File file = this.getFile(fileName);
        if (file.exists() && !overwrite) return;
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            Path path = Paths.get("configurations", fileName);
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(path.toString().replace("\\", "/"));
            assert in != null;
            in.transferTo(outputStream);
        } catch (IOException e) {
            SeamLogger.error("Something went wrong saving the resource: " + fileName + ".");
            SeamLogger.printStackTrace(e);
        }
    }
}
