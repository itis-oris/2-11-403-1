package aa.tulybaev.client.model.entity;

import aa.tulybaev.client.render.Animation;
import aa.tulybaev.client.render.SpriteLoader;

import java.awt.image.BufferedImage;

public class RemotePlayer {

    private double x, y;
    private double targetX, targetY;

    private boolean facingRight = true;
    private int hp = 100;

    private static final double SCALE = 0.25;
    private static final double INTERPOLATION = 0.12;

    // ===== ANIMATIONS =====
    private final Animation idle;
    private final Animation walk;
    private Animation current;

    public RemotePlayer(int id) {

        BufferedImage idleImg =
                SpriteLoader.load("/sprites/Player2/Player-2.png");

        BufferedImage walk1 =
                SpriteLoader.load("/sprites/Player2/Player-2-walk-1.png");
        BufferedImage walk2 =
                SpriteLoader.load("/sprites/Player2/Player-2-walk-2.png");

        idle = new Animation(new BufferedImage[]{idleImg}, 30);
        walk = new Animation(new BufferedImage[]{walk1, walk2}, 10);

        current = idle;
    }

    // ===== SERVER UPDATE =====
    public void updateFromServer(double x, double y, boolean facingRight, int hp) {
        this.targetX = x;
        this.targetY = y;
        this.facingRight = facingRight;
        this.hp = hp;
    }

    // ===== CLIENT UPDATE (INTERPOLATION) =====
    public void update() {
        x += (targetX - x) * INTERPOLATION;
        y += (targetY - y) * INTERPOLATION;

        double dx = Math.abs(targetX - x);
        current = dx > 1 ? walk : idle;

        current.update();
    }

    // ===== GETTERS =====
    public int getDrawX() { return (int) x; }
    public int getDrawY() { return (int) y; }

    public int getWidth() {
        return (int) (current.getFrame().getWidth() * SCALE);
    }

    public int getHeight() {
        return (int) (current.getFrame().getHeight() * SCALE);
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public BufferedImage getFrame() {
        return current.getFrame();
    }
}
