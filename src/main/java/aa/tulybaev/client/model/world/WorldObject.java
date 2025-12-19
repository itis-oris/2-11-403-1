package aa.tulybaev.client.model.world;

import aa.tulybaev.client.model.entity.Bullet;
import java.awt.Graphics2D;

public interface WorldObject {

    void update();
    void render(Graphics2D g, int camX, int camY);

    boolean isSolid();
    boolean blocksBullets();
    void onBulletHit(Bullet b);

    int getX();
    int getY();
    int getW();
    int getH();

    boolean isAlive();
}
