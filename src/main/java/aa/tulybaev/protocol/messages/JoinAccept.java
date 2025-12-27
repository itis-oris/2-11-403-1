package aa.tulybaev.protocol.messages;


import aa.tulybaev.protocol.core.GameMessage;
import aa.tulybaev.protocol.core.MessageType;

import java.io.*;

public final class JoinAccept implements GameMessage {

    private final int playerId;

    public JoinAccept(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public MessageType type() {
        return MessageType.JOIN_ACCEPT;
    }


    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(playerId);
    }

    public static JoinAccept read(DataInputStream in) throws IOException {
        return new JoinAccept(in.readInt());
    }

    public int playerId() {
        return playerId;
    }
}
