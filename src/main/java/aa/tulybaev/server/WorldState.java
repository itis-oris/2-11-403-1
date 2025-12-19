package aa.tulybaev.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldState {

    private final List<ServerBullet> bullets =
            Collections.synchronizedList(new ArrayList<>());

    public static class PlayerState {
        public int id;
        public double x, y;
        public boolean facingRight;
        public int hp = 100;
        public int shootCooldown = 0;
    }

    private final Map<Integer, PlayerState> players = new ConcurrentHashMap<>();

    public PlayerState createPlayer(int id) {
        PlayerState p = new PlayerState();
        p.id = id;
        p.x = 200 + id * 100;
        p.y = 200;
        players.put(id, p);
        return p;
    }

    public Map<Integer, PlayerState> getPlayers() {
        return players;
    }

    public void spawnBullet(double x, double y, boolean facing, int ownerId) {
        ServerBullet b = new ServerBullet();
        b.x = x;
        b.y = y;
        b.vx = facing ? 25 : -25;
        b.ownerId = ownerId;
        bullets.add(b);
    }

    public void update() {
        updateCooldowns();
        updateBullets();
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

    public List<ServerBullet> getBullets() {
        return bullets;
    }
}
