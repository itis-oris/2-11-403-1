package aa.tulybaev.server;

public class ServerBullet {
    public static int nextId = 1;

    public final int id;
    public double x, y, vx;
    public int ownerId;

    public ServerBullet(double x, double y, double vx, int ownerId) {
        this.id = nextId++;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.ownerId = ownerId;
    }
}
