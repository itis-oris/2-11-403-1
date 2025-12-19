package aa.tulybaev.client.model.world;

import aa.tulybaev.client.model.entity.Bullet;

import java.awt.*;

public class Platform implements WorldObject {

    private final int x, y, w, h;

    public Platform(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // ===== UPDATE =====
    @Override
    public void update() {
        // Платформа статична
    }

    // ===== RENDER =====
    @Override
    public void render(Graphics2D g, int camX, int camY) {
        g.setColor(new Color(90, 70, 50)); // дерево / земля
        g.fillRect(
                x - camX,
                y - camY,
                w,
                h
        );
    }

    // ===== COLLISION FLAGS =====
    @Override
    public boolean isSolid() {
        return true; // игрок может стоять
    }

    @Override
    public boolean blocksBullets() {
        return false; // пули пролетают
    }

    @Override
    public void onBulletHit(Bullet b) {
        // ничего не делаем
    }

    // ===== GEOMETRY =====
    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getW() {
        return w;
    }

    @Override
    public int getH() {
        return h;
    }

    // ===== LIFE =====
    @Override
    public boolean isAlive() {
        return true; // платформы не исчезают
    }
}
