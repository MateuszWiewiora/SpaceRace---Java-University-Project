package com.spacerace.core.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Collision burst at impact point.
 * Drop {@code ui/collision_burst.png} (comic "BAM"/star graphic) to replace the fallback sparks.
 */
public class CollisionEffectManager {

    private static final String BURST_TEXTURE_PATH = "ui/collision_burst.png";
    private static final float DURATION = 0.24f;

    private final Pool<Hit> pool = new Pool<Hit>(12) {
        @Override
        protected Hit newObject() {
            return new Hit();
        }
    };

    private final Array<Hit> active = new Array<>();
    private Texture burstTexture;
    private boolean textureLookupDone;

    public void spawn(float x, float y, float intensity) {
        Hit hit = pool.obtain();
        hit.init(x, y, 0.55f + intensity * 0.0015f, MathUtils.random(-25f, 25f));
        active.add(hit);
    }

    public void update(float delta) {
        for (int i = active.size - 1; i >= 0; i--) {
            Hit hit = active.get(i);
            hit.age += delta;
            if (hit.age >= DURATION) {
                active.removeIndex(i);
                pool.free(hit);
            }
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        if (active.size == 0) return;

        ensureBurstTexture();
        if (burstTexture != null) {
            renderBurstSprites(batch, camera);
        } else {
            renderFallbackSparks(shapeRenderer, camera);
        }
    }

    private void renderBurstSprites(SpriteBatch batch, OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        Color prev = batch.getColor().cpy();
        batch.begin();

        for (Hit hit : active) {
            float t = hit.age / DURATION;
            float alpha = 1f - t;
            float size = 64f * hit.scale * (0.85f + 0.35f * (1f - t));

            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(burstTexture,
                    hit.x - size / 2f, hit.y - size / 2f,
                    size / 2f, size / 2f,
                    size, size,
                    1f, 1f,
                    hit.rotation + t * 18f,
                    0, 0,
                    burstTexture.getWidth(), burstTexture.getHeight(),
                    false, false);
        }

        batch.setColor(prev);
        batch.end();
    }

    private void renderFallbackSparks(ShapeRenderer renderer, OrthographicCamera camera) {
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);

        for (Hit hit : active) {
            float t = hit.age / DURATION;
            float alpha = (1f - t) * 0.7f;
            float arm = 14f * hit.scale * (1f - t * 0.5f);

            for (int i = 0; i < 4; i++) {
                float angle = hit.rotation + i * 90f;
                float dx = MathUtils.cosDeg(angle) * arm;
                float dy = MathUtils.sinDeg(angle) * arm;
                renderer.setColor(1f, 1f, 1f, alpha);
                renderer.line(hit.x, hit.y, hit.x + dx, hit.y + dy);
            }
        }

        renderer.end();
    }

    private void ensureBurstTexture() {
        if (textureLookupDone) return;
        textureLookupDone = true;
        try {
            if (Gdx.files.internal(BURST_TEXTURE_PATH).exists()) {
                burstTexture = new Texture(Gdx.files.internal(BURST_TEXTURE_PATH));
                burstTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            }
        } catch (Exception ignored) {
            burstTexture = null;
        }
    }

    public void clear() {
        for (Hit hit : active) {
            pool.free(hit);
        }
        active.clear();
    }

    public void dispose() {
        clear();
        if (burstTexture != null) {
            burstTexture.dispose();
            burstTexture = null;
        }
        textureLookupDone = false;
    }

    private static final class Hit implements Pool.Poolable {
        float x;
        float y;
        float scale;
        float rotation;
        float age;

        void init(float x, float y, float scale, float rotation) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.rotation = rotation;
            this.age = 0f;
        }

        @Override
        public void reset() {
            age = 0f;
        }
    }
}
