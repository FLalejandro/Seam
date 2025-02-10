package me.novoro.seam.mixins;

import me.novoro.seam.api.Location;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "onDeath",
            at = @At(value = "HEAD")
    )
    public void seam$onDeath(DamageSource damageSource, CallbackInfo callback) {
        this.cacheLastLocation();
    }

    @Inject(method = "teleportTo",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;requestTeleport(DDDFF)V"
            )
    )
    public void seam$teleportTo(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> callback) {
        this.cacheLastLocation();
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;requestTeleport(DDDFFLjava/util/Set;)V"
            )
    )
    public void seam$teleport1(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch, CallbackInfoReturnable<Boolean> callback) {
        this.cacheLastLocation();
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;requestTeleport(DDDFF)V"
            )
    )
    public void seam$teleport2(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo callback) {
        this.cacheLastLocation();
    }

    @Unique
    private void cacheLastLocation() {
        Location lastLocation = new Location((ServerPlayerEntity) (Object) this);
        // ToDo: this
    }
}
