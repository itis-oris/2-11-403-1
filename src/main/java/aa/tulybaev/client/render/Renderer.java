package aa.tulybaev.client.render;

import aa.tulybaev.client.model.entity.Bullet;
import aa.tulybaev.client.model.entity.RenderablePlayer;
import aa.tulybaev.client.model.world.World;
import aa.tulybaev.client.model.world.objects.WorldObject;
import aa.tulybaev.client.render.components.Camera;
import aa.tulybaev.client.render.components.HudRenderer;
import aa.tulybaev.client.render.components.SpriteLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Render-pass.
 * Отвечает ТОЛЬКО за отрисовку текущего состояния мира.
 */
public final class Renderer {

    private static final int SCREEN_W = 960;
    private static final int SCREEN_H = 540;

    private World world;
    private final Camera camera;
    private final HudRenderer hud;

    private final BufferedImage background;
    private int hudHp, hudMaxHp, hudAmmo;
    private boolean hudIsShooting;


    public Renderer(World world) {
        this.world = world;
        this.camera = new Camera();
        this.hud = new HudRenderer();
        this.background = SpriteLoader.load("/backgrounds/forest.png");
    }

    // ================= ENTRY POINT =================

    // ================= ENTRY POINT =================

    public void render(Graphics2D g) {
        // === 0. Обновляем камеру СРАЗУ ===
        RenderablePlayer cameraTarget = world.getCameraTarget();
        if (cameraTarget != null) {
            camera.follow(cameraTarget, SCREEN_W, SCREEN_H);
        }

        // === 1. Фон (с актуальной камерой) ===
        if (background != null) {
            int bgWidth = background.getWidth();
            int camX = camera.getX(); // ← Теперь camX = 200
            int startX = -(camX % bgWidth);
            for (int x = startX; x < SCREEN_W; x += bgWidth) {
                g.drawImage(background, x, 0, null);
            }
        } else {
            g.setColor(Color.RED);
            g.fillRect(0, 0, SCREEN_W, SCREEN_H);
        }

        // === 2. Рисуем остальное с той же камерой ===
        drawWorldObjects(g);
        if (cameraTarget != null) {
            drawPlayers(g);
            drawBullets(g);
            drawHud(g);
        }
    }

    // ================= DRAW SECTIONS =================

    private void drawBackground(Graphics2D g) {
        int bgWidth = background.getWidth();
        int startX = -(camera.getX() % bgWidth);

        for (int x = startX; x < SCREEN_W; x += bgWidth) {
            g.drawImage(background, x, 0, null);
        }
    }

    private void drawWorldObjects(Graphics2D g) {
        for (WorldObject o : world.getObjects()) {
            o.render(g, camera.getX(), camera.getY());
        }
    }

    private void drawPlayers(Graphics2D g) {
        List<RenderablePlayer> players = world.getRenderablePlayers();
        for (RenderablePlayer p : players) {
            drawPlayer(g, p);
        }
    }

    private void drawBullets(Graphics2D g) {
        g.setColor(Color.YELLOW);
        for (Bullet b : world.getBullets()) {
            int x = (int) b.getX() - camera.getX();
            int y = (int) b.getY() - camera.getY() + 60;
            g.fillOval(x, y, 6, 6);
        }
    }

    public void setHudData(int hp, int maxHp, int ammo, boolean isShooting) {
        this.hudHp = hp;
        this.hudMaxHp = maxHp;
        this.hudAmmo = ammo;
        this.hudIsShooting = isShooting;
    }

    private void drawHud(Graphics2D g) {
        hud.render(g, hudHp, hudMaxHp, hudAmmo, hudIsShooting);
    }


    public void setWorld(World newWorld) {
        this.world = newWorld;
    }



    // ================= LOW-LEVEL =================

    private void drawPlayer(Graphics2D g, RenderablePlayer p) {

        // ВАЖНО: визуальное время живёт здесь
        p.advanceAnimation();

        BufferedImage img = p.getFrame();
        if (img == null) return;

        int drawX = p.getDrawX();
        int drawY = p.getDrawY();
        int camX = camera.getX();
        int camY = camera.getY();

        int w = p.getWidth();
        int h = p.getHeight();

        int x = p.getDrawX() - camera.getX();
        int y = p.getDrawY() - camera.getY();

        System.out.println("Player: drawX=" + drawX + ", drawY=" + drawY);
        System.out.println("Camera: camX=" + camX + ", camY=" + camY);
        System.out.println("Render at: x=" + x + ", y=" + y);
        System.out.println("---");

        if (p.isHit()) {
            g.setColor(new Color(255, 0, 0, 120));
            g.fillRect(x, y, w, h);
        }

        if (p.isFacingRight()) {
            g.drawImage(img, x, y, w, h, null);
        } else {
            g.drawImage(img, x + w, y, -w, h, null);
        }
    }
}