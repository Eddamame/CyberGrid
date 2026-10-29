package io.github.cybergrid.simulation.app.game;

public final class GameConstants {
    private GameConstants() {
        // Utility class
    }

    // Shared world dimensions
    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;

    // Movement and combat
    public static final float PLAYER_SPEED          = 300f;
    public static final float ENEMY_SPEED           = 700f;
    public static final float ENEMY_MAX_SPEED       = 1500f;

    // Firewalls speed
    public static final float[] FIREWALL_SPEEDS     = { 1200f, 1000f, 900f, 800f };

    // Bullets Logic
    public static final float BULLET_SPEED          = 600f;
    public static final float BULLET_RANGE          = 400f;
    public static final float SHOOT_COOLDOWN        = 0.55f;

    // Knockback Logic
    public static final float PLAYER_KNOCKBACK      = 0f;
    public static final float ENEMY_KNOCKBACK       = 800f;
    public static final float FIREWALL_BOUNCE_FORCE = 350f;

    // World boundary margin (used for firewall waypoint offsets)
    public static final float WORLD_MARGIN          = 45f;

    // Game limits
    public static final int MAX_BULLETS           = 9999;
    public static final int PLAYER_MAX_HEALTH     = 5;
    public static final int BOSS_MAX_HEALTH       = 30;
    public static final int ENEMY_ENRAGE_HEALTH_THRESHOLD = BOSS_MAX_HEALTH / 2;

    // Damage values
    public static final int BULLET_DAMAGE            = 1;
    public static final int ENEMY_COLLISION_DAMAGE   = 1;

    // Enemy behaviour
    public static final float ENEMY_ENRAGED_SPEED_MULTIPLIER = 0.8f;

    // Scene-specific speed multipliers
    public static final float ESCAPE_PLAYER_SPEED_MULTIPLIER  = 1.12f;
    public static final float ESCAPE_ENEMY_SPEED_MULTIPLIER   = 0.90f;
    public static final float ATTACK_FIREWALL_SPEED_MULTIPLIER = 0.7f;

    // Escape obfuscation mechanic
    public static final float OBFUSCATION_SHIELD_SECONDS = 5f;
    public static final float OBFUSCATED_CODE_LIFETIME_SECONDS = 8f;
    public static final float OBFUSCATED_CODE_SPAWN_MIN_SECONDS = 11f;
    public static final float OBFUSCATED_CODE_SPAWN_MAX_SECONDS = 19f;
}
