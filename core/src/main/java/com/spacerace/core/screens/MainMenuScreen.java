package com.spacerace.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacerace.core.SpaceRaceGame;
import com.spacerace.core.audio.AudioManager;
import com.spacerace.core.ui.UiFonts;

/** Main menu – fixed UI layout; background fills the window. */
public class MainMenuScreen implements Screen {

    private static final String BG_PATH = "ui/menu_bg.png";
    private static final String LOGO_PATH = "ui/logo_space_race.png";
    private static final String PLAY_BUTTON_PATH = "ui/btn_play_normal.png";
    private static final String FONT_PATH = "ui/upheavtt.ttf";

    private static final float LOGO_W = 680f;
    private static final float LOGO_X = (SpaceRaceGame.WORLD_WIDTH - LOGO_W) / 2f;
    private static final float LOGO_Y = 300f;

    private final SpaceRaceGame game;
    private final SpriteBatch batch;

    private final OrthographicCamera uiCamera;
    private final OrthographicCamera screenCamera;
    private final Viewport viewport;
    private final Rectangle playButton = new Rectangle(300f, 255f, 200f, 56f);
    private final Vector3 touch = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();

    private Texture bgTexture;
    private Texture logoTexture;
    private Texture playButtonTexture;
    private FreeTypeFontGenerator fontGenerator;

    private BitmapFont promptFont;
    private BitmapFont controlsFont;

    public MainMenuScreen(SpaceRaceGame game) {
        this.game = game;
        this.batch = game.getBatch();

        uiCamera = new OrthographicCamera();
        screenCamera = new OrthographicCamera();
        viewport = new FitViewport(SpaceRaceGame.WORLD_WIDTH, SpaceRaceGame.WORLD_HEIGHT, uiCamera);
        uiCamera.position.set(SpaceRaceGame.WORLD_WIDTH / 2f, SpaceRaceGame.WORLD_HEIGHT / 2f, 0);
        uiCamera.update();
    }

    @Override
    public void show() {
        bgTexture = new Texture(Gdx.files.internal(BG_PATH));
        logoTexture = new Texture(Gdx.files.internal(LOGO_PATH));
        logoTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        playButtonTexture = new Texture(Gdx.files.internal(PLAY_BUTTON_PATH));
        playButtonTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));

        FreeTypeFontGenerator.FreeTypeFontParameter promptParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        promptParams.size = 16;
        promptParams.color = new Color(0.82f, 0.84f, 0.9f, 1f);
        UiFonts.applyCharset(promptParams);
        promptFont = fontGenerator.generateFont(promptParams);

        FreeTypeFontGenerator.FreeTypeFontParameter controlsParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        controlsParams.size = 14;
        controlsParams.color = new Color(0.7f, 0.72f, 0.78f, 1f);
        UiFonts.applyCharset(controlsParams);
        controlsFont = fontGenerator.generateFont(controlsParams);

        AudioManager.getInstance().playMenuMusic();
    }

    @Override
    public void render(float delta) {
        if (handleInput()) {
            return;
        }

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        ScreenUtils.clear(0.05f, 0.05f, 0.15f, 1f);

        Gdx.gl.glViewport(0, 0, screenW, screenH);
        drawBackgroundCover(screenW, screenH);

        viewport.update(screenW, screenH, true);
        viewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        float logoH = LOGO_W * logoTexture.getHeight() / logoTexture.getWidth();
        batch.draw(logoTexture, LOGO_X, LOGO_Y, LOGO_W, logoH);

        boolean hover = isMouseOver(playButton);
        if (hover) {
            batch.setColor(1f, 1f, 0.85f, 1f);
        }
        batch.draw(playButtonTexture, playButton.x, playButton.y, playButton.width, playButton.height);
        batch.setColor(Color.WHITE);

        layout.setText(promptFont, "Click or press SPACE / ENTER");
        promptFont.draw(batch, "Click or press SPACE / ENTER",
                SpaceRaceGame.WORLD_WIDTH / 2f - layout.width / 2f,
                playButton.y - 24f);

        layout.setText(controlsFont, "Player 1: W A S D    |    Player 2: Arrow Keys");
        controlsFont.draw(batch, "Player 1: W A S D    |    Player 2: Arrow Keys",
                SpaceRaceGame.WORLD_WIDTH / 2f - layout.width / 2f,
                SpaceRaceGame.WORLD_HEIGHT * 0.12f);

        batch.end();

        Gdx.gl.glViewport(0, 0, screenW, screenH);
    }

    /** Scales the background to cover the full window (no side bars). */
    private void drawBackgroundCover(float screenW, float screenH) {
        screenCamera.setToOrtho(false, screenW, screenH);
        screenCamera.position.set(screenW / 2f, screenH / 2f, 0);
        screenCamera.update();

        float texAspect = (float) bgTexture.getWidth() / bgTexture.getHeight();
        float screenAspect = screenW / screenH;
        float drawW;
        float drawH;

        if (screenAspect > texAspect) {
            drawW = screenW;
            drawH = screenW / texAspect;
        } else {
            drawH = screenH;
            drawW = screenH * texAspect;
        }

        float drawX = (screenW - drawW) / 2f;
        float drawY = (screenH - drawH) / 2f;

        batch.setProjectionMatrix(screenCamera.combined);
        batch.begin();
        batch.draw(bgTexture, drawX, drawY, drawW, drawH);
        batch.end();
    }

    private boolean handleInput() {
        boolean play = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isMouseOver(playButton)) {
            play = true;
        }

        if (play) {
            game.setScreen(new SetupScreen(game));
            dispose();
            return true;
        }
        return false;
    }

    private boolean isMouseOver(Rectangle rect) {
        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);
        return rect.contains(touch.x, touch.y);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        AudioManager.getInstance().stopMenuMusic();
        if (promptFont != null) promptFont.dispose();
        if (controlsFont != null) controlsFont.dispose();
        if (fontGenerator != null) fontGenerator.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (logoTexture != null) logoTexture.dispose();
        if (playButtonTexture != null) playButtonTexture.dispose();
    }
}
