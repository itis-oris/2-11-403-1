package aa.tulybaev.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class PlayerSnapshot {

    private final int id;
    private final float x;
    private final float y;
    private final boolean facingRight;
    private final int hp;
    private final int ammo;
    private final boolean isMoving;
    private final boolean isOnGround;

    public PlayerSnapshot(int id, float x, float y, boolean facingRight, int hp, int ammo, boolean isMoving, boolean isOnGround) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.hp = hp;
        this.ammo = ammo;
        this.isMoving = isMoving;
        this.isOnGround = isOnGround;
    }


    public void write(DataOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeBoolean(facingRight);
        out.writeInt(hp);
        out.writeInt(ammo);
        out.writeBoolean(isMoving);
        out.writeBoolean(isOnGround);
    }


    public static PlayerSnapshot read(DataInputStream in) throws IOException {
        return new PlayerSnapshot(
                in.readInt(),
                in.readFloat(),
                in.readFloat(),
                in.readBoolean(),
                in.readInt(),
                in.readInt(),
                in.readBoolean(),
                in.readBoolean()
        );
    }

    public int ammo() { return ammo; }
    public int id() { return id; }
    public float x() { return x; }
    public float y() { return y; }
    public boolean facingRight() { return facingRight; }
    public int hp() { return hp; }

    public boolean isMoving() {
        return isMoving;
    }

    public boolean isOnGround() {
        return isOnGround;
    }
}
