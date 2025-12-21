package aa.tulybaev.client.model.world;

import aa.tulybaev.client.core.InterpolatedSnapshot;
import aa.tulybaev.client.core.PlayerView;
import aa.tulybaev.client.model.entity.Bullet;
import aa.tulybaev.client.model.entity.RemotePlayer;
import aa.tulybaev.client.model.entity.RenderablePlayer;
import aa.tulybaev.client.model.world.objects.*;
import aa.tulybaev.protocol.BulletSnapshot;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Клиентский мир.
 * Хранит ТОЛЬКО визуальное состояние для рендера.
 * Не содержит логики, физики, input или сетевых решений.
 */
public final class World {

    // ================= PLAYERS =================

    private int localPlayerId = -1;

    /** Визуальное представление локального игрока (рендерится как RemotePlayer) */
    private RemotePlayer localPlayer;

    /** Визуальные представления удалённых игроков */
    private final Map<Integer, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();

    // ================= BULLETS =================

    /** Визуальные пули (из snapshot’ов) */
    private final Map<Integer, Bullet> bullets = new ConcurrentHashMap<>();

    // ================= LEVEL =================

    private final List<WorldObject> objects = new ArrayList<>();
    private static final int GROUND_Y = 400;
    private static final int WORLD_WIDTH = 3000;

    // ================= INIT =================

    public World() {
        buildLevel();
    }

    // ================= SNAPSHOT =================

    /**
     * Применяет интерполированное состояние мира.
     * ЕДИНСТВЕННАЯ точка изменения состояния.
     */
    public void applyInterpolated(InterpolatedSnapshot snapshot) {

        remotePlayers.clear();
        // ===== PLAYERS =====
        for (PlayerView pv : snapshot.players().values()) {
            if (pv.id() == localPlayerId) {
                if (localPlayer == null) {
                    localPlayer = new RemotePlayer(pv.id());
                }
                localPlayer.setState(pv.x(), pv.y(), pv.facingRight(), pv.hp(), pv.isMoving(), pv.isOnGround());
            } else {
                remotePlayers
                        .computeIfAbsent(pv.id(), RemotePlayer::new)
                        .setState(pv.x(), pv.y(), pv.facingRight(), pv.hp(), pv.isMoving(), pv.isOnGround());
            }
        }

        // ===== BULLETS =====
        bullets.clear();
        for (BulletSnapshot bs : snapshot.bullets()) {
            bullets.put(bs.id(), new Bullet(bs.x(), bs.y(), bs.vx()));
        }
    }

    // ================= LEVEL =================

    private void buildLevel() {
        objects.add(new Platform(0, GROUND_Y, WORLD_WIDTH, 40));

        objects.add(new Wall(300, GROUND_Y - 120, 80, 120));
        objects.add(new Wall(900, GROUND_Y - 160, 80, 160));
        objects.add(new Wall(2100, GROUND_Y - 140, 80, 140));

        objects.add(new Platform(200, 280, 300, 20));
        objects.add(new Platform(600, 200, 250, 20));
        objects.add(new Platform(1000, 260, 350, 20));
        objects.add(new Platform(1500, 220, 300, 20));
        objects.add(new Platform(1900, 300, 300, 20));

        objects.add(new Cover(450, GROUND_Y - 100, 80, 100));
        objects.add(new Cover(1200, GROUND_Y - 120, 100, 120));
        objects.add(new Cover(1750, GROUND_Y - 100, 80, 100));

        objects.add(new Crate(650, GROUND_Y - 40));
        objects.add(new Crate(690, GROUND_Y - 40));
        objects.add(new Crate(1300, GROUND_Y - 40));
        objects.add(new Crate(1600, GROUND_Y - 40));
        objects.add(new Crate(1650, GROUND_Y - 40));
    }

    // ================= RENDER ACCESS =================

    public List<RenderablePlayer> getRenderablePlayers() {
        List<RenderablePlayer> list = new ArrayList<>();
        if (localPlayer != null) {
            list.add(localPlayer);
        }
        list.addAll(remotePlayers.values());
        return list;
    }

    public RemotePlayer getLocalPlayer() {
        return localPlayer;
    }

    public Collection<RemotePlayer> getRemotePlayers() {
        return remotePlayers.values();
    }

    public Collection<Bullet> getBullets() {
        return bullets.values();
    }

    public List<WorldObject> getObjects() {
        return objects;
    }

    public RemotePlayer getCameraTarget() {
        return localPlayer;
    }

    // ================= META =================

    public int getGroundY() {
        return GROUND_Y;
    }

    public List<Platform> getPlatforms() {
        return objects.stream()
                .filter(obj -> obj instanceof Platform)
                .map(obj -> (Platform) obj)
                .toList(); // или collect(Collectors.toList()), если Java < 16
    }

    public int getWorldWidth() {
        return WORLD_WIDTH;
    }

    // ================= ID =================

    public void setLocalPlayerId(int id) {
        this.localPlayerId = id;
    }

    public int getLocalPlayerId() {
        return localPlayerId;
    }
}