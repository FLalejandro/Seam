package me.novoro.seam.objects;

import me.novoro.seam.api.Location;
import me.novoro.seam.api.configuration.Configuration;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import java.util.Map;

public class Waypoint extends Location {
    private final String name;
    private final int weight;
    private final String permission;

    public Waypoint(ServerPlayerEntity player, String spawnName, int weight, String permission) {
        super(player);
        this.name = spawnName;
        this.weight = weight;
        this.permission = permission;
    }

    public Waypoint(String name, Configuration config) {
        super(config);
        this.name = name;
        this.weight = config.getInt("weight");
        this.permission = config.getString("permission");
    }

    @Override
    public void addReplacements(Map<String, String> replacements) {
        super.addReplacements(replacements);
        replacements.put("{spawn}", this.name);
        replacements.put("{weight}", String.valueOf(this.weight));
        replacements.put("{permission}", this.permission == null ? "none" : this.permission);
    }

    public int getWeight() {
        return this.weight;
    }

    public String getPermission() {
        return this.permission;
    }

    public TeleportTarget toTeleportTarget(TeleportTarget.PostDimensionTransition transition) {
        return new TeleportTarget(this.getWorld(), this.toVec3d(), Vec3d.ZERO, this.getYaw(), this.getPitch(), transition);
    }

    public void setNBTLocation(NbtCompound nbtCompound) {
        if (nbtCompound == null) nbtCompound = new NbtCompound();
        nbtCompound.putString("Dimension", this.getWorld().getRegistryKey().getValue().toString());
        NbtList posTag = new NbtList();
        posTag.addElement(0, NbtDouble.of(this.getX()));
        posTag.addElement(1, NbtDouble.of(this.getY()));
        posTag.addElement(2, NbtDouble.of(this.getZ()));
        nbtCompound.put("Pos", posTag);
        if (this.getPitch() != -1000 && this.getYaw() != -1000) {
            NbtList rotationTag = new NbtList();
            rotationTag.addElement(0, NbtFloat.of(this.getYaw()));
            rotationTag.addElement(1, NbtFloat.of(this.getPitch()));
            nbtCompound.put("Rotation", rotationTag);
        }
    }

    @Override
    public Configuration toConfiguration() {
        Configuration locationConfig = super.toConfiguration();
        locationConfig.set("weight", this.weight);
        locationConfig.set("permission", this.permission);
        return locationConfig;
    }
}
