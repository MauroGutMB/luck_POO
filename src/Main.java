import core.*;
import game.*;
import javax.swing.*;
import java.awt.*;

/**
 * Classe principal do jogo
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Cria a janela principal
            JFrame frame = new JFrame("LUCK");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(Settings.GAME_WIDTH, Settings.GAME_HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            
            // Cria o gerenciador de telas
            ScreenManager screenManager = new ScreenManager(frame);
            
            // Registra as telas
            screenManager.registerScreen("menu", new MenuScreen(screenManager));
            screenManager.registerScreen("options", new OptionsScreen(screenManager));
            screenManager.registerScreen("game", new GameScreen(screenManager));
            
            // Inicia no menu principal
            screenManager.changeScreen("menu");
            
            // Exibe a janela
            frame.setVisible(true);
        });
    }
}
