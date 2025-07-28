package me.novoro.seam.config;

import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.api.configuration.VersionedConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.List;

public final class SettingsManager extends VersionedConfig {
    private static boolean feedFillsSaturation;
    private static boolean clearInventoryRequiresConfirmation;
    private static List<String> hatBlacklist;
    private static List<String> repairBlacklist;

    @Override
    protected void reload(Configuration settingsConfig) {
        super.reload(settingsConfig);
        SettingsManager.feedFillsSaturation = settingsConfig.getBoolean("Feed-Fills-Saturation");
        SettingsManager.clearInventoryRequiresConfirmation = settingsConfig.getBoolean("ClearInventory-Requires-Confirmation");
        SettingsManager.hatBlacklist = settingsConfig.getStringList("Hat.Blacklisted-Items");
        SettingsManager.repairBlacklist = settingsConfig.getStringList("Repair.Blacklisted-Items");
    }

    public static boolean feedFillsSaturation() {
        return SettingsManager.feedFillsSaturation;
    }

    public static boolean clearInventoryRequiresConfirmation() {
        return SettingsManager.clearInventoryRequiresConfirmation;
    }

    public static boolean isHatBlacklisted(ItemStack item) {
        return checkBlacklist(item, hatBlacklist);
    }

    public static boolean isRepairBlacklisted(ItemStack item) {
        return checkBlacklist(item, repairBlacklist);
    }

    private static boolean checkBlacklist(ItemStack item, List<String> blacklist) {
        if (blacklist.isEmpty()) return false;

        final String itemId = Registries.ITEM.getId(item.getItem()).toString();
        CustomModelDataComponent customModelDataComponent = item.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        int customModelData = customModelDataComponent != null ? customModelDataComponent.value() : 0;

        return blacklist.contains(itemId) || blacklist.contains(itemId + ":" + customModelData);
    }

    @Override
    public double getCurrentConfigVersion() {
        return 1.0;
    }

    @Override
    protected String getConfigFileName() {
        return "settings.yml";
    }
}