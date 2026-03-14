package me.novoro.seam.commands.utility;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

/**
 * Provides a command to broadcast messages to all players on the server.
 */
public class CheckTimeCommand extends CommandBase {
    public CheckTimeCommand() {
        super("checktime", "seam.checktime", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> checkTime(context.getSource().getPlayerOrThrow()));
    }

    /**
     * Checks and sends the current world time to the player.
     *
     * @return 1 if successful, 0 otherwise.
     */
    private static int checkTime(ServerPlayerEntity ctx) {

        String formattedTime = formatTime(ctx.getWorld().getTimeOfDay() % 24000);
        LangManager.sendLang(ctx, "Time-Check", Map.of("{world-time}", formattedTime));
        return 1;
    }

    /**
     * Converts the in-game time to a readable format.
     *
     * @param ticks The in-game time in ticks.
     * @return A formatted string representing the time.
     */
    private static String formatTime(long ticks) {
        long hours = (ticks / 1000 + 6) % 24;
        long minutes = (ticks % 1000) * 60 / 1000;
        long seconds = ((ticks % 1000) * 60 % 1000) * 60 / 1000;
        String ampm = hours < 12 ? "AM" : "PM";

        if (hours >= 12) hours -= 12;
        if (hours == 0) hours = 12;

        return String.format("%02d:%02d:%02d %s", hours, minutes, seconds, ampm);
    }
}
