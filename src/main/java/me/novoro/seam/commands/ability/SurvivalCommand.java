package me.novoro.seam.commands.ability;

import net.minecraft.world.GameMode;

public class SurvivalCommand extends GamemodeCommand {
    public SurvivalCommand() {
        super("gms", "minecraft.command.gamemode", GameMode.SURVIVAL);
    }
}