package aa.tulybaev.protocol.messages;


import aa.tulybaev.protocol.core.GameMessage;
import aa.tulybaev.protocol.core.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class InputMessage implements GameMessage {

    private final int playerId;
    private final float dx;
    private final boolean jump;
    private final boolean shoot;

    public InputMessage(int playerId, float dx, boolean jump, boolean shoot) {
        this.playerId = playerId;
        this.dx = dx;
        this.jump = jump;
        this.shoot = shoot;
    }

    @Override
    public MessageType type() {
        return MessageType.INPUT;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(playerId);
        out.writeFloat(dx);
        out.writeBoolean(jump);
        out.writeBoolean(shoot);
    }

    public static InputMessage read(DataInputStream in) throws IOException {
        return new InputMessage(
                in.readInt(),
                in.readFloat(),
                in.readBoolean(),
                in.readBoolean()
        );
    }

    public int playerId() { return playerId; }
    public float dx() { return dx; }
    public boolean jump() { return jump; }
    public boolean shoot() { return shoot; }
}