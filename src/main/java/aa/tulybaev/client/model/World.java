package aa.tulybaev.client.model;

import aa.tulybaev.client.model.entity.Bullet;
import aa.tulybaev.client.model.entity.Player;
import aa.tulybaev.client.model.input.InputHandler;

import java.util.ArrayList;
import java.util.List;

public class World {

    private final Player player;
    private final List<Bullet> bullets = new ArrayList<>();
    private final InputHandler input = new InputHandler();

    private static final int GROUND_Y = 380;

    public World() {
        player = new Player(100, GROUND_Y); // старт над землёй
    }

    public void update() {
        player.update(input, bullets, GROUND_Y);

        bullets.forEach(Bullet::update);
        bullets.removeIf(b -> !b.isAlive());
    }

    public Player getPlayer() {
        return player;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public InputHandler getInput() {
        return input;
    }

    public int getGroundY() {
        return GROUND_Y;
    }
}
