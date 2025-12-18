package aa.tulybaev.client;

import aa.tulybaev.client.model.World;
import aa.tulybaev.client.ui.GamePanel;

public class GameLoop implements Runnable {

    private static final int TPS = 60;
    private final World world;
    private final GamePanel panel;

    public GameLoop(World world, GamePanel panel) {
        this.world = world;
        this.panel = panel;
    }

    @Override
    public void run() {
        long nsPerTick = 1_000_000_000 / TPS;
        long last = System.nanoTime();

        while (true) {
            long now = System.nanoTime();
            if (now - last >= nsPerTick) {
                world.update();
                panel.repaint();
                last = now;
            }
        }
    }
}
