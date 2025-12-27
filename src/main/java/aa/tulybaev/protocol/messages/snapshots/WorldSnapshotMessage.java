package aa.tulybaev.protocol.messages.snapshots;


import aa.tulybaev.protocol.core.GameMessage;
import aa.tulybaev.protocol.core.MessageType;

import java.io.*;
import java.util.*;

public final class WorldSnapshotMessage implements GameMessage {

    private final int tick;
    private final List<PlayerSnapshot> players;
    private final List<BulletSnapshot> bullets;

    public WorldSnapshotMessage(int tick, List<PlayerSnapshot> players, List<BulletSnapshot> bullets) {
        this.tick = tick;
        this.players = List.copyOf(players);
        this.bullets = List.copyOf(bullets);
    }

    @Override
    public MessageType type() {
        return MessageType.SNAPSHOT;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(tick);
        out.writeInt(players.size());
        for (PlayerSnapshot p : players) {
            p.write(out);
        }
        // Пули:
        out.writeInt(bullets.size());
        for (BulletSnapshot b : bullets) {
            b.write(out);
        }
    }

    public static WorldSnapshotMessage read(DataInputStream in) throws IOException {
        int tick = in.readInt();
        int pCount = in.readInt();
        List<PlayerSnapshot> players = new ArrayList<>(pCount);
        for (int i = 0; i < pCount; i++) {
            players.add(PlayerSnapshot.read(in));
        }
        // Пули:
        int bCount = in.readInt();
        List<BulletSnapshot> bullets = new ArrayList<>(bCount);
        for (int i = 0; i < bCount; i++) {
            bullets.add(BulletSnapshot.read(in));
        }
        return new WorldSnapshotMessage(tick, players, bullets);
    }

    public List<BulletSnapshot> bullets() { return bullets; }
    public int tick() { return tick; }
    public List<PlayerSnapshot> players() { return players; }
}
