//package me.novoro.seam.commands.item;
//
//import com.mojang.brigadier.Command;
//import com.mojang.brigadier.arguments.StringArgumentType;
//import com.mojang.brigadier.builder.LiteralArgumentBuilder;
//import me.novoro.seam.config.LangManager;
//import me.novoro.seam.utils.ColorUtil;
//import net.minecraft.component.DataComponentTypes;
//import net.minecraft.component.type.LoreComponent;
//import net.minecraft.item.ItemStack;
//import net.minecraft.server.command.ServerCommandSource;
//import net.minecraft.text.Text;
//
//import java.util.List;
//import java.util.Map;
//
//public class ItemLoreCommand extends ItemCommand {
//    public ItemLoreCommand() {
//        super("itemlore", "seam.itemlore", 0, "ilore", "lore");
//    }
//
//    @Override
//    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
//        return command.then(argument("name", StringArgumentType.greedyString())
//                .executes(context -> {
//                    ItemStack item = this.getItem(context);
//                    if (item == null) return 0;
//                    String name = context.getArgument("name", String.class);
//                    LangManager.sendLang(context.getSource(), "ItemName-Success", Map.of(
//                            "{name}", name,
//                            "{item}", ColorUtil.serialize(item.getName()))
//                    );
//                    item.set(DataComponentTypes.ITEM_NAME, ColorUtil.parseColourToText(name));
//                    return Command.SINGLE_SUCCESS;
//                })).then(literal("reset").executes(context -> {
//                    ItemStack item = this.getItem(context);
//                    if (item == null) return 0;
//                    item.set(DataComponentTypes.ITEM_NAME, null);
//                    LangManager.sendLang(context.getSource(), "ItemName-Reset", Map.of(
//                            "{item}", ColorUtil.serialize(item.getName()))
//                    );
//                    return Command.SINGLE_SUCCESS;
//                })
//        );
//    }
//
//    public static void setLore(ItemStack item, List<String> lore) {
//        if (lore == null || lore.stream().allMatch(String::isBlank)) {
//            item.set(DataComponentTypes.LORE, null);
//            return;
//        }
//        List<Text> loreText = lore.stream().map(ColorUtil::parseColourToText).toList();
//        item.set(DataComponentTypes.LORE, new LoreComponent(loreText));
//    }
//}
