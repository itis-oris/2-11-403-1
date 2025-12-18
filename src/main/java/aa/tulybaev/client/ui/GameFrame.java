package aa.tulybaev.client.ui;


import javax.swing.*;

// Создание окна
public class GameFrame extends JFrame {

    public GameFrame() {
        setTitle("Dusty the Great");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        add(panel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        panel.start();
    }
}
