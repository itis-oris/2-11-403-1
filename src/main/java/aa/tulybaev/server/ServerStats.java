package aa.tulybaev.server;

import java.util.HashMap;
import java.util.Map;

public class ServerStats {
    public Map<Integer, PlayerStats> players = new HashMap<>();

    public static class PlayerStats {
        public int wins = 0;
        public int deaths = 0;
    }

    public void recordWin(int playerId) {
        PlayerStats stats = players.get(playerId);
        if (stats == null) {
            stats = new PlayerStats();
            players.put(playerId, stats);
        }
        stats.wins++;
    }

    public void recordDeath(int playerId) {
        PlayerStats stats = players.get(playerId);
        if (stats == null) {
            stats = new PlayerStats();
            players.put(playerId, stats);
        }
        stats.deaths++;
    }
}