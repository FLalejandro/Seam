package me.novoro.seam.commands.item;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.utils.ColorUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;

public class ItemNameCommand extends ItemCommand {
    public ItemNameCommand() {
        super("itemname", "seam.itemname", 0, "iname");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("name", StringArgumentType.greedyString())
                .suggests((context, builder) -> this.getSuggestions(context, builder, "name", item ->
                        ColorUtil.serialize(item.getName()).replaceFirst("<!italic>", ""))
                ).executes(context -> {
                    ItemStack item = this.getItem(context, true);
                    if (item == null) return 0;
                    String name = context.getArgument("name", String.class);
                    LangManager.sendLang(context.getSource(), "ItemName-Success", Map.of(
                            "{name}", name,
                            "{item}", ColorUtil.serialize(item.getName()))
                    );
                    item.set(DataComponentTypes.ITEM_NAME, ColorUtil.parseColourToText(name));
                    return Command.SINGLE_SUCCESS;
                })).then(literal("reset").executes(context -> {
                    ItemStack item = this.getItem(context, true);
                    if (item == null) return 0;
                    item.set(DataComponentTypes.ITEM_NAME, null);
                    LangManager.sendLang(context.getSource(), "ItemName-Reset", Map.of(
                            "{item}", ColorUtil.serialize(item.getName()))
                    );
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
