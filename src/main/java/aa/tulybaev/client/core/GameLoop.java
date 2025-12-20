package aa.tulybaev.client.core;

import aa.tulybaev.client.input.InputHandler;
import aa.tulybaev.client.model.entity.RemotePlayer;
import aa.tulybaev.client.model.world.World;
import aa.tulybaev.client.network.NetworkClient;
import aa.tulybaev.client.ui.GameFrame;
import aa.tulybaev.client.ui.GamePanel;

public final class GameLoop implements Runnable {

    private static final int FPS = 60;

    private final World world;
    private final GamePanel panel;
    private final NetworkClient network;
    private final SnapshotBuffer snapshotBuffer;
    private final InputHandler input;
    private final GameFrame frame;

    private int renderTick = 0;

    public GameLoop(
            World world,
            GamePanel panel,
            GameFrame frame,          // ← новое
            NetworkClient network,
            SnapshotBuffer snapshotBuffer,
            InputHandler input
    ) {
        this.world = world;
        this.panel = panel;
        this.frame = frame;       // ← сохраняем
        this.network = network;
        this.snapshotBuffer = snapshotBuffer;
        this.input = input;
    }

    @Override
    public void run() {
        long nsPerFrame = 1_000_000_000L / FPS;
        long last = System.nanoTime();

        while (!Thread.currentThread().isInterrupted()) {
            long now = System.nanoTime();

            if (now - last >= nsPerFrame) {
                renderTick++;

                // 1. Собираем ввод
                float dx = input.getDx();
                float dy = input.getDy();
                boolean shoot = input.consumeShoot();

                // 2. Отправляем только ввод (если подключены)
                if (network.getPlayerId() >= 0) {
                    network.sendInput(dx, dy, shoot);
                }

                // 3. Применяем снапшоты для рендера
                InterpolatedSnapshot snap = snapshotBuffer.getInterpolated(renderTick);
                if (snap != null) {
                    System.out.println("Applying snapshot with " + snap.players().size() + " players");
                    world.applyInterpolated(snap);
                } else {
                    System.out.println("No snapshot available yet");
                }

                // 4. Обновляем HUD (берём данные из world)
                RemotePlayer local = world.getLocalPlayer();
                if (local != null) {
                    panel.setHudData(local.getHp(), local.getMaxHp(), 100, false);
                }

                // 5. Перерисовка
                panel.repaint();

                last = now;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}