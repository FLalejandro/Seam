package me.novoro.seam.commands.fun;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.SettingsManager;
import me.novoro.seam.utils.ColorUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

/**
 * Provides command to allow players to place their held item onto their head.
 */
public class HatCommand extends CommandBase {
    public HatCommand() {
        super("hat", "seam.hat", 2, "head");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {

            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            PlayerInventory playerInv = player.getInventory();
            int selectedSlot = playerInv.selectedSlot;
            ItemStack heldItem = player.getMainHandStack();

            if (heldItem.isEmpty()) {
                LangManager.sendLang(player, "Hat-Hand-Empty-Message");
                return Command.SINGLE_SUCCESS;
            }

            if (SettingsManager.isHatBlacklisted(heldItem)) {
                LangManager.sendLang(context.getSource(), "Hat-Blacklisted-Message");
                return Command.SINGLE_SUCCESS;
            }

            ItemStack playerHelmet = playerInv.getArmorStack(3);
            playerInv.armor.set(3, heldItem);
            playerInv.main.set(selectedSlot, playerHelmet);
            LangManager.sendLang(player, "Hat-Equipped-Message", Map.of("{item}", ColorUtil.serialize(heldItem.getName())));
            return Command.SINGLE_SUCCESS;
        });
    }
}
