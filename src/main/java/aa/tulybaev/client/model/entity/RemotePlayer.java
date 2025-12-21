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

    // ===== ANIMATIONS =====
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

    public RemotePlayer(int id) {
        this.id = id;
        BufferedImage idleImg = SpriteLoader.load("/sprites/Player2/Player-2.png");
        BufferedImage walk1 = SpriteLoader.load("/sprites/Player2/Player-2-walk-1.png");
        BufferedImage walk2 = SpriteLoader.load("/sprites/Player2/Player-2-walk-2.png");
        BufferedImage jumpImg = SpriteLoader.load("/sprites/Player2/Player-2-jump.png");

        idle = new Animation(new BufferedImage[]{idleImg}, 30);
        walk = new Animation(new BufferedImage[]{walk1, walk2}, 10);
        jump = new Animation(new BufferedImage[]{jumpImg}, 30);

        current = idle;
    }

    public int id() {
        return id;
    }

    // ================= APPLY SNAPSHOT =================

    public void setState(
            int x,
            int y,
            boolean facingRight,
            int hp
    ) {
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.hp = hp;

        current = idle; // default
    }

    // ================= RENDER HELPERS =================

    public void setState(float x, float y, boolean facingRight, int hp, boolean isMoving, boolean isOnGround) {
        System.out.println("RemotePlayer.setState: x=" + x + ", y=" + y + ", hp=" + hp);
        if (hp < this.hp && hp >= 0) {
            this.hitFlashTimer = 10;

            new Thread(() ->
                    aa.tulybaev.client.render.components.SoundManager.play("/sounds/hit.wav")
            ).start();
        }
        this.x = (int) x; // ← ДОБАВЬ ЭТО
        this.y = (int) y; // ← ДОБАВЬ ЭТО
        this.facingRight = facingRight; // ← ДОБАВЬ ЭТО
        this.hp = hp;
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

    public void setState(
            float x,
            float y,
            boolean facingRight,
            int hp
    ) {
        this.x = (int) x;
        this.y = (int) y;
        this.facingRight = facingRight;
        this.hp = hp;
    }


}
