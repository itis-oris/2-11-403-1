package aa.tulybaev.client;

import aa.tulybaev.client.model.World;
import aa.tulybaev.client.network.NetworkClient;
import aa.tulybaev.client.ui.GameFrame;
import aa.tulybaev.client.ui.GamePanel;

public class Main {

    public static void main(String[] args) throws Exception {

        World world = new World();
        NetworkClient network = new NetworkClient(world);

        GamePanel panel = new GamePanel(world);
        GameFrame frame = new GameFrame(panel);
        frame.setVisible(true);

        GameLoop loop = new GameLoop(world, panel, network);
        new Thread(loop, "GameLoop").start();
    }
}
