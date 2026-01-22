package core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Classe abstrata base para todas as telas do jogo
 */
public abstract class Screen extends JPanel {
    protected ScreenManager screenManager;
    
    public Screen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        setLayout(null);
        setBackground(new Color(20, 25, 35));
        setPreferredSize(new Dimension(Settings.GAME_WIDTH, Settings.GAME_HEIGHT));
    }
    
    /**
     * Inicializa os componentes da tela
     */
    public abstract void initialize();
    
    /**
     * Atualiza a lógica da tela
     */
    public abstract void update();
    
    /**
     * Renderiza a tela
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        render((Graphics2D) g);
    }
    
    /**
     * Método de renderização customizado
     */
    protected abstract void render(Graphics2D g);
    
    /**
     * Limpa recursos quando a tela é destruída
     */
    public abstract void dispose();
}
