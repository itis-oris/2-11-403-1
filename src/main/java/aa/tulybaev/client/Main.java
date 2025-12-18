package aa.tulybaev.client;

import aa.tulybaev.client.ui.GameFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}

