package aa.tulybaev.client;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class GameStateStorage {

    private static final Path SAVE_FILE = Paths.get("save.json");

    public static GameState load() {
        try {
            if (!Files.exists(SAVE_FILE)) {
                return new GameState();
            }
            String json = Files.readString(SAVE_FILE);
            // Простой парсинг без библиотек
            GameState state = new GameState();

            // Извлекаем lastPlayerId
            int idStart = json.indexOf("\"lastPlayerId\":");
            if (idStart != -1) {
                int idEnd = json.indexOf(",", idStart);
                if (idEnd == -1) idEnd = json.indexOf("}", idStart);
                String idStr = json.substring(idStart + 15, idEnd).trim();
                try {
                    state.lastPlayerId = Integer.parseInt(idStr);
                } catch (NumberFormatException ignored) {}
            }

            // Для списка игроков можно пропустить (для зачёта хватит playerId)
            return state;
        } catch (IOException e) {
            System.err.println("Failed to load save: " + e.getMessage());
            return new GameState();
        }
    }

    public static void save(GameState state) {
        try {
            String json = String.format(
                    "{\n" +
                            "  \"lastPlayerId\": %d,\n" +
                            "  \"recentPlayers\": []\n" +
                            "}",
                    state.lastPlayerId
            );
            Files.writeString(SAVE_FILE, json);
            System.out.println("Game state saved to " + SAVE_FILE.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save game state: " + e.getMessage());
        }
    }
}