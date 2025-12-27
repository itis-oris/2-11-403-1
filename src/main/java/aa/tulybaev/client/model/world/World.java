package aa.tulybaev.client.model.world;

import aa.tulybaev.client.core.InterpolatedSnapshot;
import aa.tulybaev.client.core.PlayerView;
import aa.tulybaev.client.model.entity.Bullet;
import aa.tulybaev.client.model.entity.RemotePlayer;
import aa.tulybaev.client.model.entity.RenderablePlayer;
import aa.tulybaev.client.model.world.objects.*;
import aa.tulybaev.protocol.messages.snapshots.BulletSnapshot;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class World {

    private int localPlayerId = -1;

    private final Map<Integer, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();

    private final Map<Integer, Bullet> bullets = new ConcurrentHashMap<>();

    private final List<WorldObject> objects = new ArrayList<>();
    private static final int GROUND_Y = 400;
    private static final int WORLD_WIDTH = 3000;

    public World() {
        buildLevel();
    }

    public void applyInterpolated(InterpolatedSnapshot snapshot) {

        for (PlayerView pv : snapshot.players().values()) {
            boolean isLocal = (pv.id() == localPlayerId);
            remotePlayers
                    .computeIfAbsent(pv.id(), id -> new RemotePlayer(id, isLocal))
                    .setState(pv.x(), pv.y(), pv.facingRight(), pv.hp(), pv.ammo(), pv.isMoving(), pv.isOnGround());
        }

        bullets.clear();
        for (BulletSnapshot bs : snapshot.bullets()) {
            bullets.put(bs.id(), new Bullet(bs.x(), bs.y(), bs.vx()));
        }
    }

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

        objects.add(new AmmoStation(800, GROUND_Y - 60));
        objects.add(new AmmoStation(1400, 240));
    }

    public List<RenderablePlayer> getRenderablePlayers() {
        return new ArrayList<>(remotePlayers.values());
    }

    public RemotePlayer getLocalPlayer() {
        return remotePlayers.get(localPlayerId);
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
        return remotePlayers.get(localPlayerId);
    }

    // ================= ID =================

    public void setLocalPlayerId(int id) {
        this.localPlayerId = id;
    }

    public int getLocalPlayerId() {
        return localPlayerId;
    }
}