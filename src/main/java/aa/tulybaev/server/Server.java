package aa.tulybaev.server;

import aa.tulybaev.protocol.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static final int PORT = 50000;
    private static final int TICK_RATE = 30;

    private final ServerSocket serverSocket;
    private final WorldState world = new WorldState();
    private final AtomicInteger nextId = new AtomicInteger(1);

    // Регистр клиентов: playerId -> ClientHandler
    private final Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();

    public Server() throws Exception {
        this.serverSocket = new ServerSocket(PORT);
        System.out.println("TCP Server started on port " + PORT);

        // Запуск игрового цикла
        startGameLoop();

        // Ожидание подключений
        acceptClients();
    }

    // ================= ПРИНЯТИЕ ПОДКЛЮЧЕНИЙ =================

    private void acceptClients() {
        Thread acceptor = new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    new ClientHandler(socket).start();
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    e.printStackTrace();
                }
            }
        }, "ClientAcceptor");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    // ================= ИГРОВОЙ ЦИКЛ (TICKER) =================

    private void startGameLoop() {
        Thread ticker = new Thread(() -> {
            long nsPerTick = 1_000_000_000L / TICK_RATE;
            long last = System.nanoTime();

            while (!serverSocket.isClosed()) {
                long now = System.nanoTime();
                if (now - last >= nsPerTick) {
                    world.update();

                    // Рассылаем снапшот всем клиентам
                    broadcastSnapshot();

                    last = now;
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {}
            }
        }, "ServerTicker");
        ticker.setDaemon(true);
        ticker.start();
    }

    // ================= РАССЫЛКА СНАПШОТОВ =================

    private void broadcastSnapshot() {
        // Собираем снапшот
        var players = world.getPlayers();
        var snapshots = players.stream()
                .map(p -> new PlayerSnapshot(
                        p.id,
                        (float) p.x,
                        (float) p.y,
                        p.facingRight,
                        p.hp
                ))
                .toList();

        // Генерируем уникальный тик (можно использовать системное время или счётчик)
        int tick = (int) (System.currentTimeMillis() / (1000 / TICK_RATE));

        WorldSnapshotMessage msg = new WorldSnapshotMessage(tick, snapshots);

        // Рассылаем
        for (ClientHandler client : clients.values()) {
            client.send(msg);
        }
    }

    // ================= ОБРАБОТЧИК КЛИЕНТА =================

    private class ClientHandler extends Thread {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;
        private volatile int playerId = -1;
        private volatile boolean running = true;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                while (running && !socket.isClosed()) {
                    GameMessage msg = BinaryProtocol.receive(in);
                    handle(msg);
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Client disconnected: " + playerId);
                }
            } finally {
                disconnect();
            }
        }

        private void handle(GameMessage msg) throws IOException {
            switch (msg.type()) {
                case JOIN_REQUEST -> {
                    int id = nextId.getAndIncrement();
                    world.createPlayer(id);
                    this.playerId = id;
                    clients.put(id, this);

                    send(new JoinAccept(id));
                }

                case INPUT -> {
                    InputMessage input = (InputMessage) msg;
                    if (playerId != input.playerId()) {
                        throw new IOException("Player ID mismatch");
                    }
                    world.applyInput(playerId, input.dx(), input.dy(), input.shoot());
                }

                case DISCONNECT -> {
                    running = false;
                }

                default -> {
                    // Игнорируем
                }
            }
        }

        public void send(GameMessage msg) {
            try {
                BinaryProtocol.send(out, msg);
            } catch (IOException e) {
                if (running) {
                    running = false;
                    disconnect();
                }
            }
        }

        private void disconnect() {
            if (playerId != -1) {
                clients.remove(playerId);
                world.removePlayer(playerId); // ← нужно добавить в WorldState!
            }
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    // ================= MAIN =================

    public static void main(String[] args) throws Exception {
        new Server();
    }
}