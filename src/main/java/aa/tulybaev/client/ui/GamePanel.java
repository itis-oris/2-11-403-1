package aa.tulybaev.client.ui;

import aa.tulybaev.client.model.World;
import aa.tulybaev.client.render.Renderer;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    private final Renderer renderer;

    public GamePanel(World world) {
        this.renderer = new Renderer(world);

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(world.getInput());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.render((Graphics2D) g);
    }
}
