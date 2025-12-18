package aa.tulybaev.client.model.entity;

import aa.tulybaev.client.model.input.InputHandler;
import aa.tulybaev.client.render.Animation;
import aa.tulybaev.client.render.SoundManager;
import aa.tulybaev.client.render.SpriteLoader;

import java.awt.image.BufferedImage;
import java.util.List;

public class Player {

    private static final double SCALE = 0.25;
    private double x, y;
    private double vx, vy;
    private boolean onGround;
    private boolean facingRight = true;
    private static final int MAX_AMMO = 1000;
    private static final int FIRE_COOLDOWN = 10;
    private static final double BULLET_SPEED = 35;
    private int muzzleFlashTimer = 0;
    private static final double MUZZLE_X = 0.85;
    private static final double MUZZLE_Y = -0.75;

    private int ammo = MAX_AMMO;
    private int fireCooldown = 0;

    private final Animation idle;
    private final Animation walk;
    private final Animation jump;
    private Animation current;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;

        BufferedImage idleImg =
                SpriteLoader.load("/sprites/Player1/Player-1.png");

        BufferedImage walk1 =
                SpriteLoader.load("/sprites/Player1/Player-1-walk-1.png");
        BufferedImage walk2 =
                SpriteLoader.load("/sprites/Player1/Player-1-walk-2.png");

        BufferedImage jumpImg =
                SpriteLoader.load("/sprites/Player1/Player-1-jump.png");

        idle = new Animation(new BufferedImage[]{idleImg}, 30);
        walk = new Animation(new BufferedImage[]{walk1, walk2}, 10);
        jump = new Animation(new BufferedImage[]{jumpImg}, 30);

        current = idle;
    }

    public void update(InputHandler input, List<Bullet> bullets, int groundY) {
        vx = 0;

        if (input.left) vx = -4;
        if (input.right) vx = 4;

        if (input.jumpPressed && onGround) {
            vy = -12;
            onGround = false;
            input.jumpPressed = false;
        }

        vy += 0.6;
        x += vx;
        y += vy;

        if (y >= groundY) {
            y = groundY;
            vy = 0;
            onGround = true;
        }

        if (!onGround) current = jump;
        else if (vx != 0) current = walk;
        else current = idle;

        if (vx > 0) facingRight = true;
        if (vx < 0) facingRight = false;

        if (fireCooldown > 0) fireCooldown--;

        if (input.shootPressed && fireCooldown == 0 && ammo > 0) {
            shoot(bullets);
            fireCooldown = FIRE_COOLDOWN;
            ammo--;
            input.shootPressed = false;
        }

        if (muzzleFlashTimer > 0) muzzleFlashTimer--;

        current.update();
    }

    private void shoot(List<Bullet> bullets) {
        muzzleFlashTimer = 5;
        double dir = facingRight ? 1 : -1;

        double muzzleWorldX;
        double muzzleWorldY;

        double w = getWidth();
        double h = getHeight();

        if (facingRight) {
            muzzleWorldX = x + w * MUZZLE_X;
        } else {
            muzzleWorldX = x + w * (1 - MUZZLE_X);
        }

        muzzleWorldY = y + h * MUZZLE_Y;

        bullets.add(new Bullet(
                muzzleWorldX,
                muzzleWorldY,
                (facingRight ? 1 : -1) * BULLET_SPEED
        ));

        // ОТБРОС
        vx -= dir * 2.5;

        SoundManager.play("/sounds/sound-fire.wav");
    }



    public BufferedImage getFrame() {
        return current.getFrame();
    }

    public int getDrawX() { return (int) x; }
    public int getDrawY() { return (int) y; }

    public int getWidth() {
        return (int) (getFrame().getWidth() * SCALE);
    }

    public int getHeight() {
        return (int) (getFrame().getHeight() * SCALE);
    }

    public double getScale() {
        return SCALE;
    }

    public boolean isShooting() {
        return muzzleFlashTimer > 0;
    }

    public int getAmmo() {
        return ammo;
    }

    public boolean isFacingRight() {
        return facingRight;
    }
}
