package aa.tulybaev.protocol.messages;


import aa.tulybaev.protocol.core.GameMessage;
import aa.tulybaev.protocol.core.MessageType;

import java.io.DataOutputStream;
import java.io.IOException;

public final class GameOverMessage implements GameMessage {
    private final boolean isWinner;

    public GameOverMessage(boolean isWinner) {
        this.isWinner = isWinner;
    }

    @Override
    public MessageType type() {
        return MessageType.GAME_OVER;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeBoolean(isWinner);
    }

    public static GameOverMessage read(java.io.DataInputStream in) throws IOException {
        return new GameOverMessage(in.readBoolean());
    }

    public boolean isWinner() {
        return isWinner;
    }
}
