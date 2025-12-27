package aa.tulybaev.client.model.entity;

import aa.tulybaev.client.render.components.Animation;
import aa.tulybaev.client.render.components.SpriteLoader;

import java.awt.image.BufferedImage;

public final class RemotePlayer implements RenderablePlayer {

    private int x, y;
    private boolean facingRight;
    private int hp;
    private int hitFlashTimer = 0;
    private static final double SCALE = 0.25;
    private final int id;
    private int ammo = 25;
    private static final int MAX_AMMO = 25;

    // Анимации
    private final Animation idle;
    private final Animation walk;
    private final Animation jump;
    private Animation current;

    @Override
    public boolean isHit() {
        return hitFlashTimer > 0;
    }

    @Override
    public void advanceAnimation() {
        current.update();
        if (hitFlashTimer > 0) hitFlashTimer--;
    }

    public RemotePlayer(int id, boolean isLocal) {
        this.id = id;
        String prefix = isLocal ? "Player-1" : "Player-2";
        BufferedImage base = SpriteLoader.load("/sprites/" + (isLocal ? "Player1" : "Player2") + "/" + prefix + ".png");
        BufferedImage walk1 = SpriteLoader.load("/sprites/" + (isLocal ? "Player1" : "Player2") + "/" + prefix + "-walk-1.png");
        BufferedImage walk2 = SpriteLoader.load("/sprites/" + (isLocal ? "Player1" : "Player2") + "/" + prefix + "-walk-2.png");
        BufferedImage jumpImg = SpriteLoader.load("/sprites/" + (isLocal ? "Player1" : "Player2") + "/" + prefix + "-jump.png");

        idle = new Animation(new BufferedImage[]{base}, 30);
        walk = new Animation(new BufferedImage[]{walk1, walk2}, 10);
        jump = new Animation(new BufferedImage[]{jumpImg}, 30);

        current = idle;
    }

    public int id() {
        return id;
    }

    // ================= RENDER HELPERS =================

    public void setState(float x, float y, boolean facingRight, int hp, int ammo, boolean isMoving, boolean isOnGround) {
        if (hp < this.hp && hp >= 0) {
            this.hitFlashTimer = 10;
            new Thread(() ->
                    aa.tulybaev.client.render.components.SoundManager.play("/sounds/hit.wav")
            ).start();
        }
        this.x = (int) x;
        this.y = (int) y;
        this.facingRight = facingRight;
        this.hp = hp;
        this.ammo = ammo;
        updateAnimation(isMoving, isOnGround);
    }

    private void updateAnimation(boolean isMoving, boolean isOnGround) {
        if (!isOnGround) {
            current = jump;
        } else if (isMoving) {
            current = walk;
        } else {
            current = idle;
        }
    }

    // ================= GETTERS =================

    public int getAmmo() {
        return ammo;
    }

    public int getMaxAmmo() {
        return MAX_AMMO;
    }

    public int getMaxHp() {
        return 100; // или константа
    }

    public int getDrawX() { return x; }
    public int getDrawY() { return y; }

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

    public int getHp() {
        return hp;
    }
}
