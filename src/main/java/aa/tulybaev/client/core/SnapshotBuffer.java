package aa.tulybaev.client.core;


import aa.tulybaev.protocol.messages.snapshots.WorldSnapshotMessage;

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
            return null;
        }

        int targetTick = renderTick - INTERPOLATION_DELAY;

        WorldSnapshotMessage older = null;
        WorldSnapshotMessage newer = null;

        for (WorldSnapshotMessage s : buffer) {
            if (s.tick() <= targetTick) {
                older = s;
            } else {
                newer = s;
                break;
            }
        }

        if (older != null && newer != null) {
            float alpha = (float) (targetTick - older.tick()) / (float) (newer.tick() - older.tick());
            return new InterpolatedSnapshot(older, newer, alpha);
        } else if (older != null) {
            return new InterpolatedSnapshot(older, older, 0.0f);
        } else if (!buffer.isEmpty()) {
            WorldSnapshotMessage first = buffer.iterator().next();
            return new InterpolatedSnapshot(first, first, 0.0f);
        }

        return null;
    }
}
