package me.novoro.seam.commands.utility;

import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.PlayerStorageManager;
import me.novoro.seam.objects.PlayerData;
import me.novoro.seam.utils.TimeUtil;
import me.novoro.seam.config.LangManager;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.Command;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;

public class SeenCommand extends CommandBase {
    public SeenCommand() {
        super("seen", "seam.seen", 2);
    }
    
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("player", string())
        .executes(context -> {
            String name = getString(context, "player");
            ServerCommandSource source = context.getSource();
            ServerPlayerEntity target = source.getPlayerOrThrow();
            
            if (target != null) {
                seenOnline(source, target);
                return Command.SINGLE_SUCCESS;
            }

            PlayerData data = PlayerStorageManager.findbyUsername(name);
            if (data == null) {
                    LangManager.sendLang(source, "Seen-Unknown", Map.of("player", name));
                    return 0;
            }
            
            seenOffline(source, data);
            return Command.SINGLE_SUCCESS;
        })

    );
    }

    private void seenOnline(ServerCommandSource source, ServerPlayerEntity target) {
        PlayerData data = PlayerStorageManager.get(target.getUuid());
        Map<String, String> replacements = Map.of(
            "{player}", target.getName().getString(),
            "{first-join}", TimeUtil.formatDate(data.firstJoin),
            "{last-join}", TimeUtil.formatDate(data.lastJoin)
        );
        LangManager.sendLang(source, "Seen-Online", replacements);
        if(this.permission(source, "seam.seen.ip", 2)) {
            String ip = target.getIp();
            LangManager.sendLang(source, "Seen-Online-IP", Map.of("{ip}", ip));
        }
    }

    // get IP for offline players. is it really worth storing in player data?
    private void seenOffline(ServerCommandSource source, PlayerData data) {
        String name = data.username != null ? data.username : data.uuid.toString();
        String lastSeen = data.lastJoin > 0 ? TimeUtil.executedTimeToFormatted(data.lastJoin) : "Unknown";
        Map<String, String> replacements = Map.of(
            "{player}", name,
            "{first-join}", TimeUtil.formatDate(data.firstJoin),
            "{last-seen}", lastSeen
        );
        LangManager.sendLang(source, "Seen-Offline", replacements);
    }
    
    
}
