package core;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Gerencia a transição entre diferentes telas do jogo
 */
public class ScreenManager {
    private JFrame frame;
    private Map<String, Screen> screens;
    private Screen currentScreen;
    
    public ScreenManager(JFrame frame) {
        this.frame = frame;
        this.screens = new HashMap<>();
    }
    
    /**
     * Registra uma nova tela
     */
    public void registerScreen(String name, Screen screen) {
        screens.put(name, screen);
    }
    
    /**
     * Muda para uma tela específica
     */
    public void changeScreen(String name) {
        Screen newScreen = screens.get(name);
        if (newScreen == null) {
            System.err.println("Tela não encontrada: " + name);
            return;
        }
        
        if (currentScreen != null) {
            frame.remove(currentScreen);
        }
        
        currentScreen = newScreen;
        currentScreen.initialize();
        frame.add(currentScreen);
        frame.revalidate();
        frame.repaint();
    }
    
    /**
     * Retorna a tela atual
     */
    public Screen getCurrentScreen() {
        return currentScreen;
    }
    
    /**
     * Retorna o frame principal
     */
    public JFrame getFrame() {
        return frame;
    }
}
