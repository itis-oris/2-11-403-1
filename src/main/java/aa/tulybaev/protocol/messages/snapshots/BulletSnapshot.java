package aa.tulybaev.protocol.messages.snapshots;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class BulletSnapshot {
    private final int id;
    private final float x, y;
    private final float vx;
    private final int ownerId;

    public BulletSnapshot(int id, float x, float y, float vx, int ownerId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.ownerId = ownerId;
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(vx);
        out.writeInt(ownerId);
    }

    public static BulletSnapshot read(DataInputStream in) throws IOException {
        return new BulletSnapshot(
                in.readInt(),
                in.readFloat(),
                in.readFloat(),
                in.readFloat(),
                in.readInt()
        );
    }

    // Геттеры
    public int id() { return id; }
    public float x() { return x; }
    public float y() { return y; }
    public float vx() { return vx; }
    public int ownerId() { return ownerId; }
}