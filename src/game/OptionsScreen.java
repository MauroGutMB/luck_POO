package game;

import core.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Tela de opções do jogo
 */
public class OptionsScreen extends Screen {
    private JButton backButton;
    private JSlider volumeSlider;
    private JLabel volumeValueLabel;
    private Settings settings;
    
    public OptionsScreen(ScreenManager screenManager) {
        super(screenManager);
        this.settings = Settings.getInstance();
    }
    
    @Override
    public void initialize() {
        removeAll();
        
        // Título
        JLabel titleLabel = new JLabel("OPÇÕES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 44));
        titleLabel.setForeground(new Color(255, 223, 0));
        titleLabel.setBounds(200, 80, 600, 60);
        add(titleLabel);
        
        // Label Volume
        JLabel volumeLabel = new JLabel("Volume:");
        volumeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setBounds(300, 180, 150, 30);
        add(volumeLabel);
        
        // Label com valor do volume
        volumeValueLabel = new JLabel(settings.getVolume() + "%");
        volumeValueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        volumeValueLabel.setForeground(new Color(255, 223, 0));
        volumeValueLabel.setBounds(720, 180, 80, 30);
        add(volumeValueLabel);
        
        // Slider de volume
        volumeSlider = new JSlider(0, 100, settings.getVolume());
        volumeSlider.setBounds(300, 220, 420, 60);
        volumeSlider.setBackground(new Color(20, 25, 35));
        volumeSlider.setForeground(new Color(255, 223, 0));
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = volumeSlider.getValue();
                settings.setVolume(value);
                volumeValueLabel.setText(value + "%");
            }
        });
        add(volumeSlider);
        
        // Botão Voltar
        backButton = createButton("VOLTAR", 400, 380);
        backButton.addActionListener(e -> {
            screenManager.changeScreen("menu");
        });
        add(backButton);
        
        revalidate();
        repaint();
    }
    
    private JButton createButton(String text, int x, int y) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isRollover()) {
                    g2.setColor(new Color(90, 140, 190, 230));
                } else {
                    g2.setColor(new Color(50, 90, 140, 210));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                g2.setStroke(new BasicStroke(getModel().isRollover() ? 3 : 2));
                g2.setColor(new Color(255, 223, 0));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setBounds(x, y, 200, 55);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        
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
        // Atualizar configurações se necessário
    }
    
    @Override
    protected void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    
    @Override
    public void dispose() {
        // Limpa recursos se necessário
    }
}
