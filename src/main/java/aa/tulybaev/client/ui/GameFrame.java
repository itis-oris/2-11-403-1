package aa.tulybaev.client.ui;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

    private final JLabel hpLabel;
    private final JLabel ammoLabel;
    private final JLabel playersLabel;

    public GameFrame(GamePanel panel) {
        setTitle("Dusty the Great");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Создаём метки
        this.hpLabel = new JLabel("HP: --");
        this.ammoLabel = new JLabel("Ammo: --");
        this.playersLabel = new JLabel("| Players: --");

        // Верхняя панель
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setOpaque(false);
        topPanel.add(hpLabel);
        topPanel.add(ammoLabel);
        topPanel.add(playersLabel);

        // Кнопка выхода
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        exitButton.setFocusable(false);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(exitButton);

        // Собираем всё вместе
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Обновляет HUD-метки.
     * Должен вызываться из EDT (через SwingUtilities.invokeLater).
     */
    public void updateHud(int hp, int ammo, int playerCount) {
        hpLabel.setText("HP: " + hp);
        ammoLabel.setText("Ammo: " + ammo);
        playersLabel.setText("| Players: " + playerCount);
    }
}