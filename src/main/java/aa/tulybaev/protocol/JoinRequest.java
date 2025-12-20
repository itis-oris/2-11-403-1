package aa.tulybaev.protocol;

import java.io.*;

public final class JoinRequest implements GameMessage {

    private final String name;

    public JoinRequest(String name) {
        this.name = name;
    }

    @Override
    public MessageType type() {
        return MessageType.JOIN_REQUEST;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(name);
    }

    public static JoinRequest read(DataInputStream in) throws IOException {
        return new JoinRequest(in.readUTF());
    }

    public String name() {
        return name;
    }
}
