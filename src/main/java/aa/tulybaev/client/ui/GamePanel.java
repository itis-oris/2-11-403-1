package aa.tulybaev.client.ui;

import aa.tulybaev.client.model.world.World;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final aa.tulybaev.client.render.Renderer renderer;
    private int hudHp, hudMaxHp, hudAmmo;
    private boolean hudIsShooting;

    // Фиксированный размер игрового окна
    private static final int GAME_WIDTH = 960;
    private static final int GAME_HEIGHT = 540;

    public GamePanel(World world) {
        this.renderer = new aa.tulybaev.client.render.Renderer(world);
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.BLACK); // чтобы видеть панель, если рендер не работает
    }

    public void setHudData(int hp, int maxHp, int ammo, boolean isShooting) {
        this.hudHp = hp;
        this.hudMaxHp = maxHp;
        this.hudAmmo = ammo;
        this.hudIsShooting = isShooting;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        renderer.setHudData(hudHp, hudMaxHp, hudAmmo, hudIsShooting);
        renderer.render(g2d);

        g2d.dispose();
    }
}