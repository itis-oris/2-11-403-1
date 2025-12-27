package aa.tulybaev.protocol.core;


import aa.tulybaev.protocol.messages.*;
import aa.tulybaev.protocol.messages.snapshots.WorldSnapshotMessage;

import java.io.*;

public final class MessageReader {

    private MessageReader() {}

    public static GameMessage read(DataInputStream in) throws IOException {
        MessageType type = MessageType.from(in.readByte());

        return switch (type) {
            case JOIN_REQUEST -> JoinRequest.read(in);
            case JOIN_ACCEPT -> JoinAccept.read(in);
            case INPUT -> InputMessage.read(in);
            case SNAPSHOT -> WorldSnapshotMessage.read(in);
            case GAME_OVER -> GameOverMessage.read(in);
            case DISCONNECT -> DisconnectMessage.read(in);
        };
    }
}