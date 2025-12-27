package aa.tulybaev.server;

import aa.tulybaev.client.model.world.objects.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Серверное состояние мира.
 * Единственный источник истины.
 */
public final class WorldState {

    private static final int PLAYER_WIDTH = 65;
    private static final int PLAYER_HEIGHT = 65;
    private static final int GROUND_Y = 400;
    private static final int WORLD_WIDTH = 3000;

    private Server server;

    private final Map<Integer, PlayerState> players = new ConcurrentHashMap<>();
    private final List<ServerBullet> bullets = new ArrayList<>();
    private final List<WorldObject> worldObjects = new ArrayList<>(); // ← ДОБАВЛЕНО


    public WorldState(Server server) {
        this.server = server; // ← сохраняем ссылку
        buildLevel();
    }


    private void buildLevel() {
        worldObjects.add(new Platform(0, GROUND_Y, WORLD_WIDTH, 40));

        worldObjects.add(new Wall(300, GROUND_Y - 120, 80, 120));
        worldObjects.add(new Wall(900, GROUND_Y - 160, 80, 160));
        worldObjects.add(new Wall(2100, GROUND_Y - 140, 80, 140));

        worldObjects.add(new Platform(200, 280, 300, 20));
        worldObjects.add(new Platform(600, 200, 250, 20));
        worldObjects.add(new Platform(1000, 260, 350, 20));
        worldObjects.add(new Platform(1500, 220, 300, 20));
        worldObjects.add(new Platform(1900, 300, 300, 20));

        worldObjects.add(new Cover(450, GROUND_Y - 100, 80, 100));
        worldObjects.add(new Cover(1200, GROUND_Y - 120, 100, 120));
        worldObjects.add(new Cover(1750, GROUND_Y - 100, 80, 100));

        worldObjects.add(new Crate(650, GROUND_Y - 40));
        worldObjects.add(new Crate(690, GROUND_Y - 40));
        worldObjects.add(new Crate(1300, GROUND_Y - 40));
        worldObjects.add(new Crate(1600, GROUND_Y - 40));
        worldObjects.add(new Crate(1650, GROUND_Y - 40));

        // Добавь в список объектов:
        worldObjects.add(new AmmoStation(800, GROUND_Y - 60));
        worldObjects.add(new AmmoStation(1400, 240)); // на платформе
    }


    public PlayerState createPlayer(int id) {
        PlayerState p = new PlayerState();
        p.id = id;

        if (id % 2 == 1) {
            p.x = 200;
        } else {
            p.x = WORLD_WIDTH - 300;
        }
        p.y = 200;

        players.put(id, p);
        return p;
    }

    public Collection<PlayerState> getPlayers() {
        return players.values();
    }



