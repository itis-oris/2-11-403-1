package aa.tulybaev.protocol.core;


import java.io.DataOutputStream;
import java.io.IOException;

public interface GameMessage {
    MessageType type();
    void write(DataOutputStream out) throws IOException;
}

