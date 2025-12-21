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
        public double vx, vy; // ← новые поля для скорости
        public boolean facingRight;
        public int hp = 100;
        public int shootCooldown = 0;
        public boolean onGround = false; // ← новое поле
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
        p.vx = 0;
        p.vy = 0;
        System.out.println("Created player " + id + " at (" + p.x + ", " + p.y + ")");
        p.onGround = false;
        players.put(id, p);
        return p;
    }

    public Collection<PlayerState> getPlayers() {
        return players.values();
    }

    // ================= INPUT =================

    public void applyInput(int playerId, float dx, boolean jump, boolean shoot) {
        PlayerState p = players.get(playerId);
        if (p == null) return;

        if (dx > 0) {
            p.facingRight = true;
        } else if (dx < 0) {
            p.facingRight = false;
        }

        p.vx = dx * 6;

        // Прыжок
        if (jump && p.onGround) {
            p.vy = -12;
            p.onGround = false;
        }

        // Стрельба
        if (shoot && p.shootCooldown == 0) {
            spawnBullet(p);
            p.shootCooldown = 15;
        }
    }

    // ================= UPDATE =================

    public void update() {
        updateCooldowns();
        updatePhysics();
        updateBullets();
    }

    private void updatePhysics() {
        for (PlayerState p : players.values()) {
            // Гравитация
            if (!p.onGround) {
                p.vy += 0.6; // гравитация
            } else {
                p.vy = 0;
            }

            // Применяем скорость
            p.x += p.vx;
            p.y += p.vy;

            // Простая коллизия с землёй (GROUND_Y = 400, высота игрока = 64)
            if (p.y >= 400 - 64) {
                p.y = 400 - 64;
                p.vy = 0;
                p.onGround = true;
            } else {
                p.onGround = false;
            }

            // Трение (останавливаем по горизонтали)
            p.vx *= 0.8;
            if (Math.abs(p.vx) < 0.1) p.vx = 0;
        }
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

            // Удаление за пределами экрана
            if (b.x < -100 || b.x > 3000) {
                it.remove();
                continue;
            }

            // Проверка попадания
            for (PlayerState p : players.values()) {
                if (p.id == b.ownerId) continue; // нельзя стрелять в себя

                if (hit(b, p)) {
                    p.hp -= 10;
                    if (p.hp <= 0) p.hp = 0;
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
        ServerBullet b = new ServerBullet(p.x, p.y, p.facingRight ? 25 : -25, p.id);
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
