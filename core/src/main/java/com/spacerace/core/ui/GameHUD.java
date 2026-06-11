package com.spacerace.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.spacerace.core.entities.Car;

public class GameHUD implements Disposable {

    private static final float PANEL_MIN_SIZE = 128f;

    private final BitmapFont font;
    private final ShapeRenderer shapes;
    private final OrthographicCamera viewportCamera;
    private final OrthographicCamera screenCamera;
    private final GlyphLayout layout = new GlyphLayout();
    private float raceTimer;

    public GameHUD() {
        font = new BitmapFont();
        font.getData().setScale(1.3f);
        shapes = new ShapeRenderer();
        viewportCamera = new OrthographicCamera();
        screenCamera = new OrthographicCamera();
    }

    public void update(float delta) {
        raceTimer += delta;
    }

    /** Per-player HUD inside one split-screen half. */
    public void renderPlayer(SpriteBatch batch, int vpW, int vpH, Car car, int totalLaps) {
        viewportCamera.setToOrtho(false, vpW, vpH);
        viewportCamera.update();

        batch.setProjectionMatrix(viewportCamera.combined);
        batch.begin();

        String lapText = "Lap " + car.getLapsCompleted() + "/" + totalLaps;
        layout.setText(font, lapText);
        font.setColor(Color.WHITE);
        font.draw(batch, lapText, vpW / 2f - layout.width / 2f, vpH - 16f);

        if (!car.isDriving()) {
            layout.setText(font, car.getState().name());
            font.setColor(Color.RED);
            font.draw(batch, car.getState().name(),
                    vpW / 2f - layout.width / 2f, vpH - 40f);
        }

        String speedText = "Speed: " + (int) Math.abs(car.getSpeed());
        layout.setText(font, speedText);
        font.setColor(Color.WHITE);
        font.draw(batch, speedText, vpW / 2f - layout.width / 2f, 28f);

        batch.end();
    }

    /** Shared race timer on the vertical split line (full screen). */
    public void renderCenterTimer(SpriteBatch batch, int screenW, int screenH) {
        renderCenterPanel(batch, screenW, screenH, formatTime(raceTimer), Color.WHITE, 1.3f);
    }

    /** Pre-race countdown (3, 2, 1, START!) in the same center square. */
    public void renderCountdown(SpriteBatch batch, int screenW, int screenH, String text, Color textColor) {
        float scale = text.length() <= 1 ? 3.2f : 2.4f;
        renderCenterPanel(batch, screenW, screenH, text, textColor, scale);
    }

    private void renderCenterPanel(SpriteBatch batch, int screenW, int screenH,
                                   String text, Color textColor, float fontScale) {
        screenCamera.setToOrtho(false, screenW, screenH);
        screenCamera.update();

        float prevX = font.getData().scaleX;
        float prevY = font.getData().scaleY;
        font.getData().setScale(fontScale);

        layout.setText(font, text);

        float pad = 16f;
        float inner = Math.max(layout.width, layout.height) + pad;
        float boxSize = Math.max(inner + pad, PANEL_MIN_SIZE);
        float boxX = screenW / 2f - boxSize / 2f;
        float boxY = screenH / 2f - boxSize / 2f;

        shapes.setProjectionMatrix(screenCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.04f, 0.05f, 0.12f, 0.9f);
        shapes.rect(boxX, boxY, boxSize, boxSize);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);
        shapes.setColor(0.75f, 0.82f, 1f, 1f);
        shapes.rect(boxX, boxY, boxSize, boxSize);
        shapes.end();
        Gdx.gl.glLineWidth(1f);

        batch.setProjectionMatrix(screenCamera.combined);
        batch.begin();
        font.setColor(textColor);
        font.draw(batch, text,
                screenW / 2f - layout.width / 2f,
                screenH / 2f + layout.height / 2f - 2f);
        batch.end();

        font.getData().setScale(prevX, prevY);
    }

    public float getRaceTimer() { return raceTimer; }
    public BitmapFont getFont() { return font; }

    private String formatTime(float seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        int millis = (int) ((seconds * 100) % 100);
        return String.format("%d:%02d.%02d", mins, secs, millis);
    }

    @Override
    public void dispose() {
        font.dispose();
        shapes.dispose();
    }
}
