package aa.tulybaev.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Серверное состояние мира.
 * Единственный источник истины.
 */
public final class WorldState {

    // ================= PLAYERS =================

    public static final class PlayerState {
        public int id;
        public double x, y;
        public boolean facingRight;
        public int hp = 100;
        public int shootCooldown = 0;
    }

    private final Map<Integer, PlayerState> players =
            new ConcurrentHashMap<>();

    // ================= BULLETS =================

    private final List<ServerBullet> bullets =
            new ArrayList<>();

    // ================= PLAYER =================

    public PlayerState createPlayer(int id) {
        PlayerState p = new PlayerState();
        p.id = id;
        p.x = 200 + id * 100;
        p.y = 200;
        players.put(id, p);
        return p;
    }

    public Collection<PlayerState> getPlayers() {
        return players.values();
    }

    // ================= INPUT =================

    public void applyInput(
            int playerId,
            float dx,
            float dy,
            boolean shoot
    ) {
        PlayerState p = players.get(playerId);
        if (p == null) return;

        // movement
        p.x += dx * 6;
        p.y += dy * 8;

        if (dx != 0) {
            p.facingRight = dx > 0;
        }

        // shooting
        if (shoot && p.shootCooldown == 0) {
            spawnBullet(p);
            p.shootCooldown = 15;
        }
    }

    // ================= UPDATE =================

    public void update() {
        updateCooldowns();
//        updateBullets();
    }

    private void updateCooldowns() {
        for (PlayerState p : players.values()) {
            if (p.shootCooldown > 0) {
                p.shootCooldown--;
            }
        }
    }

    private void updateBullets() {
        Iterator<ServerBullet> it = bullets.iterator();
        while (it.hasNext()) {
            ServerBullet b = it.next();
            b.x += b.vx;

            for (PlayerState p : players.values()) {
                if (p.id == b.ownerId) continue;

                if (hit(b, p)) {
                    p.hp -= 10;
                    it.remove();
                    break;
                }
            }
        }
    }

    private boolean hit(ServerBullet b, PlayerState p) {
        double dx = Math.abs(b.x - p.x);
        double dy = Math.abs(b.y - p.y);
        return dx < 40 && dy < 60;
    }

    // ================= BULLETS =================

    private void spawnBullet(PlayerState p) {
        ServerBullet b = new ServerBullet();
        b.x = p.x;
        b.y = p.y;
        b.vx = p.facingRight ? 25 : -25;
        b.ownerId = p.id;
        bullets.add(b);
    }

    public void removePlayer(int id) {
        players.remove(id);
        // Опционально: удали пули этого игрока
        bullets.removeIf(b -> b.ownerId == id);
    }

    public List<ServerBullet> getBullets() {
        return bullets;
    }
}
