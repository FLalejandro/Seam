package me.novoro.seam.objects;

import me.novoro.seam.api.Location;
import me.novoro.seam.api.configuration.Configuration;

public class Home extends Location {
    private final String name;

    public Home(Configuration config) {
        super(config);
        this.name = config.getString("name");
    }
}
