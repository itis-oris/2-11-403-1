package aa.tulybaev.protocol;

public enum MessageType {
    JOIN_REQUEST(1),
    JOIN_ACCEPT(2),
    INPUT(3),
    SNAPSHOT(4),
    DISCONNECT(5);

    private final byte id;

    MessageType(int id) {
        this.id = (byte) id;
    }

    public byte id() {
        return id;
    }

    public static MessageType from(byte id) {
        for (MessageType t : values()) {
            if (t.id == id) return t;
        }
        throw new IllegalArgumentException("Unknown type: " + id);
    }
}