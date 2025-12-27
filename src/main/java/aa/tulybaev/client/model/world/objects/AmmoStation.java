package aa.tulybaev.client.model.world.objects;

import aa.tulybaev.client.model.entity.Bullet;

import java.awt.*;

public class AmmoStation implements WorldObject {
    private final int x, y, w = 50, h = 50;
    private boolean isActive = true;
    private int timer = 0;

    public AmmoStation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void update() {
        if (!isActive) {
            timer++;
            if (timer > 180) { // 3 секунды (60 FPS * 3)
                isActive = true;
                timer = 0;
            }
        }
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        if (!isActive) return;

        // Мигающий эффект
        int alpha = 180 + (int)(70 * Math.sin(System.currentTimeMillis() / 200.0));
        g.setColor(new Color(255, 215, 0, alpha));
        g.fillRoundRect(x - camX, y - camY, w, h, 10, 10);
        g.setColor(Color.YELLOW);
        g.drawRoundRect(x - camX, y - camY, w, h, 10, 10);

        // Иконка патронов
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("A", x - camX + 15, y - camY + 30);
    }

    @Override
    public boolean isSolid() {
        return false; // игрок проходит сквозь
    }

    @Override
    public boolean blocksBullets() {
        return false; // пули проходят
    }

    @Override
    public void onBulletHit(Bullet b) {
        // ничего не делаем
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
        return true; // всегда активна (с перезарядкой)
    }

    // ================= ВЗАИМОДЕЙСТВИЕ =================

    public boolean refillAmmo() {
        if (isActive) {
            isActive = false;
            timer = 0;
            return true;
        }
        return false;
    }

    public boolean isActive() {
        return isActive;
    }
}