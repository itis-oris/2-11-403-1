package aa.tulybaev.client.ui;

import aa.tulybaev.client.GameLoop;
import aa.tulybaev.client.model.World;
import aa.tulybaev.client.render.Renderer;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    private final World world;
    private final Renderer renderer;
    private GameLoop loop;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();

        world = new World();
        renderer = new Renderer(world);

        addKeyListener(world.getInput());
    }

    public void start() {
        loop = new GameLoop(world, this);
        new Thread(loop).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.render((Graphics2D) g);
    }
}
