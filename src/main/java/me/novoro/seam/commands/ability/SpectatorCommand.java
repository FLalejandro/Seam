package me.novoro.seam.commands.ability;

import net.minecraft.world.GameMode;

public class SpectatorCommand extends GamemodeCommand {
    public SpectatorCommand() {
        super("gmsp", "minecraft.command.gamemode", GameMode.SPECTATOR);
    }
}