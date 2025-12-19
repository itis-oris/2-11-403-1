package aa.tulybaev.client.ui;

import javax.swing.*;

public class GameFrame extends JFrame {

    public GameFrame(GamePanel panel) {
        setTitle("Dusty the Great");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        add(panel);

        pack();
        setLocationRelativeTo(null);
    }
}
