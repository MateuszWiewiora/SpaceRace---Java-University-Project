package com.spacerace.core.screens;



import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Input;

import com.badlogic.gdx.Screen;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.graphics.GL20;

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

import com.spacerace.core.RaceConfig;

import com.spacerace.core.SpaceRaceGame;

import com.spacerace.core.cars.CarCatalog;
import com.spacerace.core.cars.ShipTextures;

import com.spacerace.core.track.TrackCatalog;

import com.spacerace.core.track.TrackMap;

import com.spacerace.core.ui.UiFonts;



/** Pre-race setup: track carousel, per-player ship carousels, start race. */

public class SetupScreen implements Screen {



    private static final String BG_PATH = "ui/setup_bg.png";

    private static final String ARROW_LEFT_PATH = "ui/btn_arrow_left.png";

    private static final String ARROW_RIGHT_PATH = "ui/btn_arrow_right.png";

    private static final String START_BUTTON_PATH = "ui/btn_start_normal.png";

    private static final String FONT_PATH = "ui/upheavtt.ttf";



    private static final Color P1_LABEL_COLOR = new Color(0.35f, 0.92f, 1f, 1f);

    private static final Color P2_LABEL_COLOR = new Color(1f, 0.6f, 0.25f, 1f);



    private static final float HINT_Y_1 = 18f;

    private static final float HINT_Y_2 = 34f;

    private static final float START_BTN_W = 200f;

    private static final float START_BTN_H = 52f;

    private static final float START_BTN_X = (SpaceRaceGame.WORLD_WIDTH - START_BTN_W) / 2f;

    private static final float START_BTN_Y = 40f;



    private static final float CAR_ARROW_SIZE = 44f;

    private static final float CAR_ARROW_GAP = 12f;

    private static final float CAR_PREVIEW_W = 40f;

    private static final float CAR_PREVIEW_H = 58f;

    private static final float CAR_CAROUSEL_Y = 188f;

    private static final float CAR_PLAYER_LABEL_Y = CAR_CAROUSEL_Y + CAR_PREVIEW_H + 54f;



    private static final float CAR_BLOCK_W =

            CAR_ARROW_SIZE + CAR_ARROW_GAP + CAR_PREVIEW_W + CAR_ARROW_GAP + CAR_ARROW_SIZE;

    private static final float P1_BLOCK_X = 110f;

    private static final float P2_BLOCK_X = SpaceRaceGame.WORLD_WIDTH - 110f - CAR_BLOCK_W;



    private static final float PREVIEW_W = 260f;

    private static final float PREVIEW_H = 145f;

    private static final float PREVIEW_X = (SpaceRaceGame.WORLD_WIDTH - PREVIEW_W) / 2f;

    private static final float PREVIEW_Y = 370f;



    private static final float ARROW_SIZE = 48f;

    private static final float ARROW_GAP = 16f;

    private static final float ARROW_LEFT_X = PREVIEW_X - ARROW_SIZE - ARROW_GAP;

    private static final float ARROW_RIGHT_X = PREVIEW_X + PREVIEW_W + ARROW_GAP;

    private static final float ARROW_Y = PREVIEW_Y + (PREVIEW_H - ARROW_SIZE) / 2f;



    private static final Rectangle TRACK_PREVIEW_BOX = new Rectangle(PREVIEW_X, PREVIEW_Y, PREVIEW_W, PREVIEW_H);

    private static final Rectangle ARROW_LEFT = new Rectangle(ARROW_LEFT_X, ARROW_Y, ARROW_SIZE, ARROW_SIZE);

    private static final Rectangle ARROW_RIGHT = new Rectangle(ARROW_RIGHT_X, ARROW_Y, ARROW_SIZE, ARROW_SIZE);

    private static final Rectangle START_BUTTON = new Rectangle(START_BTN_X, START_BTN_Y, START_BTN_W, START_BTN_H);



    private static final Rectangle P1_CAR_ARROW_LEFT = carArrowLeft(P1_BLOCK_X);

    private static final Rectangle P1_CAR_ARROW_RIGHT = carArrowRight(P1_BLOCK_X);

    private static final Rectangle P2_CAR_ARROW_LEFT = carArrowLeft(P2_BLOCK_X);

    private static final Rectangle P2_CAR_ARROW_RIGHT = carArrowRight(P2_BLOCK_X);



    private final SpaceRaceGame game;

    private final SpriteBatch batch;



    private final OrthographicCamera uiCamera;

    private final OrthographicCamera screenCamera;

    private final Viewport viewport;

