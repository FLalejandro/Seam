package me.novoro.seam.commands.inventory;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.utils.GameProfileUtil;
import me.novoro.seam.utils.SeamLogger;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Provides command to open a player's ender chest.
 * <ul>
 *   <li>/enderchest - Opens your own ender chest.</li>
 *   <li>/enderchest (online target) - Opens the target's ender chest (edit or view-only based on permission).</li>
 *   <li>/enderchest (offline target) - Loads the target's ender chest from playerdata NBT.</li>
 * </ul>
 *
 * Permissions:
 * <ul>
 *   <li>seam.enderchest - Base permission to open your own ender chest.</li>
 *   <li>seam.enderchestedit - Permission to edit another player's ender chest.</li>
 *   <li>seam.enderchestview - Permission to view (read-only) another player's ender chest.</li>
 * </ul>
 */
public class EnderchestCommand extends CommandBase {
    public EnderchestCommand() {
        super("enderchest", "seam.enderchest", 2, "ec");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command
                // /enderchest - Opens your own ender chest
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    openEnderChest(player, player.getEnderChestInventory(), Text.translatable("container.enderchest"));
                    LangManager.sendLang(context.getSource(), "Enderchest-Self-Message");
                    return Command.SINGLE_SUCCESS;
                })
                // /enderchest <target> - Opens target's ender chest (online or offline)
                .then(argument("target", GameProfileArgumentType.gameProfile())
                        .requires(source -> this.permission(source, "seam.enderchestview", 4) || this.permission(source, "seam.enderchestedit", 4))
                        .executes(context -> {
                            ServerPlayerEntity sender = context.getSource().getPlayerOrThrow();
                            GameProfile target = GameProfileArgumentType.getProfileArgument(context, "target").iterator().next();
                            boolean canEdit = this.permission(context.getSource(), "seam.enderchestedit", 4);

                            // Check if the target is online
                            ServerPlayerEntity onlineTarget = Objects.requireNonNull(sender.getServer()).getPlayerManager().getPlayer(target.getId());
                            if (onlineTarget != null) {
                                return openOnlineEnderChest(sender, onlineTarget, canEdit);
                            }

                            // Target is offline — load from playerdata
                            return openOfflineEnderChest(sender, target, canEdit);
                        })
                );
    }

    /**
     * Opens an online player's ender chest for the sender.
     */
    private int openOnlineEnderChest(ServerPlayerEntity sender, ServerPlayerEntity target, boolean canEdit) {
        Text title = Text.of(target.getName().getString() + "'s Ender Chest");

        if (canEdit) {
            // Direct access to the target's live ender chest inventory
            openEnderChest(sender, target.getEnderChestInventory(), title);
            LangManager.sendLang(sender, "Enderchest-Other-Edit-Message", Map.of("{player}", target.getName().getString()));
        } else {
            // View-only: copy contents into a read-only screen
            SimpleInventory viewOnly = copyToViewOnly(target.getEnderChestInventory());
            openEnderChest(sender, viewOnly, title);
            LangManager.sendLang(sender, "Enderchest-Other-View-Message", Map.of("{player}", target.getName().getString()));
        }

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Opens an offline player's ender chest by loading their playerdata NBT.
     */
    private int openOfflineEnderChest(ServerPlayerEntity sender, GameProfile target, boolean canEdit) {
        NbtCompound nbt;
        try {
            nbt = GameProfileUtil.getOfflineData(target);
        } catch (IOException e) {
            SeamLogger.warn("Failed to load offline data for " + target.getName() + ":");
            SeamLogger.printStackTrace(e);
            LangManager.sendLang(sender, "Enderchest-Offline-Fail", Map.of("{player}", target.getName()));
            return Command.SINGLE_SUCCESS;
        }

        if (nbt == null) {
            LangManager.sendLang(sender, "Enderchest-Offline-Fail", Map.of("{player}", target.getName()));
            return Command.SINGLE_SUCCESS;
        }

        NbtList enderItems = nbt.getList("EnderItems", NbtElement.COMPOUND_TYPE);
        Text title = Text.of(target.getName() + "'s Ender Chest");

        if (canEdit) {
            // Editable: load into a SimpleInventory, save back on close
            SimpleInventory inventory = new SimpleInventory(27);
            inventory.readNbtList(enderItems, sender.getRegistryManager());

            sender.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, playerEntity) -> {
                        GenericContainerScreenHandler handler = new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, 3);
                        return handler;
                    },
                    title
            ));

            // Save changes back when the screen is closed
            inventory.addListener(inv -> {
                NbtList updatedItems = inventory.toNbtList(sender.getRegistryManager());
                nbt.put("EnderItems", updatedItems);
                try {
                    GameProfileUtil.saveOfflineData(target, nbt);
                } catch (IOException e) {
                    SeamLogger.warn("Failed to save offline ender chest for " + target.getName() + ":");
                    SeamLogger.printStackTrace(e);
                }
            });

            LangManager.sendLang(sender, "Enderchest-Other-Edit-Message", Map.of("{player}", target.getName()));
        } else {
            // View-only: load into a SimpleInventory but don't persist
            SimpleInventory viewOnly = new SimpleInventory(27);
            viewOnly.readNbtList(enderItems, sender.getRegistryManager());
            openEnderChest(sender, viewOnly, title);
            LangManager.sendLang(sender, "Enderchest-Other-View-Message", Map.of("{player}", target.getName()));
        }

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Opens a generic 9x3 container screen backed by the given inventory.
     */
    private static void openEnderChest(ServerPlayerEntity player, Inventory inventory, Text title) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, playerEntity) ->
                        new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, 3),
                title
        ));
    }

    /**
     * Copies the contents of an ender chest into a read-only {@link SimpleInventory}.
     */
    private static SimpleInventory copyToViewOnly(EnderChestInventory source) {
        SimpleInventory copy = new SimpleInventory(source.size());
        for (int i = 0; i < source.size(); i++) {
            copy.setStack(i, source.getStack(i).copy());
        }
        return copy;
    }
}
