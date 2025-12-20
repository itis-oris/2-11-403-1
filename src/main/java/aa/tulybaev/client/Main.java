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

        // 1. Загружаем состояние
        GameState gameState = GameStateStorage.load();
        System.out.println("Loaded lastPlayerId: " + gameState.lastPlayerId);

        // 2. Модель мира
        World world = new World();

        // 3. Буфер снапшотов
        SnapshotBuffer snapshotBuffer = new SnapshotBuffer();

        // 4. Ввод
        InputHandler input = new InputHandler();

        // 5. Сеть
        NetworkClient network = new NetworkClient(snapshotBuffer);
        network.setConnectionCallback(id -> {
            world.setLocalPlayerId(id);
            System.out.println("Local player ID set to: " + id);
        });

        // 6. UI
        GamePanel panel = new GamePanel(world);
        panel.addKeyListener(input);
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        GameFrame frame = new GameFrame(panel);
        frame.setVisible(true);

        // 7. Игровой цикл
        GameLoop loop = new GameLoop(
                world,
                panel,
                frame,
                network,
                snapshotBuffer,
                input
        );

        Thread gameThread = new Thread(loop, "GameLoop");
        gameThread.start();

        // ДОБАВЬ ЭТО: ждём, пока окно не закроется
        while (frame.isDisplayable()) {
            Thread.sleep(100);
        }

        // Завершение
        network.shutdown();
        gameThread.interrupt();


        // 8. Сохранение при выходе
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Сохраняем playerId, если подключились
            int playerId = network.getPlayerId();
            if (playerId >= 0) {
                gameState.lastPlayerId = playerId;
                GameStateStorage.save(gameState);
            }

            // Завершаем сетевой клиент и игру
            network.shutdown();
            gameThread.interrupt();
        }));
    }
}