package aa.tulybaev.protocol.core;

import java.io.*;

public final class BinaryProtocol {

    public static void send(DataOutputStream out, GameMessage msg) throws IOException {
        out.writeByte(msg.type().id());
        msg.write(out);
        out.flush();
    }

    public static GameMessage receive(DataInputStream in) throws IOException {
        return MessageReader.read(in);
    }
}
