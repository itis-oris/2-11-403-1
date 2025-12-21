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
    private static GameLoop gameLoop;
    private static Thread gameThread;
    private static NetworkClient network;
    private static GameState gameState;
    private static GamePanel currentPanel = null;

    public static void main(String[] args) throws Exception {
        gameState = GameStateStorage.load();
        System.out.println("Loaded lastPlayerId: " + gameState.lastPlayerId);

        // УДАЛИ всё, что связано с World, GamePanel здесь!
        frame = new GameFrame();
        frame.startGame = Main::startGameImpl;
        frame.restartGame = Main::restartGameImpl;

        frame.setVisible(true);
        frame.showMenu();
    }


    // Реализация GameFrame.startGame
    public static void startGameImpl() {
        try {
            World world = new World();
            SnapshotBuffer snapshotBuffer = new SnapshotBuffer();
            InputHandler input = new InputHandler();
            network = new NetworkClient(snapshotBuffer);
            network.setConnectionCallback(id -> {
                System.out.println("CLIENT: Local player ID set to: " + id);
                world.setLocalPlayerId(id);
            });

            // Используем ОДИН GamePanel
            if (currentPanel == null) {
                currentPanel = new GamePanel(world);
                currentPanel.addKeyListener(input);
                currentPanel.setFocusable(true);
                frame.setGamePanel(currentPanel);
            } else {
                currentPanel.setWorld(world);
            }

            gameLoop = new GameLoop(world, currentPanel, frame, network, snapshotBuffer, input);
            gameThread = new Thread(gameLoop, "GameLoop");
            gameThread.start();

            frame.showGame();
            SwingUtilities.invokeLater(() -> currentPanel.requestFocusInWindow());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Реализация GameFrame.restartGame
    public static void restartGameImpl() {
        // Останавливаем текущую игру
        if (gameThread != null) {
            gameThread.interrupt();
            gameThread = null;
        }
        if (network != null) {
            network.shutdown();
            network = null;
        }

        // Сохраняем результат
        if (network != null && network.getPlayerId() >= 0) {
            gameState.lastPlayerId = network.getPlayerId();
            GameStateStorage.save(gameState);
        }

        // Запускаем новую игру
        startGameImpl();
    }

    // Метод для GameOver из GameLoop
    public static void triggerGameOver() {
        SwingUtilities.invokeLater(() -> frame.showGameOver());
    }
}