    private final Vector3 touch = new Vector3();

    private final GlyphLayout layout = new GlyphLayout();

    private OrthographicCamera previewCamera;

    private TrackMap previewMap;

    private int loadedPreviewIndex = -1;



    private Texture bgTexture;

    private Texture arrowLeftTexture;

    private Texture arrowRightTexture;

    private Texture startButtonTexture;

    private FreeTypeFontGenerator fontGenerator;



    private BitmapFont titleFont;

    private BitmapFont playerFont;

    private BitmapFont labelFont;

    private BitmapFont itemFont;

    private BitmapFont descFont;

    private BitmapFont hintFont;



    private int selectedTrack;

    private int selectedCarP1;

    private int selectedCarP2;



    public SetupScreen(SpaceRaceGame game) {

        this.game = game;

        this.batch = game.getBatch();



        uiCamera = new OrthographicCamera();

        screenCamera = new OrthographicCamera();

        viewport = new FitViewport(SpaceRaceGame.WORLD_WIDTH, SpaceRaceGame.WORLD_HEIGHT, uiCamera);

        uiCamera.position.set(SpaceRaceGame.WORLD_WIDTH / 2f, SpaceRaceGame.WORLD_HEIGHT / 2f, 0);

        uiCamera.update();

    }



    private static float carCenterX(float blockX) {

        return blockX + CAR_ARROW_SIZE + CAR_ARROW_GAP + CAR_PREVIEW_W / 2f;

    }



    private static Rectangle carArrowLeft(float blockX) {

        float y = CAR_CAROUSEL_Y + (CAR_PREVIEW_H - CAR_ARROW_SIZE) / 2f;

        return new Rectangle(blockX, y, CAR_ARROW_SIZE, CAR_ARROW_SIZE);

    }



    private static Rectangle carArrowRight(float blockX) {

        float x = blockX + CAR_ARROW_SIZE + CAR_ARROW_GAP + CAR_PREVIEW_W + CAR_ARROW_GAP;

        float y = CAR_CAROUSEL_Y + (CAR_PREVIEW_H - CAR_ARROW_SIZE) / 2f;

        return new Rectangle(x, y, CAR_ARROW_SIZE, CAR_ARROW_SIZE);

    }



    @Override

