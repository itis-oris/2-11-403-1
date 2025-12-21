package aa.tulybaev.client.network;

import aa.tulybaev.client.core.SnapshotBuffer;
import aa.tulybaev.protocol.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Клиентский сетевой слой на TCP.
 * Отвечает ТОЛЬКО за отправку/приём сообщений.
 */
public final class NetworkClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 50000;

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final SnapshotBuffer snapshotBuffer;
    private ConnectionCallback onConnected;

    private volatile int playerId = -1;
    private volatile boolean running = true;

    public NetworkClient(SnapshotBuffer snapshotBuffer) throws Exception {
        this.snapshotBuffer = snapshotBuffer;
        this.socket = new Socket(SERVER_HOST, SERVER_PORT);
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());

        send(new JoinRequest("player"));
        startListener();
    }


    // ================= ВНЕШНИЙ ИНТЕРФЕЙС =================

    public void sendInput(float dx, boolean jump, boolean shoot) {
        if (playerId < 0) return;
        send(new InputMessage(playerId, dx, jump, shoot)); // ← 4 аргумента
    }

    public int getPlayerId() {
        return playerId;
    }

    public void shutdown() {
        running = false;
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    // ================= ОТПРАВКА =================

    private void send(GameMessage msg) {
        try {
            BinaryProtocol.send(out, msg);
        } catch (Exception e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    // ================= ПРИЁМ =================

    private void startListener() {
        Thread listener = new Thread(() -> {
            try {
                while (running && !socket.isClosed()) {
                    GameMessage msg = BinaryProtocol.receive(in);
                    handle(msg);
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Connection lost");
                }
            } catch (Exception e) {
                System.err.println("Failed to read message:");
                e.printStackTrace(); // ← ВАЖНО!
            }
        }, "NetworkListener");
        listener.setDaemon(true);
        listener.start();
    }

    // ================= ОБРАБОТКА =================

    private void handle(GameMessage msg) {
        System.out.println("Received message: " + msg.type()); // ← ДОБАВЬ ЭТО

        switch (msg.type()) {
            case JOIN_ACCEPT -> {
                JoinAccept join = (JoinAccept) msg;
                this.playerId = join.playerId();
                System.out.println("Connected as player " + playerId);
                if (onConnected != null) {
                    onConnected.onConnected(playerId);
                }
            }
            case SNAPSHOT -> {
                WorldSnapshotMessage snap = (WorldSnapshotMessage) msg;
                System.out.println("CLIENT: Received snapshot with " + snap.players().size() + " players");
                for (PlayerSnapshot p : snap.players()) {
                    System.out.println("  Player " + p.id() + " at (" + p.x() + ", " + p.y() +
                            ") facing=" + p.facingRight() + " hp=" + p.hp());
                }
                snapshotBuffer.push(snap);
            }
            case DISCONNECT -> {
                System.out.println("Server disconnected");
                running = false;
            }
            default -> {
                System.out.println("Unhandled message type: " + msg.type());
            }
        }
    }

    public void setConnectionCallback(ConnectionCallback callback) {
        this.onConnected = callback;
    }

}