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

    private final World world;
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
        // === 1. Фон (всегда рисуем) ===
        if (background != null) {
            int bgWidth = background.getWidth();
            // Используем камеру, даже если игрока нет — начальная позиция = 0
            int camX = camera.getX();
            int startX = -(camX % bgWidth);
            for (int x = startX; x < SCREEN_W; x += bgWidth) {
                g.drawImage(background, x, 0, null);
            }
        } else {
            // Резерв: красный фон, если спрайт не загрузился
            g.setColor(Color.RED);
            g.fillRect(0, 0, SCREEN_W, SCREEN_H);
        }

        // === 2. Обновляем камеру, только если есть цель ===
        RenderablePlayer cameraTarget = world.getCameraTarget();
        if (cameraTarget != null) {
            camera.follow(cameraTarget, SCREEN_W, SCREEN_H);
        }
        // Иначе камера остаётся в последнем положении (или 0)

        // === 3. Рисуем статичный мир (платформы, стены и т.д.) ===
        drawWorldObjects(g);

        // === 4. Рисуем динамические объекты (только если есть игроки) ===
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
            int y = (int) b.getY() - camera.getY();
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



    // ================= LOW-LEVEL =================

    private void drawPlayer(Graphics2D g, RenderablePlayer p) {

        // ВАЖНО: визуальное время живёт здесь
        p.advanceAnimation();

        BufferedImage img = p.getFrame();
        if (img == null) return;

        int w = p.getWidth();
        int h = p.getHeight();

        int x = p.getDrawX() - camera.getX();
        int y = p.getDrawY() - camera.getY();

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