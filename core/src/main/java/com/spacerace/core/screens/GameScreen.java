package com.spacerace.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacerace.core.SpaceRaceGame;
import com.spacerace.core.entities.Car;

/**
 * GameScreen — the main gameplay screen with vertical split-screen.
 *
 * Layout:
 * ┌────────────────┬────────────────┐
 * │    Player 1    │    Player 2    │
 * │   (W/A/S/D)   │  (Arrow Keys)  │
 * └────────────────┴────────────────┘
 *
 * Each player has their own {@link OrthographicCamera} and {@link Viewport}.
 * The screen is split vertically: left half for P1, right half for P2.
 */
public class GameScreen implements Screen {

    // ── References ────────────────────────────────────────────────────
    private final SpaceRaceGame game;
    private final SpriteBatch batch;

    // ── Split-screen cameras & viewports ──────────────────────────────
    private OrthographicCamera cameraP1;
    private OrthographicCamera cameraP2;
    private Viewport viewportP1;
    private Viewport viewportP2;

    // ── Rendering ─────────────────────────────────────────────────────
    private ShapeRenderer shapeRenderer;
    private BitmapFont labelFont;

    // ── Players ───────────────────────────────────────────────────────
    private Car player1;
    private Car player2;

    // ── World / track boundaries ──────────────────────────────────────
    private static final float TRACK_WIDTH  = 2000f;
    private static final float TRACK_HEIGHT = 2000f;

    // ── Background stars (simple decoration) ──────────────────────────
    private float[] starX;
    private float[] starY;
    private float[] starSize;
    private static final int STAR_COUNT = 150;

