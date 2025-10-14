package me.novoro.seam.mixins;

import me.novoro.seam.config.WaypointManager;
import me.novoro.seam.objects.Waypoint;
import me.novoro.seam.utils.SeamLogger;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(net.minecraft.server.PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "loadPlayerData", at = @At("RETURN"), cancellable = true)
    public void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<Optional<NbtCompound>> cir) {
        Waypoint firstJoinSpawn = WaypointManager.getFirstJoinSpawn();
        Optional<NbtCompound> playerDataOptional = cir.getReturnValue();
        NbtCompound playerData = playerDataOptional.orElse(null);
        boolean isFirstJoin = playerData == null || !playerData.contains("seam.joinedBefore");

        if (isFirstJoin) {
            if (firstJoinSpawn != null) {
                cir.setReturnValue(this.addSpawnToNBT(player, cir.getReturnValue().orElse(new NbtCompound()), firstJoinSpawn));
            } else {
                SeamLogger.error("firstJoinSpawn is null for player " + player.getName().getString());
            }
        } else if (WaypointManager.shouldForceSpawnOnJoin()) {
            Waypoint playerSpawn = WaypointManager.determineSpawnPoint(player);
            if (playerSpawn != null) cir.setReturnValue(this.addSpawnToNBT(player,
                    cir.getReturnValue().orElse(new NbtCompound()), playerSpawn));
        }
    }

    @Redirect(
            method = "respawnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getRespawnTarget(ZLnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;"
            )
    )
    public TeleportTarget Seam$onPlayerRespawn(ServerPlayerEntity player, boolean alive, TeleportTarget.PostDimensionTransition transition) {
        if (WaypointManager.forceSpawnOnDeath() || (WaypointManager.spawnNoRespawn() && (player.getSpawnPointPosition() == null || player.getRespawnTarget(alive, transition).missingRespawnBlock()))) {
            Waypoint spawn = WaypointManager.determineSpawnPoint(player);
            if (spawn != null) return spawn.toTeleportTarget(transition);
        }
        return player.getRespawnTarget(alive, transition);
    }

    @Unique
    private Optional<NbtCompound> addSpawnToNBT(ServerPlayerEntity player, NbtCompound playerNBT, Waypoint spawnPoint) {
        spawnPoint.setNBTLocation(playerNBT);
        player.readNbt(playerNBT);
        return Optional.of(playerNBT);
    }

}
