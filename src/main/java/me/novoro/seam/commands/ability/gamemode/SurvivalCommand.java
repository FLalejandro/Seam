package me.novoro.seam.commands.ability.gamemode;

import net.minecraft.world.GameMode;

public class SurvivalCommand extends GamemodeCommand {
    public SurvivalCommand() {
        super("survival", "minecraft.command.gamemode.survival", GameMode.SURVIVAL, "gms");
    }
}