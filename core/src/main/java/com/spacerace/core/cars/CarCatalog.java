package com.spacerace.core.cars;

import com.badlogic.gdx.graphics.Color;

/** Selectable ship variants for the setup screen and race. */
public final class CarCatalog {

    public static final class Entry {
        public final String displayName;
        public final String texturePath;
        public final Color color;

        public Entry(String displayName, String texturePath, Color color) {
            this.displayName = displayName;
            this.texturePath = texturePath;
            this.color = color;
        }
    }

    public static final Entry[] CARS = {
            new Entry("Azure Wing",   "cars/ship_1.png", new Color(0.35f, 0.75f, 1f, 1f)),
            new Entry("Violet Wing",  "cars/ship_2.png", new Color(0.75f, 0.35f, 0.95f, 1f)),
            new Entry("Steel Wing",   "cars/ship_3.png", new Color(0.55f, 0.65f, 0.85f, 1f)),
            new Entry("Crimson Wing", "cars/ship_4.png", new Color(0.95f, 0.35f, 0.55f, 1f)),
            new Entry("Emerald Wing", "cars/ship_5.png", new Color(0.35f, 0.9f, 0.45f, 1f)),
    };

    private CarCatalog() {}
}
