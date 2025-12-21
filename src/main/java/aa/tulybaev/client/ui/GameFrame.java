package aa.tulybaev.client.ui;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    private final JPanel menuPanel;
    private final JPanel gameOverPanel;
    private GamePanel currentGamePanel = null;

    public Runnable startGame;
    public Runnable restartGame;


    public GameFrame() {
        setTitle("Dusty the Great");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        menuPanel = createMenuPanel();
        gameOverPanel = createGameOverPanel();

        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(gameOverPanel, "GAME_OVER");

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel title = new JLabel("DUSTY THE GREAT");
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.PLAIN, 24));
        startButton.setAlignmentX(CENTER_ALIGNMENT);
        startButton.addActionListener(e -> {
            if (startGame != null) {
                startGame.run(); // ← ПРАВИЛЬНЫЙ ВЫЗОВ
            }
        });

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(startButton);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createGameOverPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel title = new JLabel("YOU DIED!");
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(Color.RED);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JButton restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.PLAIN, 24));
        restartButton.setAlignmentX(CENTER_ALIGNMENT);
        restartButton.addActionListener(e -> {
            // Перезапуск игры
            restartGame();
        });

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(restartButton);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    public void setGamePanel(GamePanel panel) {
        if (currentGamePanel != null) {
            mainPanel.remove(currentGamePanel);
        }
        currentGamePanel = panel;
        mainPanel.add(currentGamePanel, "PLAYING");

        // Фиксируем размер окна
        pack();
        setLocationRelativeTo(null);
    }

    public void startGame() {
        if (startGame != null) startGame.run();
    }

    public void restartGame() {
        if (restartGame != null) restartGame.run();
    }

    public void showMenu() {
        cardLayout.show(mainPanel, "MENU");
    }

    public void showGame() {
        cardLayout.show(mainPanel, "PLAYING");
    }

    public void showGameOver() {
        cardLayout.show(mainPanel, "GAME_OVER");
    }

}