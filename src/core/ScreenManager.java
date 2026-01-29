package core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 * Gerencia a transição entre diferentes telas do jogo
 */
public class ScreenManager {
    private JFrame frame;
    private Map<String, Screen> screens;
    private Screen currentScreen;
    
    // Variáveis de transição
    private JPanel overlayPanel;
    private Timer transitionTimer;
    private float alpha = 0.0f;
    private String pendingScreenName;
    private boolean isFadingOut = false;
    private final float FADE_SPEED = 0.05f;
    
    public ScreenManager(JFrame frame) {
        this.frame = frame;
        this.screens = new HashMap<>();
        setupOverlay();
    }
    
    private void setupOverlay() {
        overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (alpha > 0) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g2.setColor(Color.BLACK);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        overlayPanel.setOpaque(false);
        overlayPanel.setVisible(false);
        
        // Bloqueia interações durante a transição
        MouseAdapter blocker = new MouseAdapter() {};
        overlayPanel.addMouseListener(blocker);
        overlayPanel.addMouseMotionListener(blocker);
        
        frame.setGlassPane(overlayPanel);
        
        transitionTimer = new Timer(16, e -> updateTransition());
    }
    
    private void updateTransition() {
        if (isFadingOut) {
            alpha += FADE_SPEED;
            SoundManager.getInstance().setFadeFactor(Math.max(0.0f, 1.0f - alpha));
            
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                SoundManager.getInstance().setFadeFactor(0.0f);
                isFadingOut = false;
                performScreenChange(pendingScreenName);
            }
        } else {
            alpha -= FADE_SPEED;
            SoundManager.getInstance().setFadeFactor(Math.max(0.0f, 1.0f - alpha));
            
            if (alpha <= 0.0f) {
                alpha = 0.0f;
                SoundManager.getInstance().setFadeFactor(1.0f);
                transitionTimer.stop();
                overlayPanel.setVisible(false);
            }
        }
        overlayPanel.repaint();
    }
    
    /**
     * Registra uma nova tela
     */
    public void registerScreen(String name, Screen screen) {
        screens.put(name, screen);
    }
    
    /**
     * Muda para uma tela específica com transição
     */
    public void changeScreen(String name) {
        if (!screens.containsKey(name)) {
            System.err.println("Tela não encontrada: " + name);
            return;
        }
        
        if (currentScreen == null) {
            performScreenChange(name);
        } else {
            pendingScreenName = name;
            isFadingOut = true;
            alpha = 0.0f;
            overlayPanel.setVisible(true);
            transitionTimer.start();
        }
    }
    
    private void performScreenChange(String name) {
        Screen newScreen = screens.get(name);
        
        // Lógica de Música
        if (name.equals("game")) {
            SoundManager.getInstance().startRadio();
        } else {
            SoundManager.getInstance().playMenuMusic();
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
