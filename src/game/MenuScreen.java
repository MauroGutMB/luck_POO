package game;

import core.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Tela do menu principal
 */
public class MenuScreen extends Screen {
    private JButton playButton;
    private JButton optionsButton;
    private JButton exitButton;
    private Image backgroundImage;
    
    public MenuScreen(ScreenManager screenManager) {
        super(screenManager);
        loadBackground();
    }
    
    /**
     * Carrega a imagem de fundo
     */
    private void loadBackground() {
        try {
            backgroundImage = ImageIO.read(new File("assets/bg-menu.png"));
        } catch (IOException e) {
            System.err.println("Erro ao carregar imagem de fundo: " + e.getMessage());
            backgroundImage = null;
        }
    }
    
    @Override
    public void initialize() {
        removeAll();
        setOpaque(false);
        
        // Título do jogo
        JLabel titleLabel = new JLabel("LUCK", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
        titleLabel.setForeground(new Color(255, 223, 0));
        titleLabel.setBounds(200, 120, 600, 80);
        titleLabel.setOpaque(false);
        add(titleLabel);
        
        // Botão Jogar
        playButton = createMenuButton("JOGAR", 350, 280);
        playButton.addActionListener(e -> {
            GameManager.getInstance().startNewGame();
            screenManager.changeScreen("game");
        });
        add(playButton);
        
        // Botão Opções
        optionsButton = createMenuButton("OPÇÕES", 350, 360);
        optionsButton.addActionListener(e -> {
            screenManager.changeScreen("options");
        });
        add(optionsButton);
        
        // Botão Sair
        exitButton = createMenuButton("SAIR", 350, 440);
        exitButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente sair do jogo?",
                "Confirmar Saída",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        add(exitButton);
        
        revalidate();
        repaint();
    }
    
    /**
     * Cria um botão estilizado para o menu
     */
    private JButton createMenuButton(String text, int x, int y) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Desenha o fundo com gradiente
                if (getModel().isRollover()) {
                    g2.setColor(new Color(90, 140, 190, 230));
                } else {
                    g2.setColor(new Color(50, 90, 140, 210));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Desenha a borda
                g2.setStroke(new BasicStroke(getModel().isRollover() ? 3 : 2));
                g2.setColor(new Color(255, 223, 0));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setBounds(x, y, 300, 65);
        button.setFont(new Font("Arial", Font.BOLD, 28));
        button.setForeground(new Color(255, 255, 255));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        
        // Efeito hover com mudança de cor do texto
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(new Color(255, 255, 100));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });
        
        return button;
    }
    
    @Override
    public void update() {
        // Menu não precisa de atualização constante
    }
    
    @Override
    protected void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Desenha a imagem de fundo
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
    
    @Override
    public void dispose() {
        // Limpa recursos se necessário
    }
}
