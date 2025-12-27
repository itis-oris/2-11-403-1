package aa.tulybaev.server;

import aa.tulybaev.protocol.core.BinaryProtocol;
import aa.tulybaev.protocol.core.GameMessage;
import aa.tulybaev.protocol.messages.GameOverMessage;
import aa.tulybaev.protocol.messages.InputMessage;
import aa.tulybaev.protocol.messages.JoinAccept;
import aa.tulybaev.protocol.messages.snapshots.BulletSnapshot;
import aa.tulybaev.protocol.messages.snapshots.PlayerSnapshot;
import aa.tulybaev.protocol.messages.snapshots.WorldSnapshotMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static final int PORT = 50000;
    private static final int TICK_RATE = 30;

    private final ServerSocket serverSocket;
    private final WorldState world = new WorldState(this);
    private final AtomicInteger nextId = new AtomicInteger(1);
    private int serverTick = 0;
    private static final String SAVE_FILE = "save.json";
    private ServerStats stats = new ServerStats();

    private final Set<ClientHandler> allClients = ConcurrentHashMap.newKeySet();

    public Server() throws Exception {
        loadStats();

        this.serverSocket = new ServerSocket(PORT);
        startGameLoop();
        acceptClients();

        // Регистрируем shutdown hook для сохранения при завершении
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveStats));
    }

    private void loadStats() {
        Path path = Paths.get(SAVE_FILE);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ServerStats loaded = new Gson().fromJson(reader, ServerStats.class);
                if (loaded != null) {
                    this.stats = loaded;
                    System.out.println("Статистика загружена из " + SAVE_FILE);
                }
            } catch (Exception e) {
                System.err.println("Ошибка загрузки статистики: " + e.getMessage());
                this.stats = new ServerStats(); // сброс при ошибке
            }
        } else {
            System.out.println("Файл сохранения не найден. Создаём новый.");
        }
    }

    private void saveStats() {
        try (Writer writer = Files.newBufferedWriter(Paths.get(SAVE_FILE))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(stats, writer);
            System.out.println("Статистика сохранена в " + SAVE_FILE);
        } catch (Exception e) {
            System.err.println("Ошибка сохранения статистики: " + e.getMessage());
        }
    }

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

    private void startGameLoop() {
        Thread ticker = new Thread(() -> {
            long nsPerTick = 1_000_000_000L / TICK_RATE;
            long last = System.nanoTime();

            while (!serverSocket.isClosed()) {
                long now = System.nanoTime();
                if (now - last >= nsPerTick) {
                    world.update();
                    serverTick++;
                    broadcastSnapshot(serverTick);
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

    // Рассылаем снапшоты пользователям

    private void broadcastSnapshot(int tick) {
        Collection<PlayerState> players = world.getPlayers();

        List<PlayerSnapshot> playerSnapshots = players.stream()
                .map(p -> new PlayerSnapshot(
                        p.id,
                        (float) p.x,
                        (float) p.y,
                        p.facingRight,
                        p.hp,
                        p.ammo,
                        Math.abs(p.vx) > 0.1f,
                        p.onGround
                ))
                .toList();

        List<BulletSnapshot> bulletSnapshots = world.getBullets().stream()
                .map(b -> new BulletSnapshot(
                        b.id,
                        (float) b.x,
                        (float) b.y,
                        (float) b.vx,
                        b.ownerId
                ))
                .toList();

        WorldSnapshotMessage msg = new WorldSnapshotMessage(tick, playerSnapshots, bulletSnapshots);
        for (ClientHandler client : allClients) {
            client.send(msg);
        }
    }

    public void onPlayerKilled(int killerId, int victimId) {
        stats.recordWin(killerId);
        stats.recordDeath(victimId);

        for (ClientHandler client : allClients) {
            if (client.playerId == killerId) {
                client.sendGameOver(true);
            } else if (client.playerId == victimId) {
                client.sendGameOver(false);
            } else {
                client.sendGameOver(true);
            }
        }
    }

    // Обработка клиента
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
            Server.this.allClients.add(this);
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

                    send(new JoinAccept(id));
                }

                case INPUT -> {
                    InputMessage input = (InputMessage) msg;
                    if (playerId != input.playerId()) {
                        throw new IOException("Player ID mismatch");
                    }
                    world.applyInput(playerId, input.dx(), input.jump(), input.shoot());
                }

                case DISCONNECT -> {
                    running = false;
                }

                default -> {
                    // Игнорируем
                }
            }
        }

        private void sendGameOver(boolean isWinner) {
            send(new GameOverMessage(isWinner));
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
                world.removePlayer(playerId);
            }
            Server.this.allClients.remove(this); // ← УДАЛЯЕМ
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    public void removePlayer(int id) {
        world.removePlayer(id);
    }


    public static void main(String[] args) throws Exception {
        Server server = new Server();

        System.out.println("Текущая статистика:");
        server.stats.players.forEach((id, s) ->
                System.out.println("  Игрок " + id + ": " + s.wins + " побед, " + s.deaths + " смертей")
        );

        System.in.read();

        server.serverSocket.close();
    }
}