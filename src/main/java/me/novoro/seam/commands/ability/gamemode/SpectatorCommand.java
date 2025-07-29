package me.novoro.seam.commands.ability.gamemode;

import net.minecraft.world.GameMode;

public class SpectatorCommand extends GamemodeCommand {
    public SpectatorCommand() {
        super("spectator", "minecraft.command.gamemode.spectator", GameMode.SPECTATOR, "gmsp");
    }
}