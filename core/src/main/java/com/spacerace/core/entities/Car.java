package com.spacerace.core.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Car — represents a player's vehicle with top-down arcade physics.
 *
 * Physics model (no Box2D):
 *   1. Input sets flags (accelerating, braking, turning)
 *   2. Acceleration is applied along the car's facing direction
 *   3. Friction continuously reduces speed
 *   4. Turning rate depends on current speed (no turning when stationary)
 *   5. Position is updated by velocity each frame
 *
 * The car's "nose" points in the direction of {@link #rotation},
 * measured in degrees counter-clockwise from the positive X axis.
 * At rotation = 90°, the car faces upward (positive Y).
 */
public class Car {

    // ── Position & orientation ────────────────────────────────────────
    private final Vector2 position;
    private float rotation;         // degrees, 0 = right, 90 = up

    // ── Velocity ──────────────────────────────────────────────────────
    private float speed;            // scalar speed (pixels/sec), can be negative (reversing)
    private final Vector2 velocity; // computed from speed + rotation each frame

    // ── Tuning constants ──────────────────────────────────────────────
    private static final float MAX_SPEED       = 300f;  // max forward speed (px/s)
    private static final float MAX_REVERSE     = 100f;  // max reverse speed (px/s)
    private static final float ACCELERATION    = 200f;  // forward acceleration (px/s²)
    private static final float BRAKE_FORCE     = 300f;  // braking deceleration (px/s²)
    private static final float FRICTION        = 0.98f; // speed multiplier per frame (< 1 = drag)
    private static final float TURN_SPEED      = 180f;  // degrees per second at full speed
    private static final float MIN_TURN_SPEED_FACTOR = 0.15f; // turning at low speed fraction

    // ── Dimensions (for rendering and future collision) ───────────────
    public static final float WIDTH  = 30f;
    public static final float HEIGHT = 50f;

    // ── Visual ────────────────────────────────────────────────────────
    private final Color color;

    // ── Input flags (set externally each frame) ───────────────────────
    private boolean accelerating;
    private boolean braking;
    private boolean turningLeft;
    private boolean turningRight;

    /**
     * Creates a new Car at the given position facing upward.
     *
     * @param x     spawn X (center of the car)
     * @param y     spawn Y (center of the car)
     * @param color the car's body color
     */
    public Car(float x, float y, Color color) {
        this.position = new Vector2(x, y);
        this.rotation = 90f;  // facing upward by default
        this.speed    = 0f;
        this.velocity = new Vector2();
        this.color    = color;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  INPUT METHODS — called by GameScreen each frame
    // ═══════════════════════════════════════════════════════════════════

    /** Set the accelerate flag (W / UP). */
    public void setAccelerating(boolean value) { this.accelerating = value; }

    /** Set the brake/reverse flag (S / DOWN). */
    public void setBraking(boolean value)      { this.braking = value; }

    /** Set the turn-left flag (A / LEFT). */
    public void setTurningLeft(boolean value)   { this.turningLeft = value; }

    /** Set the turn-right flag (D / RIGHT). */
    public void setTurningRight(boolean value)  { this.turningRight = value; }

    // ═══════════════════════════════════════════════════════════════════
    //  PHYSICS UPDATE
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Updates the car's physics for one frame.
     *
     * @param delta time since last frame in seconds
     */
    public void update(float delta) {
        // ── 1. Acceleration / Braking ─────────────────────────────────
        if (accelerating) {
            speed += ACCELERATION * delta;
        }
        if (braking) {
            // If moving forward, brake. If stopped, reverse.
            if (speed > 0) {
                speed -= BRAKE_FORCE * delta;
            } else {
                speed -= ACCELERATION * 0.5f * delta; // reverse is slower
            }
        }

        // ── 2. Friction (applied every frame) ─────────────────────────
        speed *= FRICTION;

        // Stop completely if speed is negligible (prevents endless drift)
        if (Math.abs(speed) < 1f) {
            speed = 0f;
        }

        // ── 3. Clamp speed ────────────────────────────────────────────
        speed = MathUtils.clamp(speed, -MAX_REVERSE, MAX_SPEED);

        // ── 4. Turning ────────────────────────────────────────────────
        // Turn rate scales with speed — no turning when stationary.
        // At full speed, turn at TURN_SPEED degrees/sec.
        // At low speed, turn at a reduced rate.
        float speedFraction = Math.abs(speed) / MAX_SPEED;
        float effectiveTurnRate = TURN_SPEED * Math.max(speedFraction, speed != 0 ? MIN_TURN_SPEED_FACTOR : 0);

        if (turningLeft) {
            // If moving forward, left = CCW (+). If reversing, invert.
            rotation += effectiveTurnRate * delta * (speed >= 0 ? 1 : -1);
        }
        if (turningRight) {
            rotation -= effectiveTurnRate * delta * (speed >= 0 ? 1 : -1);
        }

        // Keep rotation in [0, 360) range
        rotation = ((rotation % 360f) + 360f) % 360f;

        // ── 5. Update position ────────────────────────────────────────
        // Convert speed + rotation into a velocity vector
        float radians = MathUtils.degreesToRadians * rotation;
        velocity.set(MathUtils.cos(radians) * speed, MathUtils.sin(radians) * speed);
        position.add(velocity.x * delta, velocity.y * delta);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  RENDERING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Draws the car as a colored rectangle with a white nose indicator.
     * Uses rotation around the car's center.
     *
     * @param renderer shared ShapeRenderer (must NOT be in begin/end state)
     */
    public void render(ShapeRenderer renderer) {
        // ── Car body (filled, rotated rectangle) ──────────────────────
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(color);
        renderer.identity();
        renderer.translate(position.x, position.y, 0);
        renderer.rotate(0, 0, 1, rotation - 90f); // -90 because rect draws upward and rotation 90° = up
        renderer.rect(-WIDTH / 2f, -HEIGHT / 2f, WIDTH, HEIGHT);

        // ── Nose indicator (white triangle at the front) ──────────────
        renderer.setColor(Color.WHITE);
        renderer.triangle(
                -WIDTH / 2f, HEIGHT / 2f,
                 WIDTH / 2f, HEIGHT / 2f,
                 0f,          HEIGHT / 2f + 10f
        );
        renderer.identity(); // reset transform
        renderer.end();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ACCESSORS
    // ═══════════════════════════════════════════════════════════════════

    /** Returns the center X position of the car. */
    public float getX() { return position.x; }

    /** Returns the center Y position of the car. */
    public float getY() { return position.y; }

    /** Returns the car's rotation in degrees. */
    public float getRotation() { return rotation; }

    /** Returns the car's current speed (can be negative if reversing). */
    public float getSpeed() { return speed; }

    /** Returns the car's body color. */
    public Color getColor() { return color; }

    /**
     * Clamps the car's position within the given track bounds.
     * Called after update() to prevent driving outside the world.
     *
     * @param trackWidth  total width of the track area
     * @param trackHeight total height of the track area
     */
    public void clampToTrack(float trackWidth, float trackHeight) {
        position.x = MathUtils.clamp(position.x, WIDTH / 2f, trackWidth  - WIDTH / 2f);
        position.y = MathUtils.clamp(position.y, HEIGHT / 2f, trackHeight - HEIGHT / 2f);
    }
}
