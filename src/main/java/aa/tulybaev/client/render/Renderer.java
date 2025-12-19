package aa.tulybaev.client.render;

import aa.tulybaev.client.model.World;
import aa.tulybaev.client.model.entity.Bullet;
import aa.tulybaev.client.model.entity.Player;
import aa.tulybaev.client.model.entity.RemotePlayer;
import aa.tulybaev.client.model.world.WorldObject;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Renderer {

    private final World world;
    private final Camera camera = new Camera();
    private final HudRenderer hud = new HudRenderer();

    private final BufferedImage forestBg =
            SpriteLoader.load("/backgrounds/forest.png");

    public Renderer(World world) {
        this.world = world;
    }

    public void render(Graphics2D g) {

        // ===== CAMERA =====
        camera.update(world.getPlayer(), 960, 540);

        // ===== BACKGROUND =====
        drawBackground(g);

        // ===== WORLD OBJECTS =====
        for (WorldObject o : world.getObjects()) {
            o.render(g, camera.getX(), camera.getY());
        }

        // ===== REMOTE PLAYERS =====
        drawRemotePlayers(g);

        // ===== LOCAL PLAYER =====
        drawPlayer(g, world.getPlayer());

        // ===== BULLETS =====
        drawBullets(g);

        // ===== UI =====
        hud.render(g, world.getPlayer());

        drawRemotePlayers(g);
    }

    // ================= DRAW METHODS =================

    private void drawBackground(Graphics2D g) {
        int bgWidth = forestBg.getWidth();
        int screenW = 960;

        int startX = -(camera.getX() % bgWidth);

        for (int x = startX; x < screenW; x += bgWidth) {
            g.drawImage(forestBg, x, 0, null);
        }
    }

    private void drawPlayer(Graphics2D g, Player p) {
        var img = p.getFrame();
        if (img == null) return;

        int w = p.getWidth();
        int h = p.getHeight();

        int screenX = p.getDrawX() - camera.getX();
        int screenY = p.getDrawY() - camera.getY();

        if (p.isFacingRight()) {
            g.drawImage(img, screenX, screenY, w, h, null);
        } else {
            g.drawImage(img, screenX + w, screenY, -w, h, null);
        }

        // muzzle flash
        if (p.isShooting()) {
            g.setColor(new Color(255, 220, 120, 200));
            int fx = screenX + (p.isFacingRight() ? w : -7);
            int fy = screenY + h / 2 + 5;
            g.fillOval(fx, fy, 12, 12);
        }
    }

    private void drawRemotePlayers(Graphics2D g) {
        for (RemotePlayer p : world.getRemotePlayers().values()) {

            BufferedImage img = p.getFrame();
            if (img == null) continue;

            int w = p.getWidth();
            int h = p.getHeight();

            int x = p.getDrawX() - camera.getX();
            int y = p.getDrawY() - camera.getY();

            if (p.isFacingRight()) {
                g.drawImage(img, x, y, w, h, null);
            } else {
                g.drawImage(img, x + w, y, -w, h, null);
            }
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
}
