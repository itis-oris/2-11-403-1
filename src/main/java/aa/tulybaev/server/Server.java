package aa.tulybaev.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static final int PORT = 50000;
    private static final int TICK_RATE = 30;

    private final DatagramSocket socket;
    private final WorldState world = new WorldState();
    private final AtomicInteger nextId = new AtomicInteger(1);

    // ================= CLIENT REGISTRY =================

    private static class ClientInfo {
        InetAddress address;
        int port;
    }

    private final Map<Integer, ClientInfo> clients = new ConcurrentHashMap<>();

    // ================= CONSTRUCTOR =================

    public Server() throws Exception {
        socket = new DatagramSocket(PORT);
        System.out.println("Server started on port " + PORT);

        startGameLoop();
        listen();
    }

    // ================= SERVER TICK =================

    private void startGameLoop() {
        Thread tick = new Thread(() -> {
            long nsPerTick = 1_000_000_000L / TICK_RATE;
            long last = System.nanoTime();

            while (true) {
                long now = System.nanoTime();
                if (now - last >= nsPerTick) {

                    world.update();      // bullets + cooldowns
                    broadcastBullets();
                    broadcastState();

                    last = now;
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {}
            }
        }, "ServerTick");

        tick.setDaemon(true);
        tick.start();
    }

    // ================= NETWORK LISTENER =================

    private void listen() throws Exception {
        byte[] buf = new byte[2048];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            String msg = new String(
                    packet.getData(),
                    0,
                    packet.getLength()
            );

            handle(msg, packet.getAddress(), packet.getPort());
        }
    }

    // ================= MESSAGE HANDLER =================

    private void handle(String msg, InetAddress addr, int port) {
        try {
            String[] parts = msg.split(" ");

            switch (parts[0]) {

                // ---------- JOIN ----------
                case "JOIN" -> {
                    int id = nextId.getAndIncrement();
                    world.createPlayer(id);

                    ClientInfo c = new ClientInfo();
                    c.address = addr;
                    c.port = port;
                    clients.put(id, c);

                    send("JOIN_OK " + id, addr, port);
                }

                // ---------- INPUT ----------
                case "INPUT" -> {
                    int id = Integer.parseInt(parts[1]);

                    ClientInfo c = clients.get(id);
                    if (c == null) return;
                    if (!c.address.equals(addr) || c.port != port) return;

                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    boolean facing = parts[4].equals("1");

                    var p = world.getPlayers().get(id);
                    if (p != null) {
                        p.x = x;
                        p.y = y;
                        p.facingRight = facing;
                    }
                }

                // ---------- SHOT ----------
                case "SHOT" -> {
                    int id = Integer.parseInt(parts[1]);

                    ClientInfo c = clients.get(id);
                    if (c == null) return;
                    if (!c.address.equals(addr) || c.port != port) return;

                    var p = world.getPlayers().get(id);
                    if (p != null && p.shootCooldown == 0) {
                        world.spawnBullet(p.x, p.y, p.facingRight, id);
                        p.shootCooldown = 10;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= BROADCAST =================

    private void broadcastState() {
        StringBuilder sb = new StringBuilder("STATE ");

        world.getPlayers().forEach((id, p) -> {
            sb.append(id).append(",")
                    .append(p.x).append(",")
                    .append(p.y).append(",")
                    .append(p.facingRight ? 1 : 0).append(",")
                    .append(p.hp).append(";");
        });

        sendAll(sb.toString());
    }

    private void broadcastBullets() {
        StringBuilder sb = new StringBuilder("BULLETS ");

        for (var b : world.getBullets()) {
            sb.append(b.x).append(",")
                    .append(b.y).append(";");
        }

        sendAll(sb.toString());
    }

    private void sendAll(String msg) {
        byte[] data = msg.getBytes();

        clients.values().forEach(c -> {
            try {
                socket.send(
                        new DatagramPacket(
                                data,
                                data.length,
                                c.address,
                                c.port
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ================= SEND =================

    private void send(String msg, InetAddress addr, int port) throws Exception {
        byte[] data = msg.getBytes();
        socket.send(
                new DatagramPacket(
                        data,
                        data.length,
                        addr,
                        port
                )
        );
    }

    // ================= ENTRY POINT =================

    public static void main(String[] args) throws Exception {
        new Server();
    }
}
