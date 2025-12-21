package aa.tulybaev.client.core;

import aa.tulybaev.protocol.BulletSnapshot;
import aa.tulybaev.protocol.PlayerSnapshot;
import aa.tulybaev.protocol.WorldSnapshotMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Интерполированное состояние мира
 * Используется ТОЛЬКО для отрисовки
 */
public final class InterpolatedSnapshot {

    private final Map<Integer, PlayerView> players = new HashMap<>();
    private final List<BulletSnapshot> bullets = new ArrayList<>();


    public InterpolatedSnapshot(
            WorldSnapshotMessage a,
            WorldSnapshotMessage b,
            float alpha
    ) {
        // ===== PLAYERS =====
        for (PlayerSnapshot p1 : a.players()) {
            PlayerSnapshot p2 = findPlayer(b, p1.id());
            if (p2 == null) continue;

            float x = lerp(p1.x(), p2.x(), alpha);
            float y = lerp(p1.y(), p2.y(), alpha);

            players.put(
                    p1.id(),
                    new PlayerView(
                            p1.id(),
                            x,
                            y,
                            p2.facingRight(),
                            p2.hp(),
                            p2.ammo(), // ← новое
                            p2.isMoving(),
                            p2.isOnGround()
                    )
            );
        }

        // ===== BULLETS =====
        // Пули не интерполируем — просто берём из нового снапшота
        this.bullets.addAll(b.bullets());
    }
    private PlayerSnapshot findPlayer(WorldSnapshotMessage snap, int id) {
        for (PlayerSnapshot p : snap.players()) {
            if (p.id() == id) return p;
        }
        return null;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public Map<Integer, PlayerView> players() {
        return players;
    }

    public List<BulletSnapshot> bullets() { // ← новое
        return bullets;
    }
}

