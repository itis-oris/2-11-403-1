package aa.tulybaev.client.model.world;

import aa.tulybaev.client.model.entity.Bullet;

import java.awt.*;

public class Cover implements WorldObject {

    private final int x, y, w, h;

    public Cover(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // ===== UPDATE =====
    @Override
    public void update() {
        // укрытие статично
    }

    // ===== RENDER =====
    @Override
    public void render(Graphics2D g, int camX, int camY) {
        g.setColor(new Color(60, 100, 60)); // куст / дерево
        g.fillRect(
                x - camX,
                y - camY,
                w,
                h
        );
    }

    // ===== COLLISIONS =====
    @Override
    public boolean isSolid() {
        return true; // игрок не проходит
    }

    @Override
    public boolean blocksBullets() {
        return true; // пули останавливаются
    }

    @Override
    public void onBulletHit(Bullet b) {
        // пока не разрушается
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
        return true; // не исчезает
    }
}
