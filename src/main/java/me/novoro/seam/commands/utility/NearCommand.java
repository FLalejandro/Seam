package me.novoro.seam.commands.utility;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.SettingsManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NearCommand extends CommandBase {
    public NearCommand() {
        super("near", "seam.near", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            checkNearbyPlayers(context.getSource().getPlayerOrThrow());
            return Command.SINGLE_SUCCESS;
        });
    }

    /*
     * Checks for nearby players within the configured radius.
     */
    private void checkNearbyPlayers(ServerPlayerEntity player) {
        int SEARCH_RADIUS = SettingsManager.getNearSearchRadius();

        BlockPos playerPos = player.getBlockPos();
        List<ServerPlayerEntity> nearbyPlayers = player.getServer().getPlayerManager().getPlayerList().stream()
                .filter(otherPlayer -> otherPlayer != player)
                .filter(otherPlayer -> otherPlayer.getBlockPos().isWithinDistance(playerPos, SEARCH_RADIUS))
                .collect(Collectors.toList());

        if (nearbyPlayers.isEmpty()) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("{radius}", String.valueOf(SEARCH_RADIUS));
            LangManager.sendLang(player, "Near-No-Players", replacements);
        } else {
            StringBuilder playerList = new StringBuilder();
            nearbyPlayers.forEach(otherPlayer -> {
                double distance = Math.sqrt(playerPos.getSquaredDistance(otherPlayer.getBlockPos()));
                String entry = LangManager.getLang("Near-Player-Entry")
                        .replace("{player}", otherPlayer.getName().getString())
                        .replace("{distance}", String.format("%.0f", distance));
                playerList.append(entry).append("\n");
            });

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{playerList}", playerList.toString().trim());
            LangManager.sendLang(player, "Near-Player-List", replacements);
        }
    }

}
