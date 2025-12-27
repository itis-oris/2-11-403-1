package aa.tulybaev.client.core;

import aa.tulybaev.client.input.InputHandler;
import aa.tulybaev.client.model.entity.Bullet;
import aa.tulybaev.client.model.entity.RemotePlayer;
import aa.tulybaev.client.model.world.World;
import aa.tulybaev.client.network.NetworkClient;
import aa.tulybaev.client.render.components.SoundManager;
import aa.tulybaev.client.ui.GameFrame;
import aa.tulybaev.client.ui.GamePanel;

import static aa.tulybaev.client.Main.triggerGameOver;

public final class GameLoop implements Runnable {

    private static final int FPS = 60;

    private final World world;
    private final GamePanel panel;
    private final NetworkClient network;
    private final SnapshotBuffer snapshotBuffer;
    private final InputHandler input;

    private int renderTick = 0;

    public GameLoop(
            World world,
            GamePanel panel,
            NetworkClient network,
            SnapshotBuffer snapshotBuffer,
            InputHandler input
    ) {
        this.world = world;
        this.panel = panel;
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
                boolean jump = input.consumeJump();
                boolean shoot = input.consumeShoot();

                // 2. Отправляем только ввод
                if (network.getPlayerId() >= 0) {
                    network.sendInput(dx, jump, shoot);
                }

                if (shoot) {
                    SoundManager.play("/sounds/sound-fire.wav");
                }

                if (jump) {
                    SoundManager.play("/sounds/jump.wav");
                }

                // 3. Применяем снапшоты для рендера
                InterpolatedSnapshot snap = snapshotBuffer.getInterpolated(renderTick);
                if (snap != null) {
                    world.applyInterpolated(snap);
                    world.getBullets().removeIf(bullet -> !bullet.isAlive());
                    for (Bullet b : world.getBullets()) {
                        b.update(); // для плавности между снапшотами
                    }
                } else {
                    System.out.println("No snapshot available yet");
                }

                // 4. Обновляем HUD (берём данные из world)
                RemotePlayer local = world.getLocalPlayer();
                if (local != null) {
                    if (local.getHp() <= 0) {
                        triggerGameOver();
                    }
                    panel.setHudData(
                            local.getHp(),
                            local.getMaxHp(),
                            local.getAmmo(),
                            shoot
                    );
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