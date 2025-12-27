package aa.tulybaev.protocol.messages;


import aa.tulybaev.protocol.core.GameMessage;
import aa.tulybaev.protocol.core.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class DisconnectMessage implements GameMessage {

    public static final DisconnectMessage INSTANCE = new DisconnectMessage();

    private DisconnectMessage() {}

    @Override
    public MessageType type() {
        return MessageType.DISCONNECT;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
    }

    public static DisconnectMessage read(DataInputStream in) throws IOException {
        return INSTANCE;
    }
}