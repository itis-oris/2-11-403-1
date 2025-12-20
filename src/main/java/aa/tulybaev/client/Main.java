package aa.tulybaev.client;

import aa.tulybaev.client.core.GameLoop;
import aa.tulybaev.client.core.SnapshotBuffer;
import aa.tulybaev.client.input.InputHandler;
import aa.tulybaev.client.model.world.World;
import aa.tulybaev.client.network.NetworkClient;
import aa.tulybaev.client.ui.GameFrame;
import aa.tulybaev.client.ui.GamePanel;

public class Main {

    public static void main(String[] args) throws Exception {
        World world = new World();
        SnapshotBuffer snapshotBuffer = new SnapshotBuffer();
        InputHandler input = new InputHandler();
        NetworkClient network = new NetworkClient(snapshotBuffer); // ← новый TCP-клиент

        GamePanel panel = new GamePanel(world);
        panel.addKeyListener(input);
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        GameFrame frame = new GameFrame(panel);
        frame.setVisible(true);

        GameLoop loop = new GameLoop(world, panel, network, snapshotBuffer, input);
        Thread gameThread = new Thread(loop, "GameLoop");
        gameThread.start();

        // Завершение при закрытии
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            network.shutdown();
            gameThread.interrupt();
        }));
    }
}
