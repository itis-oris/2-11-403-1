package aa.tulybaev.client.core;

import aa.tulybaev.protocol.WorldSnapshotMessage;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Хранит последние снапшоты от сервера
 * и отдаёт интерполированное состояние для рендера
 */
public final class SnapshotBuffer {

    // сколько снапшотов держим (обычно 2–5 хватает)
    private static final int MAX_SNAPSHOTS = 10;

    // искусственная задержка (в тиках сервера)
    private static final int INTERPOLATION_DELAY = 0;

    private final Deque<WorldSnapshotMessage> buffer = new ArrayDeque<>();

    /**
     * Добавляется из network-потока
     */
    public synchronized void push(WorldSnapshotMessage snapshot) {
        buffer.addLast(snapshot);

        while (buffer.size() > MAX_SNAPSHOTS) {
            buffer.removeFirst();
        }
    }

    /**
     * Вызывается из GameLoop / render-потока
     */
    public synchronized InterpolatedSnapshot getInterpolated(int renderTick) {
        if (buffer.isEmpty()) {
            System.out.println("CLIENT: Snapshot buffer is empty");
            return null;
        }

        int targetTick = renderTick - INTERPOLATION_DELAY;
        System.out.println("CLIENT: renderTick=" + renderTick + ", targetTick=" + targetTick);

        WorldSnapshotMessage older = null;
        WorldSnapshotMessage newer = null;

        for (WorldSnapshotMessage s : buffer) {
            System.out.println("  Buffer entry: tick=" + s.tick());
            if (s.tick() <= targetTick) {
                older = s;
            } else {
                newer = s;
                break;
            }
        }

        if (older != null && newer != null) {
            System.out.println("CLIENT: Interpolating between tick " + older.tick() + " and " + newer.tick());
            float alpha = (float) (targetTick - older.tick()) / (float) (newer.tick() - older.tick());
            return new InterpolatedSnapshot(older, newer, alpha);
        } else if (older != null) {
            System.out.println("CLIENT: Using older snapshot (tick=" + older.tick() + ")");
            return new InterpolatedSnapshot(older, older, 0.0f);
        } else if (!buffer.isEmpty()) {
            WorldSnapshotMessage first = buffer.iterator().next();
            System.out.println("CLIENT: Using first snapshot (tick=" + first.tick() + ")");
            return new InterpolatedSnapshot(first, first, 0.0f);
        }

        System.out.println("CLIENT: No valid snapshots found");
        return null;
    }
}
