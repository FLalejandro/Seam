package me.novoro.seam.api.permissions;

import me.novoro.seam.Seam;
import net.minecraft.server.network.ServerPlayerEntity;

public class SeamPermission {

    private final String permission;
    private final int permissionLevel;

    private SeamPermission(String permission, int permissionLevel) {
        this.permission = permission;
        this.permissionLevel = permissionLevel;
    }

    public boolean matches(ServerPlayerEntity player) {
        if (this.permission.isEmpty()) return true;
        return Seam.getPermissionProvider().hasPermission(player, this.permission, this.permissionLevel);
    }

    @Override
    public String toString() {
        return "SeamPermission{permission=\"" + this.permission + "\",permissionLevel=" + this.permissionLevel + "}";
    }

    public static SeamPermission of(String permission, int level) {
        return new SeamPermission(permission, level);
    }

}
