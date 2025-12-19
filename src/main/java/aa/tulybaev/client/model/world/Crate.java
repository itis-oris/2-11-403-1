package aa.tulybaev.client.model.world;

import aa.tulybaev.client.model.entity.Bullet;
import java.awt.*;

public class Crate implements WorldObject {

    private int x, y, w, h;
    private int hp = 15;
    private boolean alive = true;

    public Crate(int x, int y) {
        this.x = x;
        this.y = y-50;
        this.w = 100;
        this.h = 100;
    }

    public void update() {}

    public void render(Graphics2D g, int camX, int camY) {
        g.setColor(new Color(139, 69, 19));
        g.fillRect(x - camX, y - camY, w, h);

        // трещины
        g.setColor(Color.BLACK);
        g.drawString("" + hp, x - camX + 15, y - camY + 25);
    }

    public boolean isSolid() { return true; }
    public boolean blocksBullets() { return true; }

    public void onBulletHit(Bullet b) {
        hp--;
        if (hp <= 0) alive = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getW() { return w; }
    public int getH() { return h; }

    public boolean isAlive() { return alive; }
}
