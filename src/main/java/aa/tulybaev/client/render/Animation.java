package aa.tulybaev.client.render;

import java.awt.image.BufferedImage;

public class Animation {

    private final BufferedImage[] frames;
    private int index = 0;
    private int timer = 0;
    private final int speed;

    public Animation(BufferedImage[] frames, int speed) {
        this.frames = frames;
        this.speed = speed;
    }

    public void update() {
        timer++;
        if (timer >= speed) {
            timer = 0;
            index = (index + 1) % frames.length;
        }
    }

    public BufferedImage getFrame() {
        return frames[index];
    }
}
