package me.novoro.seam.commands.item;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.utils.StringUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class ItemCommand extends CommandBase {
    public ItemCommand(String command, String permission, int permissionLevel, String... aliases) {
        super(command, permission, permissionLevel, aliases);
    }

    protected ItemStack getItem(CommandContext<ServerCommandSource> context, boolean sendLang) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ItemStack item = player.getStackInHand(Hand.MAIN_HAND);
        if (!item.isEmpty()) return item;
        if (sendLang) LangManager.sendLang(player, "Item-No-Item-In-Hand");
        return null;
    }

    protected CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder,
                                                            String arg, Function<ItemStack, String> completionFunction) throws CommandSyntaxException {
        ItemStack item = this.getItem(context, false);
        if (item == null) return builder.buildFuture();
        String input = "";
        try {
            input = context.getArgument(arg, String.class);
        } catch (IllegalArgumentException ignored) {}
        String completion = completionFunction.apply(item);
        if (StringUtil.startsWith(input, completion)) builder.suggest(completion);
        return builder.buildFuture();
    }
}
