package aa.tulybaev.client;

import aa.tulybaev.client.core.GameLoop;
import aa.tulybaev.client.core.SnapshotBuffer;
import aa.tulybaev.client.input.InputHandler;
import aa.tulybaev.client.model.world.World;
import aa.tulybaev.client.network.NetworkClient;
import aa.tulybaev.client.ui.GameFrame;
import aa.tulybaev.client.ui.GamePanel;

import javax.swing.*;

public class Main {

    private static GameFrame frame;
    private static Thread gameThread;
    private static NetworkClient network;
    private static GamePanel currentPanel = null;
    private static InputHandler input;

    public static void main(String[] args) {
        frame = new GameFrame();
        frame.startGame = Main::startGameImpl;
        frame.restartGame = Main::restartGameImpl;

        frame.setVisible(true);
        frame.showMenu();
    }


    public static void startGameImpl() {
        try {
            World world = new World();
            SnapshotBuffer snapshotBuffer = new SnapshotBuffer();
            InputHandler input = new InputHandler();
            NetworkClient network = new NetworkClient(snapshotBuffer);

            network.setConnectionCallback(id -> world.setLocalPlayerId(id));

            GamePanel panel = new GamePanel(world);
            panel.addKeyListener(input);
            panel.setFocusable(true);

            currentPanel = panel;
            Main.input = input;
            Main.network = network;

            frame.setGamePanel(panel);
            GameLoop loop = new GameLoop(world, panel, network, snapshotBuffer, input);
            gameThread = new Thread(loop, "GameLoop");
            gameThread.start();

            frame.showGame();
            SwingUtilities.invokeLater(() -> panel.requestFocusInWindow());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void restartGameImpl() {
        if (gameThread != null) {
            gameThread.interrupt();
            gameThread = null;
        }
        if (network != null) {
            network.shutdown();
            network = null;
        }

        if (currentPanel != null) {
            currentPanel = null;
        }

        startGameImpl();
    }

    public static void triggerGameOver() {
        SwingUtilities.invokeLater(() -> frame.showGameOver());
    }

    public static void triggerVictory() {
        SwingUtilities.invokeLater(() -> frame.showVictory());
    }
}