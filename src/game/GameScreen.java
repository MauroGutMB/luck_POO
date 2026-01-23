package game;

import core.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela principal do jogo
 */
public class GameScreen extends Screen {
    private Image backgroundImage;
    private GameState gameState;
    private CardRenderer cardRenderer;
    private List<Rectangle> cardAreas;
    private JButton playButton;
    private JButton discardButton;
    private Point mousePos;
    
    public GameScreen(ScreenManager screenManager) {
        super(screenManager);
        loadBackground();
        this.gameState = GameManager.getInstance().getGameState();
        this.cardRenderer = CardRenderer.getInstance();
        this.cardAreas = new ArrayList<>();
        this.mousePos = new Point(0, 0);
        setupMouseListeners();
    }
    
    private void loadBackground() {
        try {
            backgroundImage = ImageIO.read(new File("assets/bg-game.png"));
        } catch (IOException e) {
            System.err.println("Erro ao carregar bg-game.png: " + e.getMessage());
            backgroundImage = null;
        }
    }
    
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleCardClick(e.getX(), e.getY());
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
                repaint();
            }
        });
    }
    
    private void handleCardClick(int x, int y) {
        List<PlayingCard> hand = gameState.getPlayerHand();
        int cardWidth = cardRenderer.getCardWidth();
        int cardHeight = cardRenderer.getCardHeight();
        
        // Calcula posição inicial para centralizar (mesma lógica do drawPlayerHand)
        int totalWidth = hand.size() * cardWidth + (hand.size() - 1) * 5;
        int startX = (getWidth() - totalWidth) / 2;
        int startY = 450;
        int cardSpacing = 5;
        
        for (int i = 0; i < hand.size(); i++) {
            int cardX = startX + i * (cardWidth + cardSpacing);
            int cardY = startY;
            
            // Considera a área expandida da carta (pode estar levantada)
            Rectangle cardArea = new Rectangle(cardX, cardY - 25, cardWidth, cardHeight + 25);
            if (cardArea.contains(x, y)) {
                toggleCardSelection(i);
                repaint();
                break;
            }
        }
    }
    
    @Override
    public void initialize() {
        removeAll();
        setOpaque(false);
        cardAreas.clear();
        
        if (gameState.getPlayerHand().isEmpty()) {
            gameState.startNewRound();
            dealInitialHand();
        }
        
        updateCardAreas();
        
        playButton = createActionButton("JOGAR", 400, 550);
        playButton.addActionListener(e -> playHand());
        add(playButton);
        
        discardButton = createActionButton("DESCARTAR", 550, 550);
        discardButton.addActionListener(e -> discardCards());
        add(discardButton);
        
        revalidate();
        repaint();
    }
    
    private void dealInitialHand() {
        gameState.getPlayerHand().clear();
        List<PlayingCard> cards = gameState.getGameDeck().draw(8);
        gameState.getPlayerHand().addAll(cards);
    }
    
    private void updateCardAreas() {
        cardAreas.clear();
        List<PlayingCard> hand = gameState.getPlayerHand();
        
        // Usa as dimensões reais das cartas do sprite sheet
        int cardWidth = cardRenderer.getCardWidth();
        int cardHeight = cardRenderer.getCardHeight();
        
        // Calcula posição inicial para centralizar as cartas
        int totalWidth = hand.size() * cardWidth + (hand.size() - 1) * 5;
        int startX = (getWidth() - totalWidth) / 2;
        int startY = 450;
        int cardSpacing = 5;
        
        for (int i = 0; i < hand.size(); i++) {
            int x = startX + i * (cardWidth + cardSpacing);
            cardAreas.add(new Rectangle(x, startY, cardWidth, cardHeight));
        }
    }
    
    private void toggleCardSelection(int index) {
        if (index < 0 || index >= gameState.getPlayerHand().size()) {
            return;
        }
        
        PlayingCard card = gameState.getPlayerHand().get(index);
        
        if (gameState.getSelectedCards().contains(card)) {
            gameState.getSelectedCards().remove(card);
        } else {
            if (gameState.getSelectedCards().size() < 5) {
                gameState.getSelectedCards().add(card);
            }
        }
    }
    
    private void playHand() {
        if (gameState.getSelectedCards().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione entre 1 e 5 cartas!", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (gameState.getSelectedCards().size() > 5) {
            JOptionPane.showMessageDialog(this, "Selecione no máximo 5 cartas!", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        PokerHand hand = PokerHandEvaluator.evaluateHand(gameState.getSelectedCards());
        double handMultiplier = hand.getMultiplier();
        gameState.addMultiplier(handMultiplier);
        
        showHandResultScreen(hand, handMultiplier);
    }
    
    private void discardCards() {
        if (gameState.getSelectedCards().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione cartas para descartar!", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (gameState.getDiscards() <= 0) {
            JOptionPane.showMessageDialog(this, "Sem descartes restantes!", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int discardCount = gameState.getSelectedCards().size();
        
        gameState.getPlayerHand().removeAll(gameState.getSelectedCards());
        gameState.getGameDeck().discard(gameState.getSelectedCards());
        gameState.getSelectedCards().clear();
        
        List<PlayingCard> newCards = gameState.getGameDeck().draw(discardCount);
        if (newCards.size() < discardCount) {
            JOptionPane.showMessageDialog(this, "Deck vazio! Fim da rodada.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        gameState.getPlayerHand().addAll(newCards);
        gameState.decrementDiscards();
        
        initialize();
    }
    
    private void endRound() {
        if (gameState.getMultiplier() <= 1.5) {
            showGameOverScreen();
            return;
        }
        
        int earnings = (int) (gameState.getMoney() * gameState.getMultiplier());
        gameState.setMoney(earnings);
        
        showRoundCompleteScreen();
    }
    
    private void showGameOverScreen() {
        removeAll();
        setLayout(null);
        
        // Overlay escuro
        JPanel overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
                g2.setColor(new Color(0, 0, 0, 200));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setBounds(0, 0, 1000, 700);
        overlay.setLayout(null);
        
        // Título
        JLabel titleLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 60));
        titleLabel.setForeground(new Color(255, 50, 50));
        titleLabel.setBounds(250, 150, 500, 80);
        overlay.add(titleLabel);
        
        // Multiplicador
        JLabel multLabel = new JLabel("Multiplicador: " + String.format("%.1f", gameState.getMultiplier()) + "x", SwingConstants.CENTER);
        multLabel.setFont(new Font("Arial", Font.BOLD, 28));
        multLabel.setForeground(new Color(255, 223, 0));
        multLabel.setBounds(250, 260, 500, 40);
        overlay.add(multLabel);
        
        // Mínimo necessário
        JLabel minLabel = new JLabel("Mínimo necessário: 1.5x", SwingConstants.CENTER);
        minLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        minLabel.setForeground(Color.WHITE);
        minLabel.setBounds(250, 310, 500, 40);
        overlay.add(minLabel);
        
        // Botão menu
        JButton menuButton = createStyledButton("MENU", 410, 420);
        menuButton.addActionListener(e -> screenManager.changeScreen("menu"));
        overlay.add(menuButton);
        
        add(overlay);
        revalidate();
        repaint();
    }
    
    private void showRoundCompleteScreen() {
        removeAll();
        setLayout(null);
        
        // Overlay escuro
        JPanel overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setBounds(0, 0, 1000, 700);
        overlay.setLayout(null);
        
        // Título
        JLabel titleLabel = new JLabel("RODADA COMPLETA!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        titleLabel.setForeground(new Color(100, 255, 100));
        titleLabel.setBounds(200, 120, 600, 70);
        overlay.add(titleLabel);
        
        // Multiplicador
        JLabel multLabel = new JLabel("Multiplicador: " + String.format("%.1f", gameState.getMultiplier()) + "x", SwingConstants.CENTER);
        multLabel.setFont(new Font("Arial", Font.BOLD, 32));
        multLabel.setForeground(new Color(255, 223, 0));
        multLabel.setBounds(200, 220, 600, 50);
        overlay.add(multLabel);
        
        // Dinheiro
        JLabel moneyLabel = new JLabel("Dinheiro: $" + gameState.getMoney(), SwingConstants.CENTER);
        moneyLabel.setFont(new Font("Arial", Font.BOLD, 32));
        moneyLabel.setForeground(new Color(100, 255, 100));
        moneyLabel.setBounds(200, 280, 600, 50);
        overlay.add(moneyLabel);
        
        // Botão continuar
        JButton continueButton = createStyledButton("CONTINUAR", 300, 400);
        continueButton.addActionListener(e -> {
            gameState.nextRound();
            gameState.startNewRound();
            initialize();
        });
        overlay.add(continueButton);
        
        // Botão sair
        JButton exitButton = createStyledButton("SAIR", 500, 400);
        exitButton.addActionListener(e -> screenManager.changeScreen("menu"));
        overlay.add(exitButton);
        
        add(overlay);
        revalidate();
        repaint();
    }
    
    private JButton createStyledButton(String text, int x, int y) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isRollover()) {
                    g2.setColor(new Color(90, 140, 190, 250));
                } else {
                    g2.setColor(new Color(50, 90, 140, 220));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                g2.setStroke(new BasicStroke(3));
                g2.setColor(new Color(255, 223, 0));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 15, 15);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setBounds(x, y, 180, 50);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        
        return button;
    }
    
    private void showHandResultScreen(PokerHand hand, double handMultiplier) {
        removeAll();
        setLayout(null);
        
        // Overlay escuro
        JPanel overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setBounds(0, 0, 1000, 700);
        overlay.setLayout(null);
        
        // Título
        JLabel titleLabel = new JLabel("RESULTADO", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        titleLabel.setForeground(new Color(255, 223, 0));
        titleLabel.setBounds(200, 120, 600, 70);
        overlay.add(titleLabel);
        
        // Mão jogada
        JLabel handLabel = new JLabel("Você jogou: " + hand.getName(), SwingConstants.CENTER);
        handLabel.setFont(new Font("Arial", Font.BOLD, 32));
        handLabel.setForeground(Color.WHITE);
        handLabel.setBounds(200, 220, 600, 50);
        overlay.add(handLabel);
        
        // Multiplicador adicionado
        JLabel multLabel = new JLabel("Multiplicador: +" + handMultiplier + "x", SwingConstants.CENTER);
        multLabel.setFont(new Font("Arial", Font.BOLD, 28));
        multLabel.setForeground(new Color(100, 255, 100));
        multLabel.setBounds(200, 280, 600, 50);
        overlay.add(multLabel);
        
        // Total
        JLabel totalLabel = new JLabel("Total: " + String.format("%.1f", gameState.getMultiplier()) + "x", SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 28));
        totalLabel.setForeground(new Color(255, 223, 0));
        totalLabel.setBounds(200, 330, 600, 50);
        overlay.add(totalLabel);
        
        // Botão continuar
        JButton continueButton = createStyledButton("CONTINUAR", 410, 430);
        continueButton.addActionListener(e -> {
            if (gameState.getCurrentBlind() < 3) {
                gameState.nextBlind();
                dealInitialHand();
                initialize();
            } else {
                endRound();
            }
        });
        overlay.add(continueButton);
        
        add(overlay);
        revalidate();
        repaint();
    }
    
    private JButton createActionButton(String text, int x, int y) {
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                g2.setStroke(new BasicStroke(2));
                g2.setColor(new Color(255, 223, 0));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setBounds(x, y, 120, 45);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        
        return button;
    }
    
    @Override
    public void update() {
    }
    
    @Override
    protected void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Recalcula áreas das cartas se necessário (primeira renderização)
        if (cardAreas.isEmpty() && !gameState.getPlayerHand().isEmpty()) {
            updateCardAreas();
        }
        
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(50, 80, 180, 120);
        
        g.setColor(new Color(255, 223, 0));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Round: " + gameState.getCurrentRound(), 70, 110);
        g.drawString("Blind: " + gameState.getCurrentBlind() + "/3", 70, 135);
        g.drawString("Descartes: " + gameState.getDiscards(), 70, 160);
        g.drawString("Multi: " + String.format("%.1f", gameState.getMultiplier()) + "x", 70, 185);
        
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(320, 100, 360, 100);
        
        g.setColor(new Color(255, 223, 0));
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Mão Necessária:", 380, 130);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(gameState.getRequiredHand().getName(), 380, 165);
        
        g.setColor(new Color(255, 223, 0));
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("$" + gameState.getMoney(), 800, 50);
        
        drawPlayerHand(g);
    }
    
    private void drawPlayerHand(Graphics2D g) {
        List<PlayingCard> hand = gameState.getPlayerHand();
        
        // Usa as dimensões reais das cartas
        int cardWidth = cardRenderer.getCardWidth();
        int cardHeight = cardRenderer.getCardHeight();
        
        // Calcula posição inicial para centralizar
        int totalWidth = hand.size() * cardWidth + (hand.size() - 1) * 5;
        int startX = (getWidth() - totalWidth) / 2;
        int startY = 450;
        int cardSpacing = 5;
        
        for (int i = 0; i < hand.size(); i++) {
            PlayingCard card = hand.get(i);
            int x = startX + i * (cardWidth + cardSpacing);
            int y = startY;
            
            boolean isSelected = gameState.getSelectedCards().contains(card);
            if (isSelected) {
                y -= 25;
            }
            
            // Verifica hover considerando a área expandida da carta
            Rectangle cardArea = new Rectangle(x, y - 25, cardWidth, cardHeight + 25);
            boolean isHovered = cardArea.contains(mousePos);
            if (isHovered && !isSelected) {
                y -= 15;
            }
            
            cardRenderer.drawCard(g, card, x, y, cardWidth, cardHeight);
            
            if (isSelected) {
                g.setColor(new Color(100, 255, 100));
                g.setStroke(new BasicStroke(3));
                g.drawRoundRect(x - 2, y - 2, cardWidth + 4, cardHeight + 4, 8, 8);
            } else if (isHovered) {
                g.setColor(new Color(255, 255, 100, 150));
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(x - 1, y - 1, cardWidth + 2, cardHeight + 2, 8, 8);
            }
        }
    }
    
    @Override
    public void dispose() {
    }
}
