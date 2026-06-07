package me.novoro.seam.commands.teleportation.homes;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.PlayerStorageManager;
import me.novoro.seam.config.SettingsManager;
import me.novoro.seam.objects.Home;
import me.novoro.seam.objects.PlayerData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
 
import java.util.Map;

//TODO: Hook this up to lp meta perms
public class SetHomeCommand extends CommandBase {
    public SetHomeCommand() {
        super("sethome", "seam.sethome", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("home-name", StringArgumentType.string())
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String homeName = StringArgumentType.getString(context, "home-name");
                    PlayerData data = PlayerStorageManager.get(player.getUuid());

                    // saying this out loud made me delirious
                    Home home = new Home(player, homeName);
                    data.setHome(homeName, home);
                    PlayerStorageManager.save(player.getUuid());
                    
                    LangManager.sendLang(player, "Home-Set", Map.of(
                            "{home}", homeName,
                            "{x}", String.valueOf((int) player.getX()),
                            "{y}", String.valueOf((int) player.getY()),
                            "{z}", String.valueOf((int) player.getZ()),
                            "{world}", player.getWorld().getRegistryKey().getValue().toString()
                    ));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

}
