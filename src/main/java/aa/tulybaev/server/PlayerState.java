package aa.tulybaev.server;

public final class PlayerState {
    public int id;
    public double x, y;
    public double vx, vy;
    public boolean facingRight = true;
    public int hp = 100;
    public int shootCooldown = 0;
    public boolean onGround = false;
    public int ammo = 25;
    public static final int MAX_AMMO = 100;

    public void refillAmmo() {
        this.ammo = MAX_AMMO;
    }
}
