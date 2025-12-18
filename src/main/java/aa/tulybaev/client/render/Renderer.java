package aa.tulybaev.client.render;

import aa.tulybaev.client.model.World;
import aa.tulybaev.client.model.entity.Bullet;
import aa.tulybaev.client.model.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Renderer {

    private final World world;
    private final Camera camera = new Camera();
    private final BufferedImage forestBg =
            SpriteLoader.load("/backgrounds/forest.png");
    private static final int GROUND_Y = 512;

    public Renderer(World world) {
        this.world = world;
    }

    public void render(Graphics2D g) {
        camera.update(world.getPlayer(), 960, 540);

        drawBackground(g);
        drawGround(g);
        drawPlayer(g, world.getPlayer());
        drawBullets(g);
        g.setColor(Color.WHITE);
        g.drawString(
                "Ammo: " + world.getPlayer().getAmmo(),
                20,
                20
        );
    }

    private void drawGround(Graphics2D g) {
        g.setColor(new Color(60, 120, 60)); // зелёная земля

        int y = GROUND_Y - camera.getY();
        g.fillRect(0, y, 960, 200);
    }


    private void drawBackground(Graphics2D g) {
        int bgWidth = forestBg.getWidth();
        int screenW = 960;

        // фон повторяется по X
        int startX = -(camera.getX() % bgWidth);

        for (int x = startX; x < screenW; x += bgWidth) {
            g.drawImage(
                    forestBg,
                    x,
                    0,
                    null
            );
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

        if (p.isShooting()) {
            g.setColor(new Color(255, 220, 120, 200));

            int fx = screenX + (p.isFacingRight() ? w : -10);
            int fy = screenY + h / 2 + 7;

            g.fillOval(fx, fy, 12, 12);
        }
    }



    private void drawBullets(Graphics2D g) {
        g.setColor(Color.YELLOW);
        for (Bullet b : world.getBullets()) {
            g.fillOval((int)b.getX(), (int)b.getY(), 6, 6);
        }
    }
}
