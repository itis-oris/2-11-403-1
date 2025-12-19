package aa.tulybaev.client;

import aa.tulybaev.client.model.World;
import aa.tulybaev.client.network.NetworkClient;
import aa.tulybaev.client.ui.GamePanel;

public class GameLoop implements Runnable {

    private static final int TPS = 60;

    private final World world;
    private final GamePanel panel;
    private final NetworkClient network;
    private int shootCooldown = 0;

    public GameLoop(World world, GamePanel panel, NetworkClient network) {
        this.world = world;
        this.panel = panel;
        this.network = network;
    }

    @Override
    public void run() {
        long nsPerTick = 1_000_000_000L / TPS;
        long last = System.nanoTime();

        while (true) {
            long now = System.nanoTime();

            if (now - last >= nsPerTick) {

                // ===== SHOOT =====
                if (shootCooldown > 0) shootCooldown--;

                if (world.getInput().shootPressed && shootCooldown == 0) {
                    network.sendShot();
                    shootCooldown = 10;
                    world.getInput().shootPressed = false;
                }

                // ===== SEND INPUT =====
                network.sendInput();

                // ===== LOCAL UPDATE =====
                world.update();

                // ===== RENDER =====
                panel.repaint();

                last = now;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
        }
    }

}
