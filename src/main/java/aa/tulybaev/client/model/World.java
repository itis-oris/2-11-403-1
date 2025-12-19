package aa.tulybaev.client.model;

import aa.tulybaev.client.model.entity.Bullet;
import aa.tulybaev.client.model.entity.Player;
import aa.tulybaev.client.model.entity.RemotePlayer;
import aa.tulybaev.client.model.input.InputHandler;
import aa.tulybaev.client.model.world.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class World {

    private final Map<Integer, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();


    // ================= CORE =================
    private int localPlayerId = -1;
    private final Player player;
    private final InputHandler input = new InputHandler();
    private final List<Bullet> bullets = new ArrayList<>();

    // ================= LEVEL =================
    private final List<WorldObject> objects = new ArrayList<>();

    private static final int GROUND_Y = 400;
    private static final int WORLD_WIDTH = 3000;

    // ================= INIT =================
    public World() {
        player = new Player(
                100,
                GROUND_Y - PLAYER_HEIGHT()
        );

        buildLevel();
    }

    // ================= LEVEL BUILD =================
    private void buildLevel() {

        // ======== GROUND ========
        objects.add(new Platform(0, GROUND_Y, WORLD_WIDTH, 40));

        // ======== WALLS (indestructible) ========
        objects.add(new Wall(300, GROUND_Y - 120, 80, 120));
        objects.add(new Wall(900, GROUND_Y - 160, 80, 160));
        objects.add(new Wall(2100, GROUND_Y - 140, 80, 140));

        // ======== PLATFORMS (vertical gameplay) ========
        objects.add(new Platform(200, 280, 300, 20));
        objects.add(new Platform(600, 200, 250, 20));
        objects.add(new Platform(1000, 260, 350, 20));
        objects.add(new Platform(1500, 220, 300, 20));
        objects.add(new Platform(1900, 300, 300, 20));

        // ======== COVERS ========
        objects.add(new Cover(450, GROUND_Y - 100, 80, 100));
        objects.add(new Cover(1200, GROUND_Y - 120, 100, 120));
        objects.add(new Cover(1750, GROUND_Y - 100, 80, 100));

        // ======== DESTRUCTIBLE CRATES ========
        objects.add(new Crate(650, GROUND_Y - 40));
        objects.add(new Crate(690, GROUND_Y - 40));
        objects.add(new Crate(1300, GROUND_Y - 40));
        objects.add(new Crate(1600, GROUND_Y - 40));
        objects.add(new Crate(1650, GROUND_Y - 40));
    }

    private int PLAYER_HEIGHT() {
        return 150; // логическая высота персонажа
    }

    // ================= UPDATE =================
    public void update() {

        // ----- Player -----
        player.update(
                input,
                bullets,
                GROUND_Y,
                getSolidPlatforms()
        );

        // ----- World objects -----
        objects.forEach(WorldObject::update);
        objects.removeIf(o -> !o.isAlive());

        // ----- Bullets -----
        bullets.forEach(Bullet::update);
        bullets.removeIf(b -> !b.isAlive());

        // ----- Bullet collisions -----
        bullets.removeIf(this::handleBulletCollision);

        // ----- Remote players interpolation -----
        for (RemotePlayer rp : remotePlayers.values()) {
            rp.update();
        }
    }

    // ================= COLLISIONS =================
    private boolean handleBulletCollision(Bullet b) {
        for (WorldObject o : objects) {
            if (!o.blocksBullets()) continue;

            if (b.getX() >= o.getX() &&
                    b.getX() <= o.getX() + o.getW() &&
                    b.getY() >= o.getY() &&
                    b.getY() <= o.getY() + o.getH()
            ) {
                o.onBulletHit(b);
                return true;
            }
        }
        return false;
    }

    private List<Platform> getSolidPlatforms() {
        List<Platform> result = new ArrayList<>();

        for (WorldObject o : objects) {
            if (o.isSolid() && o instanceof Platform p) {
                result.add(p);
            }
        }
        return result;
    }

    public void updateRemotePlayer(
            int id,
            double x,
            double y,
            boolean facingRight,
            int hp
    ) {
        remotePlayers
                .computeIfAbsent(id, RemotePlayer::new)
                .updateFromServer(x, y, facingRight, hp);
    }

    public void syncBulletsFromServer(String data) {
        bullets.clear();

        String[] parts = data.split(";");
        for (String s : parts) {
            if (s.isEmpty()) continue;

            String[] d = s.split(",");
            double x = Double.parseDouble(d[0]);
            double y = Double.parseDouble(d[1]);

            bullets.add(new Bullet(x, y, 0)); // vx клиенту не нужен
        }
    }



    // ================= GETTERS =================
    public Player getPlayer() {
        return player;
    }

    public InputHandler getInput() {
        return input;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public List<WorldObject> getObjects() {
        return objects;
    }

    public Map<Integer, RemotePlayer> getRemotePlayers() {
        return remotePlayers;
    }


    public int getGroundY() {
        return GROUND_Y;
    }

    public int getWorldWidth() {
        return WORLD_WIDTH;
    }

    public void setLocalPlayerId(int id) {
        this.localPlayerId = id;
    }

    public int getLocalPlayerId() {
        return localPlayerId;
    }

}
