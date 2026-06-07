package me.novoro.seam.objects;

import me.novoro.seam.api.Location;
import me.novoro.seam.api.configuration.Configuration;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class Home extends Location {
    private final String name;

    public Home(String name, Configuration config) {
        super(config);
        this.name = name;
    }

    public Home(ServerPlayerEntity player, String name) {
        super(player);
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public void addReplacements(Map<String, String> replacements) {
        super.addReplacements(replacements);
        replacements.put("home_name", this.name);
    }

    @Override
    public Configuration toConfiguration() {
        return super.toConfiguration();
    }
}
