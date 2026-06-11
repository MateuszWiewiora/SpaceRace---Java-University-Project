package com.spacerace.core.cars;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.utils.ObjectMap;

/** Shared ship texture cache (setup screen + race). */
public final class ShipTextures {

    private static final ObjectMap<String, Texture> cache = new ObjectMap<>();

    private ShipTextures() {}

    public static Texture get(String path) {
        Texture texture = cache.get(path);
        if (texture == null) {
            texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
            cache.put(path, texture);
        }
        return texture;
    }

    public static void dispose() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