    public void applyInput(int playerId, float dx, boolean jump, boolean shoot) {
        PlayerState p = players.get(playerId);
        if (p == null) return;

        if (dx > 0) {
            p.facingRight = true;
        } else if (dx < 0) {
            p.facingRight = false;
        }

        p.vx = dx * 6;

        if (jump && p.onGround) {
            p.vy = -12;
            p.onGround = false;
        }

        if (shoot && p.shootCooldown == 0 && p.ammo > 0) {
            spawnBullet(p);
            p.shootCooldown = 15;
            p.ammo--; // ← уменьшаем патроны
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
            // Защита от вылета за границы
            if (p.x < -100 || p.x > WORLD_WIDTH + 100 || p.y < -100 || p.y > GROUND_Y + 500) {
                p.x = 200;
                p.y = 200;
                p.vx = 0;
                p.vy = 0;
                p.onGround = false;
            }

            // 1. Горизонтальное движение
            p.x += p.vx;

            // 2. Гравитация с ограничением скорости
            p.vy += 0.6;
            if (p.vy > 15) p.vy = 15; // предотвращает tunneling

            p.y += p.vy;

            // 3. Сбрасываем onGround
            p.onGround = false;

            // 4. Обработка коллизий со всеми solid-объектами
            for (WorldObject obj : worldObjects) {
                if (!obj.isAlive() || !obj.isSolid()) continue;

                double playerRight = p.x + PLAYER_WIDTH;
                double playerBottom = p.y + PLAYER_HEIGHT;
                double objectRight = obj.getX() + obj.getW();
                double objectBottom = obj.getY() + obj.getH();

                // Проверка пересечения
                if (playerRight > obj.getX() && p.x < objectRight &&
                        playerBottom > obj.getY() && p.y < objectBottom) {

                    // === ПЛАТФОРМА: проходима снизу ===
                    if (obj instanceof Platform && p.vy >= 0 && p.y < obj.getY()) {
                        p.y = obj.getY() - PLAYER_HEIGHT;
                        p.vy = 0;
                        p.onGround = true;
                        break; // одна платформа за раз
                    }
                    // === СТЕНЫ / ЯЩИКИ: полная коллизия с выталкиванием ===
                    else {
                        // Вычисляем перекрытия
                        double overlapLeft = playerRight - obj.getX();     // слева
                        double overlapRight = objectRight - p.x;           // справа
                        double overlapTop = playerBottom - obj.getY();     // сверху
                        double overlapBottom = objectBottom - p.y;         // снизу

                        // Находим минимальное перекрытие
                        double minHorizontal = Math.min(overlapLeft, overlapRight);
                        double minVertical = Math.min(overlapTop, overlapBottom);
                        double minOverlap = Math.min(minHorizontal, minVertical);

                        // Выталкиваем в сторону минимального перекрытия
                        if (minOverlap == overlapLeft && overlapLeft > 0) {
                            p.x = obj.getX() - PLAYER_WIDTH;
                            p.vx = 0;
                        } else if (minOverlap == overlapRight && overlapRight > 0) {
                            p.x = objectRight;
                            p.vx = 0;
                        } else if (minOverlap == overlapTop && overlapTop > 0) {
                            p.y = obj.getY() - PLAYER_HEIGHT;
                            p.vy = 0;
                            p.onGround = true;
                        } else if (minOverlap == overlapBottom && overlapBottom > 0) {
                            p.y = objectBottom;
                            p.vy = 0;
                        }
                    }
                }
            }

            // Проверка взаимодействия с AmmoStation
            for (WorldObject obj : worldObjects) {
                if (obj instanceof AmmoStation station && station.isActive()) {
                    if (p.x + PLAYER_WIDTH > station.getX() &&
                            p.x < station.getX() + station.getW() &&
                            p.y + PLAYER_HEIGHT > station.getY() &&
                            p.y < station.getY() + station.getH()) {

                        if (station.refillAmmo()) {
                            p.refillAmmo();
                        }
                    }
                }
            }

            // 5. Защита от падения в бездну
            if (p.y > GROUND_Y + 200) {
                p.y = GROUND_Y - PLAYER_HEIGHT;
                p.vy = 0;
                p.onGround = true;
            }

            // 6. Трение по горизонтали
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

            if (b.x < -100 || b.x > WORLD_WIDTH + 100) {
                it.remove();
                continue;
            }

            // Коллизия с объектами
            for (WorldObject obj : worldObjects) {
                if (obj.blocksBullets() && hitBulletObject(b, obj)) {
                    it.remove();
                    break;
                }
            }

            // Коллизия с игроками
            if (!bullets.contains(b)) continue; // если уже удалена
            for (PlayerState p : players.values()) {
                if (p.id == b.ownerId) continue;
                if (hit(b, p)) {
                    p.hp -= 10;
                    if (p.hp <= 0) {
                        p.hp = 0;
                        // ОТПРАВИТЬ GAME_OVER
                        server.onPlayerKilled(b.ownerId, p.id);

                        server.removePlayer(p.id);
                    }
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

    private boolean hitBulletObject(ServerBullet b, WorldObject obj) {
        return b.x >= obj.getX() && b.x <= obj.getX() + obj.getW() &&
                b.y >= obj.getY() && b.y <= obj.getY() + obj.getH();
    }

    // ================= BULLETS =================

    private void spawnBullet(PlayerState p) {
        ServerBullet b = new ServerBullet(p.x, p.y, p.facingRight ? 25 : -25, p.id);
        bullets.add(b);
    }

    public void removePlayer(int id) {
        players.remove(id);
        bullets.removeIf(b -> b.ownerId == id);
    }

    public List<ServerBullet> getBullets() {
        return bullets;
    }
}