    public GameScreen(SpaceRaceGame game) {
        this.game  = game;
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        // ── Create cameras ────────────────────────────────────────────
        cameraP1 = new OrthographicCamera();
        cameraP2 = new OrthographicCamera();

        viewportP1 = new FitViewport(SpaceRaceGame.WORLD_WIDTH, SpaceRaceGame.WORLD_HEIGHT, cameraP1);
        viewportP2 = new FitViewport(SpaceRaceGame.WORLD_WIDTH, SpaceRaceGame.WORLD_HEIGHT, cameraP2);

        // ── Create shape renderer ─────────────────────────────────────
        shapeRenderer = new ShapeRenderer();

        // ── Create label font ─────────────────────────────────────────
        labelFont = new BitmapFont();
        labelFont.setColor(Color.WHITE);
        labelFont.getData().setScale(1.2f);

        // ── Spawn players ─────────────────────────────────────────────
        player1 = new Car(TRACK_WIDTH * 0.25f, TRACK_HEIGHT * 0.5f, Color.CYAN);
        player2 = new Car(TRACK_WIDTH * 0.75f, TRACK_HEIGHT * 0.5f, Color.ORANGE);

        // ── Generate random star positions ────────────────────────────
        starX    = new float[STAR_COUNT];
        starY    = new float[STAR_COUNT];
        starSize = new float[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]    = MathUtils.random(0f, TRACK_WIDTH);
            starY[i]    = MathUtils.random(0f, TRACK_HEIGHT);
            starSize[i] = MathUtils.random(1f, 3f);
        }
    }

    @Override
    public void render(float delta) {
        // ── Handle input ──────────────────────────────────────────────
        handleInput();

        // ── Update car physics ────────────────────────────────────────
        player1.update(delta);
        player2.update(delta);

        // ── Clamp to track boundaries ─────────────────────────────────
        player1.clampToTrack(TRACK_WIDTH, TRACK_HEIGHT);
        player2.clampToTrack(TRACK_WIDTH, TRACK_HEIGHT);

        // ── Update cameras to follow their respective players ─────────
        updateCamera(cameraP1, player1);
        updateCamera(cameraP2, player2);

        // ── Get actual screen dimensions ──────────────────────────────
        int screenWidth  = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        int halfWidth    = screenWidth / 2;

        // ── Clear the entire screen first ─────────────────────────────
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ── Render Player 1's viewport (left half) ────────────────────
        Gdx.gl.glViewport(0, 0, halfWidth, screenHeight);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(0, 0, halfWidth, screenHeight);

        renderWorld(cameraP1);
        renderHUD("P1", Color.CYAN, halfWidth, screenHeight);

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        // ── Render Player 2's viewport (right half) ───────────────────
        Gdx.gl.glViewport(halfWidth, 0, halfWidth, screenHeight);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(halfWidth, 0, halfWidth, screenHeight);

        renderWorld(cameraP2);
        renderHUD("P2", Color.ORANGE, halfWidth, screenHeight);

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        // ── Reset viewport to full screen ─────────────────────────────
        Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);

        // ── Draw the divider line ─────────────────────────────────────
        drawDivider(screenWidth, screenHeight, halfWidth);

        // ── ESC to return to main menu ────────────────────────────────
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  INPUT HANDLING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Maps keyboard input to Car control flags.
     *
     * Player 1: W (accelerate), S (brake/reverse), A (turn left), D (turn right)
     * Player 2: Arrow keys
     */
    private void handleInput() {
        // ── Player 1 — WASD ───────────────────────────────────────────
        player1.setAccelerating(Gdx.input.isKeyPressed(Input.Keys.W));
        player1.setBraking(Gdx.input.isKeyPressed(Input.Keys.S));
        player1.setTurningLeft(Gdx.input.isKeyPressed(Input.Keys.A));
        player1.setTurningRight(Gdx.input.isKeyPressed(Input.Keys.D));

        // ── Player 2 — Arrow Keys ─────────────────────────────────────
        player2.setAccelerating(Gdx.input.isKeyPressed(Input.Keys.UP));
        player2.setBraking(Gdx.input.isKeyPressed(Input.Keys.DOWN));
        player2.setTurningLeft(Gdx.input.isKeyPressed(Input.Keys.LEFT));
        player2.setTurningRight(Gdx.input.isKeyPressed(Input.Keys.RIGHT));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CAMERA
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Centers the camera on the given car.
     */
    private void updateCamera(OrthographicCamera camera, Car car) {
        camera.position.set(car.getX(), car.getY(), 0);
        camera.update();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  RENDERING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Renders the game world: background, stars, grid, and both cars.
     */
    private void renderWorld(OrthographicCamera camera) {

        shapeRenderer.setProjectionMatrix(camera.combined);

        // ── Draw background stars ─────────────────────────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.7f, 0.7f, 0.8f, 1f);
        for (int i = 0; i < STAR_COUNT; i++) {
            shapeRenderer.circle(starX[i], starY[i], starSize[i]);
        }
        shapeRenderer.end();

        // ── Draw track boundary and grid ──────────────────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(0, 0, TRACK_WIDTH, TRACK_HEIGHT);

        shapeRenderer.setColor(0.1f, 0.1f, 0.2f, 1f);
        for (float gx = 0; gx <= TRACK_WIDTH; gx += 200f) {
            shapeRenderer.line(gx, 0, gx, TRACK_HEIGHT);
        }
        for (float gy = 0; gy <= TRACK_HEIGHT; gy += 200f) {
            shapeRenderer.line(0, gy, TRACK_WIDTH, gy);
        }
        shapeRenderer.end();

        // ── Draw cars (each car handles its own rotation) ─────────────
        shapeRenderer.setProjectionMatrix(camera.combined);
        player1.render(shapeRenderer);
        player2.render(shapeRenderer);
    }

    /**
     * Draws a simple HUD overlay showing player label.
     */
    private void renderHUD(String label, Color color, int vpW, int vpH) {
        OrthographicCamera hudCamera = new OrthographicCamera(vpW, vpH);
        hudCamera.position.set(vpW / 2f, vpH / 2f, 0);
        hudCamera.update();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        labelFont.setColor(color);
        labelFont.draw(batch, label + "  [ESC = Menu]", 10f, vpH - 10f);
        batch.end();
    }

    /**
     * Draws a vertical divider line between the two viewports.
     */
    private void drawDivider(int screenWidth, int screenHeight, int halfWidth) {
        OrthographicCamera uiCamera = new OrthographicCamera(screenWidth, screenHeight);
        uiCamera.position.set(screenWidth / 2f, screenHeight / 2f, 0);
        uiCamera.update();

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(halfWidth - 1, 0, 2, screenHeight);
        shapeRenderer.end();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void resize(int width, int height) { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (labelFont     != null) labelFont.dispose();
    }
}
