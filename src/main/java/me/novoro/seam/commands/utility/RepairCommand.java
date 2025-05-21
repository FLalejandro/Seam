package me.novoro.seam.commands.utility;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.SettingsManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Provides command to repair the item in the player's hand.
 */
public class RepairCommand extends CommandBase {
    public RepairCommand() {
        super("repair", "seam.repair", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            ItemStack heldItem = player.getMainHandStack();

            if (heldItem.isEmpty()) {
                LangManager.sendLang(player, "Repair-Hand-Empty-Message");
                return Command.SINGLE_SUCCESS;
            }

            if(SettingsManager.isRepairBlacklisted(heldItem)) {
                LangManager.sendLang(context.getSource(), "Repair-Blacklisted-Message");
                return Command.SINGLE_SUCCESS;
            }

            heldItem.setDamage(0);
            LangManager.sendLang(context.getSource(), "Repair-Success-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("all", EntityArgumentType.player())
                .requires(source -> this.permission(source, "seam.repairall", 4))
                .executes(context -> {
                    RepairCommand.repairInventory(context.getSource().getPlayerOrThrow());
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Repairs the inventory of the player.
     */
    private static void repairInventory(ServerPlayerEntity ctx) {
        // Repair main inventory items
        for (int i = 0; i < ctx.getInventory().size(); i++) {
            ItemStack stack = ctx.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.isDamageable() && !SettingsManager.isRepairBlacklisted(stack)) stack.setDamage(0);
        }

        // Repair armor slots
        for (int i = 0; i < ctx.getInventory().armor.size(); i++) {
            ItemStack armorStack = ctx.getInventory().armor.get(i);
            if (!armorStack.isEmpty() && armorStack.isDamageable() && !SettingsManager.isRepairBlacklisted(armorStack)) armorStack.setDamage(0);
        }

        // Repair off-hand slots
        ItemStack offHandStack = ctx.getInventory().offHand.get(0);
        if (!offHandStack.isEmpty() && offHandStack.isDamageable() && !SettingsManager.isRepairBlacklisted(offHandStack)) offHandStack.setDamage(0);

        LangManager.sendLang(ctx, "Repair-All-Success-Message");
    }
}