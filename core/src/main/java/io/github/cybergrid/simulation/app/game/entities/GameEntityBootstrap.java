package io.github.cybergrid.simulation.app.game.entities;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

import io.github.cybergrid.simulation.app.game.events.GameEventBus;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.managers.EntityManager;

public final class GameEntityBootstrap {

    private enum Key {
        PLAYER(EntityType.CONTROLLABLE),
        ENEMY(EntityType.AI),
        PROJECTILE(EntityType.PROJECTILE),
        MOVING_FIREWALL(EntityType.AI),
        FIREWALL_STATIC(EntityType.STATIC),
        SERVER_BOSS(EntityType.STATIC),
        SECURE_SERVER(EntityType.TRIGGER),
        VULNERABLE_SERVER(EntityType.TRIGGER),
        EXIT_TRIGGER(EntityType.TRIGGER),
        OBFUSCATED_CODE_TRIGGER(EntityType.TRIGGER);

        final EntityType baseType;

        Key(EntityType baseType) {
            this.baseType = baseType;
        }
    }

    private static final class FactoryDefinition {
        private final EntityType baseType;
        private final BiFunction<Integer, EntityType, Entity> factory;

        private FactoryDefinition(EntityType baseType, BiFunction<Integer, EntityType, Entity> factory) {
            this.baseType = baseType;
            this.factory = factory;
        }
    }

    private static final Map<Key, FactoryDefinition> DEFINITIONS = new EnumMap<>(Key.class);

    private GameEntityBootstrap() {
    }

    public static void initialize(EngineContext context, GameEventBus gameEventBus) {
        DEFINITIONS.clear();

        register(Key.PLAYER,                  (id, type) -> new PlayerEntity(context, id, type));
        register(Key.ENEMY,                   (id, type) -> new EnemyEntity(context, id, type));
        register(Key.PROJECTILE,              (id, type) -> new BulletEntity(context, id, type, gameEventBus));
        register(Key.MOVING_FIREWALL,         (id, type) -> new FirewallEntity(context, id, type));
        register(Key.FIREWALL_STATIC,         (id, type) -> new FirewallStaticEntity(context, id, type));
        register(Key.SERVER_BOSS,             (id, type) -> new ServerBossEntity(context, id, type));
        register(Key.SECURE_SERVER,           (id, type) -> new SecureServerEntity(context, id, type));
        register(Key.VULNERABLE_SERVER,       (id, type) -> new VulnerableServerEntity(context, id, type, gameEventBus));
        register(Key.EXIT_TRIGGER,            (id, type) -> new ExitTriggerEntity(context, id, type));
        register(Key.OBFUSCATED_CODE_TRIGGER, (id, type) -> new ObfuscatedCodeEntity(context, id, type));

    }

    // --- Named factory methods ---

    public static int createPlayer(EngineContext context) {
        return create(context, Key.PLAYER);
    }

    public static int createEnemy(EngineContext context) {
        return create(context, Key.ENEMY);
    }

    public static int createBullet(EngineContext context) {
        return create(context, Key.PROJECTILE);
    }

    public static int createMovingFirewall(EngineContext context) {
        return create(context, Key.MOVING_FIREWALL);
    }

    public static int createStaticFirewall(EngineContext context) {
        return create(context, Key.FIREWALL_STATIC);
    }

    public static int createServerBoss(EngineContext context) {
        return create(context, Key.SERVER_BOSS);
    }

    public static int createSecureServer(EngineContext context) {
        return create(context, Key.SECURE_SERVER);
    }

    public static int createVulnerableServer(EngineContext context) {
        return create(context, Key.VULNERABLE_SERVER);
    }

    public static int createExitTrigger(EngineContext context) {
        return create(context, Key.EXIT_TRIGGER);
    }

    public static int createObfuscatedCode(EngineContext context) {
        return create(context, Key.OBFUSCATED_CODE_TRIGGER);
    }

    // --- Internal ---

    private static int create(EngineContext context, Key key) {
        FactoryDefinition definition = DEFINITIONS.get(key);
        if (definition == null) {
            throw new IllegalStateException("No game entity factory registered for key: " + key);
        }
        EntityManager entityManager = context.getEntityManager();
        entityManager.registerFactory(definition.baseType, definition.factory);
        return entityManager.createEntity(definition.baseType);
    }

    private static void register(Key key, BiFunction<Integer, EntityType, Entity> factory) {
        DEFINITIONS.put(key, new FactoryDefinition(key.baseType, factory));
    }
}
