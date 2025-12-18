package aa.tulybaev.client.model.entity;

public class Bullet {

    private double x, y;
    private final double vx;
    private int life = 120;

    public Bullet(double x, double y, double vx) {
        this.x = x;
        this.y = y;
        this.vx = vx;
    }

    public void update() {
        x += vx;
        life--;
    }

    public boolean isAlive() {
        return life > 0;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
