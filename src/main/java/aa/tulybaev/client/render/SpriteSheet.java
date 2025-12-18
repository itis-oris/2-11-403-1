package aa.tulybaev.client.render;

import java.awt.image.BufferedImage;

public class SpriteSheet {

    private final BufferedImage sheet;

    public SpriteSheet(BufferedImage sheet) {
        this.sheet = sheet;
    }

    public BufferedImage[] getRow(int y, int frameCount, int w, int h) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * w, y * h, w, h);
        }
        return frames;
    }
}