    public void show() {

        previewCamera = new OrthographicCamera();



        bgTexture = loadUiTexture(BG_PATH);

        arrowLeftTexture = loadUiTexture(ARROW_LEFT_PATH);

        arrowRightTexture = loadUiTexture(ARROW_RIGHT_PATH);

        startButtonTexture = loadUiTexture(START_BUTTON_PATH);

        for (CarCatalog.Entry car : CarCatalog.CARS) {
            ShipTextures.get(car.texturePath);
        }

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));



        titleFont = fontGenerator.generateFont(fontParams(26, Color.WHITE));

        playerFont = fontGenerator.generateFont(fontParams(36, Color.WHITE));

        labelFont = fontGenerator.generateFont(fontParams(15, new Color(0.85f, 0.87f, 0.92f, 1f)));

        itemFont = fontGenerator.generateFont(fontParams(20, new Color(0.2f, 0.95f, 1f, 1f)));

        descFont = fontGenerator.generateFont(fontParams(13, new Color(0.75f, 0.8f, 0.9f, 1f)));

        hintFont = fontGenerator.generateFont(fontParams(11, new Color(0.65f, 0.68f, 0.75f, 1f)));

    }



    private FreeTypeFontGenerator.FreeTypeFontParameter fontParams(int size, Color color) {

        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();

        params.size = size;

        params.color = color;

        UiFonts.applyCharset(params);

        return params;

    }



    private Texture loadUiTexture(String path) {

        Texture texture = new Texture(Gdx.files.internal(path));

        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        return texture;

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



        drawTitle();

        drawTrackCarousel();

        drawCarCarousel(1, carCenterX(P1_BLOCK_X), selectedCarP1, P1_CAR_ARROW_LEFT, P1_CAR_ARROW_RIGHT);

        drawCarCarousel(2, carCenterX(P2_BLOCK_X), selectedCarP2, P2_CAR_ARROW_LEFT, P2_CAR_ARROW_RIGHT);

        drawStartButton();

        drawHints();



        batch.end();



        drawTrackPreview();



        Gdx.gl.glViewport(0, 0, screenW, screenH);

    }



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



    private void drawTitle() {

        layout.setText(titleFont, "RACE SETUP");

        titleFont.draw(batch, "RACE SETUP",

                SpaceRaceGame.WORLD_WIDTH / 2f - layout.width / 2f,

                PREVIEW_Y + PREVIEW_H + 62f);

    }



    private void drawHints() {

        drawCenteredHint(hintFont, "ESC - back   |   ARROW UP / ARROW DOWN - track", HINT_Y_2);

        drawCenteredHint(hintFont, "A/D or SHIFT+ARROW LEFT/RIGHT - P1   |   ARROW LEFT/RIGHT - P2", HINT_Y_1);

    }



    private void drawCenteredHint(BitmapFont font, String text, float y) {

        layout.setText(font, text);

        font.draw(batch, text, SpaceRaceGame.WORLD_WIDTH / 2f - layout.width / 2f, y);

    }



    private void drawTrackCarousel() {

        TrackCatalog.Entry track = TrackCatalog.TRACKS[selectedTrack];



        drawArrowButton(arrowLeftTexture, ARROW_LEFT, isMouseOver(ARROW_LEFT));

        drawArrowButton(arrowRightTexture, ARROW_RIGHT, isMouseOver(ARROW_RIGHT));



        layout.setText(itemFont, track.displayName);

        itemFont.draw(batch, track.displayName,

                SpaceRaceGame.WORLD_WIDTH / 2f - layout.width / 2f,

                PREVIEW_Y + PREVIEW_H + 36f);



        layout.setText(descFont, track.description);

        descFont.draw(batch, track.description,

                SpaceRaceGame.WORLD_WIDTH / 2f - layout.width / 2f,

                PREVIEW_Y + PREVIEW_H + 18f);

    }



    private void drawCarCarousel(int player, float centerX, int selectedIndex,

                                 Rectangle arrowLeft, Rectangle arrowRight) {

        String playerLabel = player == 1 ? "P1" : "P2";

        Color labelColor = player == 1 ? P1_LABEL_COLOR : P2_LABEL_COLOR;



        playerFont.setColor(labelColor);

        layout.setText(playerFont, playerLabel);

        playerFont.draw(batch, playerLabel, centerX - layout.width / 2f, CAR_PLAYER_LABEL_Y);



        drawArrowButton(arrowLeftTexture, arrowLeft, isMouseOver(arrowLeft));

        drawShipPreview(centerX, selectedIndex);

        drawArrowButton(arrowRightTexture, arrowRight, isMouseOver(arrowRight));



        CarCatalog.Entry car = CarCatalog.CARS[selectedIndex];

        labelFont.setColor(car.color);

        layout.setText(labelFont, car.displayName);

        labelFont.draw(batch, car.displayName, centerX - layout.width / 2f, CAR_CAROUSEL_Y - 28f);

    }



    private void drawShipPreview(float centerX, int selectedIndex) {

        Texture ship = ShipTextures.get(CarCatalog.CARS[selectedIndex].texturePath);

        float x = centerX - CAR_PREVIEW_W / 2f;

        float y = CAR_CAROUSEL_Y;

        batch.setColor(Color.WHITE);

        batch.draw(ship, x, y, CAR_PREVIEW_W, CAR_PREVIEW_H);

    }



    private void drawArrowButton(Texture texture, Rectangle bounds, boolean hover) {

        batch.setColor(Color.WHITE);

        if (hover) {

            batch.setColor(1f, 1f, 0.85f, 1f);

        }

        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);

        batch.setColor(Color.WHITE);

    }



    private void drawStartButton() {

        boolean hover = isMouseOver(START_BUTTON);

        if (hover) {

            batch.setColor(1f, 1f, 0.85f, 1f);

        }

        batch.draw(startButtonTexture, START_BUTTON.x, START_BUTTON.y, START_BUTTON.width, START_BUTTON.height);

        batch.setColor(Color.WHITE);

    }



    private void ensurePreviewLoaded() {

        if (loadedPreviewIndex == selectedTrack && previewMap != null) {

            return;

        }

        if (previewMap != null) {

            previewMap.dispose();

            previewMap = null;

        }

        previewMap = new TrackMap(TrackCatalog.TRACKS[selectedTrack].getMapPath());

        loadedPreviewIndex = selectedTrack;

        updatePreviewCamera();

    }



    private void updatePreviewCamera() {

        float mapW = previewMap.getWidthPx();

        float mapH = previewMap.getHeightPx();

        float boxAspect = TRACK_PREVIEW_BOX.width / TRACK_PREVIEW_BOX.height;

        float padding = 1.06f;

        float viewW;

        float viewH;



        if (mapW / mapH > boxAspect) {

            viewW = mapW * padding;

            viewH = viewW / boxAspect;

        } else {

            viewH = mapH * padding;

            viewW = viewH * boxAspect;

        }



        previewCamera.setToOrtho(false, viewW, viewH);

        previewCamera.position.set(mapW / 2f, mapH / 2f, 0);

        previewCamera.update();

    }



    private void drawTrackPreview() {

        ensurePreviewLoaded();



        int screenW = Gdx.graphics.getWidth();

        int screenH = Gdx.graphics.getHeight();



        Vector3 bottomLeft = new Vector3(TRACK_PREVIEW_BOX.x, TRACK_PREVIEW_BOX.y, 0);

        Vector3 topRight = new Vector3(

                TRACK_PREVIEW_BOX.x + TRACK_PREVIEW_BOX.width,

                TRACK_PREVIEW_BOX.y + TRACK_PREVIEW_BOX.height,

                0);

        viewport.project(bottomLeft);

        viewport.project(topRight);



        int x = Math.round(Math.min(bottomLeft.x, topRight.x));

        int y = Math.round(Math.min(bottomLeft.y, topRight.y));

        int w = Math.round(Math.abs(topRight.x - bottomLeft.x));

        int h = Math.round(Math.abs(topRight.y - bottomLeft.y));



        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        Gdx.gl.glScissor(x, y, w, h);

        Gdx.gl.glViewport(x, y, w, h);

        previewMap.render(previewCamera);

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        Gdx.gl.glViewport(0, 0, screenW, screenH);

    }



    private void cycleTrack(int delta) {

        int count = TrackCatalog.TRACKS.length;

        selectedTrack = (selectedTrack + delta + count) % count;

    }



    private void cycleCarP1(int delta) {

        int count = CarCatalog.CARS.length;

        selectedCarP1 = (selectedCarP1 + delta + count) % count;

    }



    private void cycleCarP2(int delta) {

        int count = CarCatalog.CARS.length;

        selectedCarP2 = (selectedCarP2 + delta + count) % count;

    }



    private boolean isShiftHeld() {

        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)

                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

    }



    private boolean handleInput() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            game.setScreen(new MainMenuScreen(game));

            dispose();

            return true;

        }



        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {

            cycleTrack(-1);

        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {

            cycleTrack(1);

        }



        for (int i = 0; i < TrackCatalog.TRACKS.length; i++) {

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {

                selectedTrack = i;

            }

        }



        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {

            cycleCarP1(-1);

        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {

            cycleCarP1(1);

        }



        boolean shift = isShiftHeld();

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {

            if (shift) {

                cycleCarP1(-1);

            } else {

                cycleCarP2(-1);

            }

        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {

            if (shift) {

                cycleCarP1(1);

            } else {

                cycleCarP2(1);

            }

        }



        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {

            if (isMouseOver(ARROW_LEFT)) {

                cycleTrack(-1);

            } else if (isMouseOver(ARROW_RIGHT)) {

                cycleTrack(1);

            } else if (isMouseOver(P1_CAR_ARROW_LEFT)) {

                cycleCarP1(-1);

            } else if (isMouseOver(P1_CAR_ARROW_RIGHT)) {

                cycleCarP1(1);

            } else if (isMouseOver(P2_CAR_ARROW_LEFT)) {

                cycleCarP2(-1);

            } else if (isMouseOver(P2_CAR_ARROW_RIGHT)) {

                cycleCarP2(1);

            }

        }



        boolean start = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)

                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER);



        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isMouseOver(START_BUTTON)) {

            start = true;

        }



        if (start) {

            RaceConfig config = new RaceConfig(

                    TrackCatalog.TRACKS[selectedTrack],

                    CarCatalog.CARS[selectedCarP1],

                    CarCatalog.CARS[selectedCarP2]

            );

            game.setScreen(new GameScreen(game, config));

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

        if (previewMap != null) previewMap.dispose();

        if (titleFont != null) titleFont.dispose();

        if (playerFont != null) playerFont.dispose();

        if (labelFont != null) labelFont.dispose();

        if (itemFont != null) itemFont.dispose();

        if (descFont != null) descFont.dispose();

        if (hintFont != null) hintFont.dispose();

        if (fontGenerator != null) fontGenerator.dispose();

        if (bgTexture != null) bgTexture.dispose();

        if (arrowLeftTexture != null) arrowLeftTexture.dispose();

        if (arrowRightTexture != null) arrowRightTexture.dispose();

        if (startButtonTexture != null) startButtonTexture.dispose();

    }

}


