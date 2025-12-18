package aa.tulybaev.client.render;

import aa.tulybaev.client.model.entity.Player;

public class Camera {

    private int x, y;

    public void update(Player player, int screenW, int screenH) {
        x = (int) (player.getDrawX() - screenW / 2);
        y = (int) (player.getDrawY() - screenH / 2);

        if (x < 0) x = 0;
        if (y < 0) y = 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
