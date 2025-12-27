package aa.tulybaev.client.network;

import aa.tulybaev.client.Main;
import aa.tulybaev.client.core.SnapshotBuffer;
import aa.tulybaev.protocol.core.BinaryProtocol;
import aa.tulybaev.protocol.core.GameMessage;
import aa.tulybaev.protocol.messages.GameOverMessage;
import aa.tulybaev.protocol.messages.InputMessage;
import aa.tulybaev.protocol.messages.JoinAccept;
import aa.tulybaev.protocol.messages.JoinRequest;
import aa.tulybaev.protocol.messages.snapshots.WorldSnapshotMessage;

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



    public void sendInput(float dx, boolean jump, boolean shoot) {
        if (playerId < 0) return;
        send(new InputMessage(playerId, dx, jump, shoot));
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


    private void send(GameMessage msg) {
        try {
            BinaryProtocol.send(out, msg);
        } catch (Exception e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }


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
                e.printStackTrace();
            }
        }, "NetworkListener");
        listener.setDaemon(true);
        listener.start();
    }


    private void handle(GameMessage msg) {

        switch (msg.type()) {
            case JOIN_ACCEPT -> {
                JoinAccept join = (JoinAccept) msg;
                this.playerId = join.playerId();
                if (onConnected != null) {
                    onConnected.onConnected(playerId);
                }
            }
            case SNAPSHOT -> {
                WorldSnapshotMessage snap = (WorldSnapshotMessage) msg;
                snapshotBuffer.push(snap);
            }
            case GAME_OVER -> {
                GameOverMessage gameOverMsg = (GameOverMessage) msg;
                if (gameOverMsg.isWinner()) {
                    Main.triggerVictory();
                } else {
                    Main.triggerGameOver();
                }
            }
            case DISCONNECT -> {
                running = false;
            }
            default -> {
            }
        }
    }

    public void setConnectionCallback(ConnectionCallback callback) {
        this.onConnected = callback;
    }

}