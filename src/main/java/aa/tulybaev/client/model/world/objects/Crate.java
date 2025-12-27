package aa.tulybaev.client.model.world.objects;

import aa.tulybaev.client.model.entity.Bullet;

import java.awt.*;

public class Crate implements WorldObject {

    private final int x, y, w = 100, h = 100;
    private final boolean alive = true; // всегда жив

    public Crate(int x, int y) {
        this.x = x;
        this.y = y - h; // чтобы стоял на земле
    }

    @Override
    public void update() {
        // Ничего не делаем
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        g.setColor(new Color(139, 69, 19));
        g.fillRect(x - camX, y - camY, w, h);

        g.setColor(new Color(101, 67, 33));
        for (int i = 0; i < w; i += 20) {
            g.drawLine(x - camX + i, y - camY, x - camX + i, y - camY + h);
        }
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public boolean blocksBullets() {
        return true;
    }

    @Override
    public void onBulletHit(Bullet b) {
        // ничего не делаем — неразрушаемый
    }

    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }
    @Override
    public int getW() { return w; }
    @Override
    public int getH() { return h; }

    @Override
    public boolean isAlive() {
        return true; // всегда активен
    }
}