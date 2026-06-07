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

//TODO: this has a bad glitch where viewing offline inventories wipes the enderchest. 
//future me use encoding
public class EnderchestCommand extends CommandBase {
    public EnderchestCommand() {
        super("enderchest", "seam.enderchest", 2, "ec");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            openEnderChest(player, player.getEnderChestInventory(), Text.translatable("container.enderchest"));
            LangManager.sendLang(context.getSource(), "Enderchest-Self-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", GameProfileArgumentType.gameProfile())
                .requires(source -> this.permission(source, "seam.enderchestview", 4) || this.permission(source, "seam.enderchestedit", 4))
                .executes(context -> {
                    ServerPlayerEntity sender = context.getSource().getPlayerOrThrow();
                    GameProfile target = GameProfileArgumentType.getProfileArgument(context, "target").iterator().next();
                    boolean canEdit = this.permission(context.getSource(), "seam.enderchestedit", 4);
                    ServerPlayerEntity onlineTarget = Objects.requireNonNull(sender.getServer()).getPlayerManager().getPlayer(target.getId());
                    if (onlineTarget != null) return openOnlineEnderChest(sender, onlineTarget, canEdit);
                    return openOfflineEnderChest(sender, target, canEdit);
                })
        );
    }

    private int openOnlineEnderChest(ServerPlayerEntity sender, ServerPlayerEntity target, boolean canEdit) {
        Text title = Text.of(target.getName().getString() + "'s Ender Chest");

        if (canEdit) {
            openEnderChest(sender, target.getEnderChestInventory(), title);
            LangManager.sendLang(sender, "Enderchest-Other-Edit-Message", Map.of("{player}", target.getName().getString()));
        } else {
            SimpleInventory viewOnly = copyToViewOnly(target.getEnderChestInventory());
            openEnderChest(sender, viewOnly, title);
            LangManager.sendLang(sender, "Enderchest-Other-View-Message", Map.of("{player}", target.getName().getString()));
        }

        return Command.SINGLE_SUCCESS;
    }

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
            SimpleInventory inventory = new SimpleInventory(27);
            inventory.readNbtList(enderItems, sender.getRegistryManager());

            sender.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, playerEntity) -> {
                        GenericContainerScreenHandler handler = new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, 3);
                        return handler;
                    },
                    title
            ));

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
            SimpleInventory viewOnly = new SimpleInventory(27);
            viewOnly.readNbtList(enderItems, sender.getRegistryManager());
            openEnderChest(sender, viewOnly, title);
            LangManager.sendLang(sender, "Enderchest-Other-View-Message", Map.of("{player}", target.getName()));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void openEnderChest(ServerPlayerEntity player, Inventory inventory, Text title) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, playerEntity) ->
                        new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, 3),
                title
        ));
    }

    private static SimpleInventory copyToViewOnly(EnderChestInventory source) {
        SimpleInventory copy = new SimpleInventory(source.size());
        for (int i = 0; i < source.size(); i++) {
            copy.setStack(i, source.getStack(i).copy());
        }
        return copy;
    }
}
