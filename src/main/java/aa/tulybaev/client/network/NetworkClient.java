package aa.tulybaev.client.network;

import aa.tulybaev.client.model.World;
import aa.tulybaev.client.model.entity.Player;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NetworkClient {

    private static final int SERVER_PORT = 50000;

    private final DatagramSocket socket;
    private final InetAddress serverAddr;
    private final World world;

    private int playerId = -1;

    public NetworkClient(World world) throws Exception {
        this.world = world;
        this.socket = new DatagramSocket();
        this.serverAddr = InetAddress.getByName("localhost");

        sendJoin();
        startListener();
    }

    // ================= JOIN =================

    private void sendJoin() {
        send("JOIN");
    }

    // ================= INPUT =================

    public void sendInput() {
        if (playerId < 0) return;

        Player p = world.getPlayer();

        send(
                "INPUT " +
                        playerId + " " +
                        p.getDrawX() + " " +
                        p.getDrawY() + " " +
                        (p.isFacingRight() ? 1 : 0)
        );
    }

    // ================= SHOT =================

    public void sendShot() {
        if (playerId < 0) return;
        send("SHOT " + playerId);
    }

    // ================= LISTENER =================

    private void startListener() {
        Thread t = new Thread(() -> {
            byte[] buf = new byte[4096];

            while (true) {
                try {
                    DatagramPacket packet =
                            new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    String msg = new String(
                            packet.getData(),
                            0,
                            packet.getLength()
                    );

                    handle(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "NetworkListener");

        t.setDaemon(true);
        t.start();
    }

    // ================= MESSAGE HANDLER =================

    private void handle(String msg) {
        String[] parts = msg.split(" ");

        switch (parts[0]) {

            // ---------- JOIN OK ----------
            case "JOIN_OK" -> {
                playerId = Integer.parseInt(parts[1]);
                world.setLocalPlayerId(playerId);
                System.out.println("Connected as player " + playerId);
            }

            // ---------- PLAYERS STATE ----------
            case "STATE" -> {
                if (parts.length < 2) return;

                String[] players = parts[1].split(";");

                for (String s : players) {
                    if (s.isEmpty()) continue;

                    String[] d = s.split(",");

                    int id = Integer.parseInt(d[0]);
                    if (id == playerId) continue;

                    double x = Double.parseDouble(d[1]);
                    double y = Double.parseDouble(d[2]);
                    boolean facing = d[3].equals("1");
                    int hp = Integer.parseInt(d[4]);

                    world.updateRemotePlayer(id, x, y, facing, hp);
                }
            }

            // ---------- BULLETS ----------
            case "BULLETS" -> {
                if (parts.length < 2) return;
                world.syncBulletsFromServer(parts[1]);
            }
        }
    }

    // ================= SEND =================

    private void send(String msg) {
        try {
            byte[] data = msg.getBytes();
            socket.send(
                    new DatagramPacket(
                            data,
                            data.length,
                            serverAddr,
                            SERVER_PORT
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
