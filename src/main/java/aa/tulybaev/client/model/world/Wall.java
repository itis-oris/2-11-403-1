package aa.tulybaev.client.model.world;

import aa.tulybaev.client.model.entity.Bullet;
import java.awt.*;

public class Wall implements WorldObject {

    private final int x, y, w, h;

    public Wall(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }

    public void update() {}

    public void render(Graphics2D g, int camX, int camY) {
        g.setColor(new Color(80, 80, 80));
        g.fillRect(x - camX, y - camY, w, h);
    }

    public boolean isSolid() { return true; }
    public boolean blocksBullets() { return true; }

    public void onBulletHit(Bullet b) {}

    public int getX() { return x; }
    public int getY() { return y; }
    public int getW() { return w; }
    public int getH() { return h; }

    public boolean isAlive() { return true; }
}
