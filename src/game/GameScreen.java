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
        
        int buttonY = 610; // Posicionado abaixo das cartas (que terminam em ~580)
        
        playButton = createActionButton("JOGAR", 340, buttonY, new Color(46, 204, 113)); // Emerald Green
        playButton.addActionListener(e -> playHand());
        add(playButton);
        
        discardButton = createActionButton("DESCARTAR", 520, buttonY, new Color(231, 76, 60)); // Alizarin Red
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
        int projectedMoney = (int) (gameState.getMoney() * gameState.getMultiplier());
        
        if (projectedMoney < gameState.getTargetMoney()) {
            showGameOverScreen();
            return;
        }
        
        gameState.setMoney(projectedMoney);
        
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
        
        // Resultado Final
        int finalMoney = (int) (gameState.getMoney() * gameState.getMultiplier());
        JLabel scoreLabel = new JLabel("Dinheiro Alcançado: $" + formatValue(finalMoney), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 28));
        scoreLabel.setForeground(new Color(255, 223, 0));
        scoreLabel.setBounds(200, 260, 600, 40);
        overlay.add(scoreLabel);
        
        // Meta
        JLabel targetLabel = new JLabel("Meta Necessária: $" + formatValue(gameState.getTargetMoney()), SwingConstants.CENTER);
        targetLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        targetLabel.setForeground(Color.WHITE);
        targetLabel.setBounds(200, 310, 600, 40);
        overlay.add(targetLabel);
        
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
        JLabel moneyLabel = new JLabel("Dinheiro: $" + formatValue(gameState.getMoney()), SwingConstants.CENTER);
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
        
        // Overlay com desenho customizado
        JPanel overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fundo escuro
                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
                g2.setColor(new Color(0, 0, 0, 200));
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Caixa central
                int boxW = 500;
                int boxH = 400;
                int boxX = (getWidth() - boxW) / 2;
                int boxY = (getHeight() - boxH) / 2;
                
                // Sombra da caixa
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(boxX + 10, boxY + 10, boxW, boxH, 30, 30);
                
                // Fundo da caixa
                GradientPaint bgPaint = new GradientPaint(boxX, boxY, new Color(40, 40, 60), boxX, boxY + boxH, new Color(20, 20, 30));
                g2.setPaint(bgPaint);
                g2.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);
                
                // Borda da caixa
                g2.setColor(new Color(100, 100, 150));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(boxX, boxY, boxW, boxH, 30, 30);
                
                // Título "MÃO JOGADA"
                g2.setColor(new Color(150, 200, 255));
                g2.setFont(new Font("Arial", Font.BOLD, 20));
                String title = "MÃO JOGADA";
                int titleW = g2.getFontMetrics().stringWidth(title);
                g2.drawString(title, boxX + (boxW - titleW) / 2, boxY + 50);
                
                // Nome da mão (ex: DOIS PARES)
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 40));
                String handName = hand.getName().toUpperCase();
                int handW = g2.getFontMetrics().stringWidth(handName);
                g2.drawString(handName, boxX + (boxW - handW) / 2, boxY + 110);
                
                // Divisória
                g2.setColor(new Color(255, 255, 255, 50));
                g2.drawLine(boxX + 50, boxY + 150, boxX + boxW - 50, boxY + 150);
                
                // Multiplicador
                g2.setFont(new Font("Arial", Font.PLAIN, 24));
                g2.setColor(new Color(200, 200, 200));
                String multText = "Bônus: ";
                int multLabelW = g2.getFontMetrics().stringWidth(multText);
                g2.drawString(multText, boxX + 150, boxY + 200);
                
                g2.setFont(new Font("Arial", Font.BOLD, 30));
                g2.setColor(new Color(100, 255, 100));
                String multValue = "+" + String.format("%.1f", handMultiplier) + "x";
                g2.drawString(multValue, boxX + 150 + multLabelW, boxY + 200);
                
                // Novo Total
                g2.setFont(new Font("Arial", Font.BOLD, 24));
                g2.setColor(new Color(255, 223, 0));
                String totalText = "Total Atual: " + String.format("%.1f", gameState.getMultiplier()) + "x";
                int totalW = g2.getFontMetrics().stringWidth(totalText);
                g2.drawString(totalText, boxX + (boxW - totalW) / 2, boxY + 260);
                
                g2.dispose();
            }
        };
        overlay.setBounds(0, 0, 1000, 700);
        overlay.setLayout(null);
        
        // Botão continuar (centralizado na parte inferior da caixa)
        JButton continueButton = createStyledButton("CONTINUAR", 400, 530);
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
    
    private JButton createActionButton(String text, int x, int y, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRoundRect(3, 3, w - 3, h - 3, 20, 20);

                // Variáveis de estado
                Color c1 = baseColor;
                Color c2 = baseColor.darker();
                int yOffset = 0;
                
                if (getModel().isPressed()) {
                    c1 = baseColor.darker();
                    c2 = baseColor.darker().darker();
                    yOffset = 2;
                } else if (getModel().isRollover()) {
                    c1 = baseColor.brighter();
                    c2 = baseColor;
                    yOffset = -1;
                }
                
                // Botão principal
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, yOffset, w - 3, h - 3, 20, 20);
                
                // Borda interna brilhante (efeito 3D)
                g2.setStroke(new BasicStroke(1));
                g2.setColor(new Color(255, 255, 255, 100));
                g2.drawRoundRect(1, yOffset + 1, w - 5, h - 5, 18, 18);
                
                // Texto com sombra
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (w - 3 - fm.stringWidth(getText())) / 2;
                int textY = (h - 3 + fm.getAscent() - fm.getDescent()) / 2 + yOffset;
                
                g2.setColor(new Color(0, 0, 0, 50));
                g2.drawString(getText(), textX + 1, textY + 1);
                
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), textX, textY);
                
                g2.dispose();
            }
        };
        
        button.setBounds(x, y, 160, 50);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    @Override
    public void update() {
    }
    
    @Override
    protected void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Recalcula áreas das cartas se necessário
        if (cardAreas.isEmpty() && !gameState.getPlayerHand().isEmpty()) {
            updateCardAreas();
        }
        
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fundo gradiente caso a imagem falhe
            GradientPaint bgGradient = new GradientPaint(0, 0, new Color(20, 40, 60), 0, getHeight(), new Color(10, 20, 30));
            g.setPaint(bgGradient);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // --- Painel Esquerdo (Status) ---
        drawStatusPanel(g, 30, 30);
        
        // --- Painel Central (Mão Necessária) ---
        drawRequiredHandPanel(g);
        
        // --- Painel Direito (Dinheiro) ---
        drawMoneyPanel(g);
        
        drawPlayerHand(g);
    }

    private String formatValue(double value) {
        if (Math.abs(value) >= 1_000_000) {
            return String.format("%.2e", value);
        }
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }

    private void drawStatusPanel(Graphics2D g, int x, int y) {
        int width = 200;
        int height = 160;
        
        // Fundo do painel
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(x, y, width, height, 15, 15);
        g.setColor(new Color(100, 150, 200));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, height, 15, 15);
        
        // Título estilizado
        g.setColor(new Color(200, 220, 255));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("ESTATÍSTICAS DA RODADA", x + 15, y + 25);
        
        // Linha divisória
        g.setColor(new Color(100, 150, 200, 100));
        g.drawLine(x + 10, y + 35, x + width - 10, y + 35);
        
        // Itens
        int startY = y + 55;
        int gap = 25;
        
        drawStatItem(g, "Round:", String.valueOf(gameState.getCurrentRound()), x + 15, startY);
        drawStatItem(g, "Blind:", gameState.getCurrentBlind() + "/3", x + 15, startY + gap);
        drawStatItem(g, "Descartes:", String.valueOf(gameState.getDiscards()), x + 15, startY + gap * 2);
        
        // Multiplicador com destaque
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(new Color(200, 200, 200));
        g.drawString("Multi:", x + 15, startY + gap * 3 + 2);
        
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(255, 223, 0));
        g.drawString(formatValue(gameState.getMultiplier()) + "x", x + 110, startY + gap * 3 + 2);
    }
    
    private void drawStatItem(Graphics2D g, String label, String value, int x, int y) {
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.setColor(new Color(180, 180, 180));
        g.drawString(label, x, y);
        g.setColor(Color.WHITE);
        g.drawString(value, x + 110, y);
    }

    private void drawRequiredHandPanel(Graphics2D g) {
        int width = 360;
        int height = 90;
        int x = (getWidth() - width) / 2;
        int y = 30;
        
        // Sombra suave
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRoundRect(x + 5, y + 5, width, height, 20, 20);
        
        // Gradiente de fundo
        GradientPaint gradient = new GradientPaint(x, y, new Color(40, 30, 60, 230), x, y + height, new Color(20, 15, 30, 230));
        g.setPaint(gradient);
        g.fillRoundRect(x, y, width, height, 20, 20);
        
        // Borda brilhante indicando status da meta
        int currentProjected = (int)(gameState.getMoney() * gameState.getMultiplier());
        boolean isReached = currentProjected >= gameState.getTargetMoney();
        
        if (isReached) {
            g.setColor(new Color(100, 255, 100)); // Verde se garantiu
        } else {
            g.setColor(new Color(255, 100, 100)); // Vermelho se ainda não
        }
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, height, 20, 20);
        
        // Label "META DA RODADA"
        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String label = "META DA RODADA";
        g.drawString(label, x + 20, y + 25);
        
        // Valor da Meta ($)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String targetText = "$" + formatValue(gameState.getTargetMoney());
        FontMetrics fmTarget = g.getFontMetrics();
        // Ajusta fonte se for muito grande
        if (fmTarget.stringWidth(targetText) > 130) {
            g.setFont(new Font("Arial", Font.BOLD, 28));
        }
        g.drawString(targetText, x + 20, y + 65);
        
        // Separador vertical
        g.setColor(new Color(255, 255, 255, 50));
        g.drawLine(x + 160, y + 15, x + 160, y + 75);
        
        // Projeção atual
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(new Color(180, 180, 180));
        g.drawString("Mão sugerida:", x + 180, y + 25);
        
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(new Color(255, 223, 0));
        String handName = gameState.getRequiredHand().getName();
        // Trunca se for muito longo
        if (g.getFontMetrics().stringWidth(handName) > 170) {
            handName = handName.substring(0, 12) + "...";
        }
        g.drawString(handName, x + 180, y + 45);
        
        // Dinheiro Projetado
        g.setFont(new Font("Arial", Font.BOLD, 14));
        if (isReached) g.setColor(new Color(100, 255, 100));
        else g.setColor(new Color(255, 150, 150));
        
        g.drawString("Proj: $" + formatValue(currentProjected), x + 180, y + 70);
    }

    private void drawMoneyPanel(Graphics2D g) {
        String moneyText = "$" + formatValue(gameState.getMoney());
        g.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        
        int textWidth = fm.stringWidth(moneyText);
        int padding = 20;
        int width = Math.max(textWidth + padding * 2, 100); // Garante largura mínima
        int height = 50;
        int x = getWidth() - width - 30;
        int y = 30;
        
        // Fundo estilo etiqueta
        g.setColor(new Color(20, 60, 20, 200));
        g.fillRoundRect(x, y, width, height, 25, 25);
        
        g.setColor(new Color(50, 200, 50));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, height, 25, 25);
        
        // Texto
        g.setColor(new Color(100, 255, 100));
        // Centraliza texto
        g.drawString(moneyText, x + (width - textWidth) / 2, y + 35);
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
