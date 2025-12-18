package aa.tulybaev.client.render;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SpriteLoader {

    public static BufferedImage load(String path) {
        try {
            System.out.println("Loaded sprite: " + path);
            return ImageIO.read(
                    SpriteLoader.class.getResourceAsStream(path)
            );
        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Cannot load sprite: " + path, e);
        }
    }
}
