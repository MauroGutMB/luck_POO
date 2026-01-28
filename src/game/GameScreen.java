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
    private Image revolverImage;
    private GameState gameState;
    private CardRenderer cardRenderer;
    private DiceRenderer diceRenderer; // New renderer
    private List<Rectangle> cardAreas;
    private JButton playButton;
    private JButton discardButton;
    private Point mousePos;
    
    // Cutscene/Roulette State
    private enum RouletteState { NONE, WARNING, ROLLING, LOADING, SPINNING, SHOOTING, FIRING, RESULT }
    private RouletteState rouletteState = RouletteState.NONE;
    private int diceAnimationFrame = 0;
    private int diceAnimationResult = 1;
    // Animation vars
    private int loadingBulletIndex = 0;
    private double cylinderAngle = 0; // Angle for spinning animation
    private int shootingFrame = 0;
    private int firingFrame = 0; // Para animação de tiro
    private boolean roundDied = false; // Armazena resultado temporariamente
    private int finalChamberSlot = 0; // Slot que vai parar no gatilho (0-5)
    private Timer diceTimer;
    private String rouletteResultText = "";
    private boolean rouletteSuccess = false;
    private JButton rouletteConfirmButton;
    private JButton rouletteCancelButton;
    private JButton rouletteContinueButton;
    
    public GameScreen(ScreenManager screenManager) {
        super(screenManager);
        loadBackground();
        this.gameState = GameManager.getInstance().getGameState();
        this.cardRenderer = CardRenderer.getInstance();
        this.diceRenderer = DiceRenderer.getInstance(); // Initialize
        this.cardAreas = new ArrayList<>();
        this.mousePos = new Point(0, 0);
        setupMouseListeners();
        setupRouletteButtons();
    }
    
    private void setupRouletteButtons() {
        // Inicializa botões do overlay de roleta, mas não os adiciona ainda
        rouletteConfirmButton = createActionButton("PUXAR GATILHO", 0, 0, new Color(142, 68, 173));
        rouletteConfirmButton.addActionListener(e -> startDiceRollCutscene());
        
        rouletteCancelButton = createActionButton("CANCELAR", 0, 0, new Color(120, 120, 120));
        rouletteCancelButton.addActionListener(e -> {
            rouletteState = RouletteState.NONE;
            setGameButtonsEnabled(true);
            removeAll(); // Limpa botões antigos
            add(playButton);
            add(discardButton);
            add(createActionButton("ROLETA", 230, 610, new Color(142, 68, 173))); // Recria botão
            revalidate();
            repaint();
            initialize(); // Restaura controles padrão
        });
        
        rouletteContinueButton = createActionButton("CONTINUAR", 0, 0, new Color(46, 204, 113));
        rouletteContinueButton.addActionListener(e -> {
            rouletteState = RouletteState.NONE;
            if (rouletteSuccess) {
                showRoundCompleteScreen();
            } else {
                showGameOverScreen();
            }
        });
    }

    private void setGameButtonsEnabled(boolean enabled) {
        playButton.setEnabled(enabled);
        discardButton.setEnabled(enabled);
        // Desabilita visualmente se necessário, ou apenas lógica
        for(Component c : getComponents()) {
            if (c instanceof JButton && c != rouletteConfirmButton && c != rouletteCancelButton && c != rouletteContinueButton) {
                c.setEnabled(enabled);
            }
        }
    }
    
    private void loadBackground() {
        try {
            backgroundImage = ImageIO.read(new File("assets/bg-game.png"));
            revolverImage = ImageIO.read(new File("assets/revolver.png"));
        } catch (IOException e) {
            System.err.println("Erro ao carregar assets: " + e.getMessage());
        }
    }
    
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (rouletteState == RouletteState.NONE) {
                    handleCardClick(e.getX(), e.getY());
                }
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
        
        playButton = createActionButton("JOGAR", 410, buttonY, new Color(46, 204, 113)); // Emerald Green
        playButton.addActionListener(e -> playHand());
        add(playButton);
        
        discardButton = createActionButton("DESCARTAR", 590, buttonY, new Color(231, 76, 60)); // Alizarin Red
        discardButton.addActionListener(e -> discardCards());
        add(discardButton);

        // Botão Roleta Russa
        JButton rouletteButton = createActionButton("ROLETA", 230, buttonY, new Color(142, 68, 173)); // Wisteria Purple
        rouletteButton.addActionListener(e -> playRussianRoulette());
        add(rouletteButton);

        
        revalidate();
        repaint();
    }
    
    private void dealInitialHand() {
        gameState.getPlayerHand().clear();
        List<PlayingCard> cards = gameState.getGameDeck().draw(8);
        
        if (cards.size() < 8) {
            JOptionPane.showMessageDialog(this, "Deck vazio! O jogo acabou.", "Fim de Jogo", JOptionPane.WARNING_MESSAGE);
            showGameOverScreen();
            return;
        }
        
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

        if (gameState.getHandsPlayed() >= gameState.getMaxHands()) {
            JOptionPane.showMessageDialog(this, "Sem mãos restantes nesta blind!", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        PokerHand hand = PokerHandEvaluator.evaluateHand(gameState.getSelectedCards());
        boolean isSuccess = hand.compareTo(gameState.getRequiredHand()) >= 0;

        // Consome as cartas jogadas
        gameState.getPlayerHand().removeAll(gameState.getSelectedCards());
        gameState.getGameDeck().discard(gameState.getSelectedCards());
        
        gameState.incrementHandsPlayed();
        
        double handMultiplier = 0;
        if (isSuccess) {
            handMultiplier = hand.getMultiplier();
            gameState.addMultiplier(handMultiplier);
        } else {
            // Penalidade
            double penalty = 0.2 * gameState.getCurrentRound();
            gameState.addMultiplier(-penalty);
            // Garante que não fique negativo (opcional, mas bom pra evitar bugs visuais extremos)
            if (gameState.getMultiplier() < 0) gameState.setMultiplier(0);
        }
        
        showHandResultScreen(hand, handMultiplier, isSuccess);
    }

    private void playRussianRoulette() {
        rouletteState = RouletteState.WARNING;
        setGameButtonsEnabled(false);
        repaint();
    }

    private void startDiceRollCutscene() {
        rouletteState = RouletteState.ROLLING;
        diceAnimationFrame = 0;
        // Determine final result beforehand
        diceAnimationResult = (int) (Math.random() * 6) + 1; // 1 to 6
        
        // Timer for animation
        diceTimer = new Timer(50, new ActionListener() {
            int frames = 0;
            int maxFrames = 30; // ~1.5 seconds spin
            
            @Override
            public void actionPerformed(ActionEvent e) {
                frames++;
                // Random face during spin
                diceAnimationFrame = (int)(Math.random() * 6) + 1;
                repaint();
                
                if (frames >= maxFrames) {
                    ((Timer)e.getSource()).stop();
                    finishDiceRoll();
                }
            }
        });
        diceTimer.start();
    }
    
    private void finishDiceRoll() {
        // Show final result
        diceAnimationFrame = diceAnimationResult;
        repaint();
        
        // Pause briefly to show result then start loading bullets
        Timer pause = new Timer(1000, e -> {
            ((Timer)e.getSource()).stop();
            startLoadingCutscene();
        });
        pause.setRepeats(false);
        pause.start();
    }
    
    private void startLoadingCutscene() {
        rouletteState = RouletteState.LOADING;
        loadingBulletIndex = 0;
        
        // Timer adding bullets one by one
        Timer loadTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadingBulletIndex++;
                repaint();
                
                if (loadingBulletIndex >= diceAnimationResult) {
                     ((Timer)e.getSource()).stop();
                     // Pause before spinning
                     Timer pause = new Timer(500, ev -> {
                         ((Timer)ev.getSource()).stop();
                         startSpinningCutscene();
                     });
                     pause.setRepeats(false);
                     pause.start();
                }
            }
        });
        loadTimer.start();
    }

    private void startSpinningCutscene() {
        rouletteState = RouletteState.SPINNING;
        
        // Decidir resultado agora para alinhar visualmente com a lógica
        // Slots 0 a (bullets-1) têm bala.
        // Escolhemos um slot aleatório de 0 a 5 para ficar no topo (gatilho).
        finalChamberSlot = (int)(Math.random() * 6);
        
        // Calcular angulo alvo para que o slot escolhido termine no topo (visual -90 graus)
        double targetRotationForAlignment = (360 - (finalChamberSlot * 60)) % 360;
        double totalRotation = 360 * 5 + targetRotationForAlignment; // 5 voltas completas + alinhamento
        
        long startTime = System.currentTimeMillis();
        long duration = 2000; // 2 segundos de giro

        // Timer spinning the chamber
        Timer spinTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long now = System.currentTimeMillis();
                float progress = (float)(now - startTime) / duration;
                
                if (progress >= 1.0f) {
                    progress = 1.0f;
                    cylinderAngle = totalRotation;
                    repaint();
                    ((Timer)e.getSource()).stop();
                    
                    // Pausa muda mostrando o tambor alinhado antes do tiro
                    Timer pause = new Timer(1000, ev -> {
                        ((Timer)ev.getSource()).stop();
                        startShootingCutscene();
                    });
                    pause.setRepeats(false);
                    pause.start();
                } else {
                    // Ease-out
                    float ease = 1 - (1 - progress) * (1 - progress) * (1 - progress); 
                    cylinderAngle = totalRotation * ease;
                    repaint();
                }
            }
        });
        spinTimer.start();
    }

    private void startShootingCutscene() {
        rouletteState = RouletteState.SHOOTING;
        shootingFrame = 0;
        
        // Shake animation timer
        Timer shootTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shootingFrame++;
                repaint();
                
                if (shootingFrame > 20) { // ~1 second of tension
                    ((Timer)e.getSource()).stop();
                    resolveRussianRoulette();
                }
            }
        });
        shootTimer.start();
    }
    
    private void resolveRussianRoulette() {
        // Verificar se houve disparo baseado no slot que parou
        // Slots 0 até diceAnimationResult-1 estão carregados.
        roundDied = finalChamberSlot < diceAnimationResult;
        
        rouletteState = RouletteState.FIRING;
        firingFrame = 0;
        
        // Timer da animação de disparo (Flash ou Click)
        Timer fireTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                firingFrame++;
                repaint();
                
                // Duração curta para o flash (ex: 15 frames = ~450ms)
                if (firingFrame > 15) {
                    ((Timer)e.getSource()).stop();
                    finalizeRouletteResult();
                }
            }
        });
        fireTimer.start();
    }

    private void finalizeRouletteResult() {
        if (roundDied) {
            // Morreu
            rouletteSuccess = false;
            rouletteResultText = "BANG! O tambor parou na bala (S" + (finalChamberSlot + 1) + ").";
        } else {
            // Sobreviveu
            rouletteSuccess = true;
            double bonusFactor = Math.pow(diceAnimationResult + 1, 2);
            gameState.setMultiplier(gameState.getMultiplier() * bonusFactor);
            rouletteResultText = "CLICK! Sobreviveu. (S" + (finalChamberSlot + 1) + " Vazio). Bônus: " + (int)bonusFactor + "x!";
            
            // Vence a blind automaticamente
            gameState.setMoney((int)(gameState.getMoney() * gameState.getMultiplier()));
            if (gameState.getMoney() < gameState.getTargetMoney()) {
                 gameState.setMoney(gameState.getTargetMoney());
            }
        }
        rouletteState = RouletteState.RESULT;
        repaint();
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
        
        // Preenche a mão até voltar a ter 8 cartas
        int currentHandSize = gameState.getPlayerHand().size();
        int cardsToDraw = 8 - currentHandSize;
        
        if (cardsToDraw > 0) {
            List<PlayingCard> newCards = gameState.getGameDeck().draw(cardsToDraw);
            if (newCards.size() < cardsToDraw) {
                JOptionPane.showMessageDialog(this, "Deck vazio! O jogo acabou.", "Fim de Jogo", JOptionPane.WARNING_MESSAGE);
                showGameOverScreen();
                return;
            }
            gameState.getPlayerHand().addAll(newCards);
        }
        
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
    
    private void showHandResultScreen(PokerHand hand, double handMultiplier, boolean isSuccess) {
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
                
                // Fundo da caixa dinâmico (verde se sucesso, vermelho se falha)
                Color c1 = isSuccess ? new Color(40, 60, 40) : new Color(60, 40, 40);
                Color c2 = isSuccess ? new Color(20, 30, 20) : new Color(30, 20, 20);
                GradientPaint bgPaint = new GradientPaint(boxX, boxY, c1, boxX, boxY + boxH, c2);
                
                g2.setPaint(bgPaint);
                g2.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);
                
                // Borda da caixa
                g2.setColor(isSuccess ? new Color(100, 150, 100) : new Color(150, 100, 100));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(boxX, boxY, boxW, boxH, 30, 30);
                
                // Título
                g2.setColor(isSuccess ? new Color(150, 255, 200) : new Color(255, 150, 150));
                g2.setFont(new Font("Arial", Font.BOLD, 20));
                String title = isSuccess ? "MÃO JOGADA" : "MÃO INSUFICIENTE";
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
                
                // Info
                if (isSuccess) {
                    // Multiplicador ganho
                    g2.setFont(new Font("Arial", Font.PLAIN, 24));
                    g2.setColor(new Color(200, 200, 200));
                    String multText = "Bônus: ";
                    int multLabelW = g2.getFontMetrics().stringWidth(multText);
                    g2.drawString(multText, boxX + 150, boxY + 200);
                    
                    g2.setFont(new Font("Arial", Font.BOLD, 30));
                    g2.setColor(new Color(100, 255, 100));
                    String multValue = "+" + String.format("%.1f", handMultiplier) + "x";
                    g2.drawString(multValue, boxX + 150 + multLabelW, boxY + 200);
                } else {
                    // Penalidade
                    g2.setFont(new Font("Arial", Font.PLAIN, 24));
                    g2.setColor(new Color(255, 150, 150));
                    String penText = "Penalidade: ";
                    int penLabelW = g2.getFontMetrics().stringWidth(penText);
                    g2.drawString(penText, boxX + 130, boxY + 200);
                    
                    g2.setFont(new Font("Arial", Font.BOLD, 30));
                    g2.setColor(Color.RED);
                    String penValue = "-" + String.format("%.1f", 0.2 * gameState.getCurrentRound()) + "x";
                    g2.drawString(penValue, boxX + 130 + penLabelW, boxY + 200);
                    
                    g2.setFont(new Font("Arial", Font.PLAIN, 18));
                    g2.setColor(new Color(255, 200, 200));
                    String reqText = "Necessário: " + gameState.getRequiredHand().getName();
                    int reqW = g2.getFontMetrics().stringWidth(reqText);
                    g2.drawString(reqText, boxX + (boxW - reqW) / 2, boxY + 235);
                }
                
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
        
        boolean canTryAgain = !isSuccess && !gameState.getPlayerHand().isEmpty() && gameState.getHandsPlayed() < gameState.getMaxHands();
        
        // Botão continuar (centralizado na parte inferior da caixa)
        String btnText = isSuccess ? "CONTINUAR" : (canTryAgain ? "TENTAR NOVAMENTE" : "FIM DE JOGO");
        JButton continueButton = createStyledButton(btnText, 400, 530);
        continueButton.addActionListener(e -> {
            if (isSuccess) {
                if (gameState.getCurrentBlind() < 3) {
                    gameState.nextBlind();
                    dealInitialHand();
                    initialize();
                } else {
                    endRound();
                }
            } else {
                if (canTryAgain) {
                    // Tentar novamente: remove cartas selecionadas e redesenha (já removidas no playHand, só limpa seleção)
                    gameState.getSelectedCards().clear();
                    initialize();
                } else {
                    showGameOverScreen();
                }
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
        
        if (rouletteState != RouletteState.NONE) {
            drawRouletteOverlay(g);
        }
    }

    private void drawRouletteOverlay(Graphics2D g) {
        // Overlay escuro
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        
        if (rouletteState == RouletteState.WARNING) {
            // Caixa de Aviso
            g.setColor(new Color(50, 20, 20));
            g.fillRoundRect(cx - 250, cy - 150, 500, 300, 20, 20);
            g.setColor(new Color(200, 50, 50));
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(cx - 250, cy - 150, 500, 300, 20, 20);
            
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.setColor(new Color(255, 80, 80));
            drawCenteredText(g, "RISCO EXTREMO", cy - 100);
            
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.setColor(Color.WHITE);
            drawCenteredText(g, "O dado definirá o número de balas no tambor.", cy - 50);
            drawCenteredText(g, "Se morrer, FIM DE JOGO.", cy - 20);
            drawCenteredText(g, "Se viver, MULTIPLICADOR INSANO e Vitória.", cy + 10);
            
            // Render Buttons manually since they are not in component hierarchy properly during overlay paint usually but here we added action listeners
            // Let's manually position and draw them here, but we need to ensure they are added to layout or handled via mouse clicks.
            // Since we're in custom render, we can just effectively draw them and rely on a click handler or add them as components.
            // The cleanest way for "Screen" is adding components.
            // But we need to update their positions.
            
            rouletteConfirmButton.setBounds(cx - 210, cy + 60, 200, 50);
            rouletteCancelButton.setBounds(cx + 10, cy + 60, 200, 50);
            
            // "Paint" them by delegating
            g.translate(rouletteConfirmButton.getX(), rouletteConfirmButton.getY());
            rouletteConfirmButton.paint(g);
            g.translate(-rouletteConfirmButton.getX(), -rouletteConfirmButton.getY());
            
            g.translate(rouletteCancelButton.getX(), rouletteCancelButton.getY());
            rouletteCancelButton.paint(g);
            g.translate(-rouletteCancelButton.getX(), -rouletteCancelButton.getY());
            
            // Add to component list if not there? 
            // In swing, painting doesn't add interaction. 
            // We should add them to the panel when entering state and remove when leaving.
            if (rouletteConfirmButton.getParent() != this) {
               add(rouletteConfirmButton);
               add(rouletteCancelButton);
               revalidate();
            }

        } else if (rouletteState == RouletteState.ROLLING) {
            // Remove buttons if rolling
             if (rouletteConfirmButton.getParent() == this) {
                remove(rouletteConfirmButton);
                remove(rouletteCancelButton);
                revalidate();
            }

            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.setColor(new Color(142, 68, 173));
            drawCenteredText(g, "Rolando o dado...", cy - 100);
            
            // Desenha o dado no centro
            int diceSize = 128;
            g.setColor(new Color(142, 68, 173, 100)); // Roxo brilhante
            g.fillOval(cx - diceSize/2 - 20, cy - diceSize/2 - 20, diceSize + 40, diceSize + 40);
            diceRenderer.drawFace(g, diceAnimationFrame, cx - diceSize/2, cy - diceSize/2, diceSize, diceSize);
            
        } else if (rouletteState == RouletteState.LOADING) {
             g.setFont(new Font("Arial", Font.BOLD, 40));
             g.setColor(new Color(231, 76, 60));
             drawCenteredText(g, "Carregando " + diceAnimationResult + " bala(s)...", cy - 200);
             
             // --- Desenho do Tambor (Cylinder) ---
             int cylRadius = 120; // Raio do tambor
             int holeDist = 70;   // Distância dos buracos ao centro
             int holeRadius = 25; // Raio de cada buraco
             
             // Corpo do tambor
             g.setColor(new Color(40, 40, 40));
             g.fillOval(cx - cylRadius, cy - cylRadius, cylRadius * 2, cylRadius * 2);
             g.setColor(new Color(20, 20, 20));
             g.setStroke(new BasicStroke(5));
             g.drawOval(cx - cylRadius, cy - cylRadius, cylRadius * 2, cylRadius * 2);
             
             // Eixo central
             g.setColor(new Color(10, 10, 10));
             g.fillOval(cx - 15, cy - 15, 30, 30);
             
             // Balas/Buracos
             for (int i = 0; i < 6; i++) {
                 double theta = Math.toRadians(i * 60 - 90); // -90 para começar do topo (12h)
                 int hx = cx + (int)(Math.cos(theta) * holeDist);
                 int hy = cy + (int)(Math.sin(theta) * holeDist);
                 
                 // Fundo do buraco
                 g.setColor(Color.BLACK);
                 g.fillOval(hx - holeRadius, hy - holeRadius, holeRadius * 2, holeRadius * 2);
                 
                 // Bala (se já foi carregada neste slot)
                 if (i < loadingBulletIndex) {
                     // Cartucho (Dourado)
                     g.setColor(new Color(218, 165, 32)); 
                     g.fillOval(hx - holeRadius + 2, hy - holeRadius + 2, holeRadius * 2 - 4, holeRadius * 2 - 4);
                     // Espoleta (Prata/Cinza)
                     g.setColor(new Color(180, 180, 180)); 
                     g.fillOval(hx - 8, hy - 8, 16, 16);
                     // Anel da espoleta
                     g.setColor(new Color(150, 150, 150));
                     g.setStroke(new BasicStroke(1));
                     g.drawOval(hx - 8, hy - 8, 16, 16);
                 }
                 
                 // Borda do buraco
                 g.setColor(new Color(80, 80, 80));
                 g.setStroke(new BasicStroke(2));
                 g.drawOval(hx - holeRadius, hy - holeRadius, holeRadius * 2, holeRadius * 2);
             }

        } else if (rouletteState == RouletteState.SPINNING) {
             // Texto ou Efeito de "Giro"
             g.setFont(new Font("Arial", Font.BOLD, 40));
             g.setColor(new Color(231, 76, 60));
             drawCenteredText(g, "Girando Tambor...", cy - 200);
             
             int cylRadius = 120;
             int holeDist = 70;
             int holeRadius = 25;
             
             Graphics2D gSpin = (Graphics2D) g.create();
             // Renderização de qualidade para rotação
             gSpin.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             gSpin.rotate(Math.toRadians(cylinderAngle), cx, cy); // Aplica rotação
             
             // Corpo
             gSpin.setColor(new Color(40, 40, 40));
             gSpin.fillOval(cx - cylRadius, cy - cylRadius, cylRadius * 2, cylRadius * 2);
             gSpin.setColor(new Color(20, 20, 20));
             gSpin.setStroke(new BasicStroke(5));
             gSpin.drawOval(cx - cylRadius, cy - cylRadius, cylRadius * 2, cylRadius * 2);
             
             // Eixo
             gSpin.setColor(new Color(10, 10, 10));
             gSpin.fillOval(cx - 15, cy - 15, 30, 30);
             
             for (int i = 0; i < 6; i++) {
                 double theta = Math.toRadians(i * 60 - 90);
                 int hx = cx + (int)(Math.cos(theta) * holeDist);
                 int hy = cy + (int)(Math.sin(theta) * holeDist);
                 
                 gSpin.setColor(Color.BLACK);
                 gSpin.fillOval(hx - holeRadius, hy - holeRadius, holeRadius * 2, holeRadius * 2);
                 
                 // Desenha todas as balas carregadas
                 if (i < diceAnimationResult) {
                     gSpin.setColor(new Color(218, 165, 32));
                     gSpin.fillOval(hx - holeRadius + 2, hy - holeRadius + 2, holeRadius * 2 - 4, holeRadius * 2 - 4);
                     gSpin.setColor(new Color(180, 180, 180)); 
                     gSpin.fillOval(hx - 8, hy - 8, 16, 16);
                 }
                 
                 gSpin.setColor(new Color(80, 80, 80));
                 gSpin.setStroke(new BasicStroke(2));
                 gSpin.drawOval(hx - holeRadius, hy - holeRadius, holeRadius * 2, holeRadius * 2);
             }
             gSpin.dispose();
             
        } else if (rouletteState == RouletteState.SHOOTING) {
             g.setFont(new Font("Arial", Font.BOLD, 40));
             g.setColor(new Color(255, 50, 50));
             drawCenteredText(g, "Puxando o Gatilho...", cy - 150);

             if (revolverImage != null) {
                 int revW = 300;
                 int revH = (int)((double)revW / revolverImage.getWidth(null) * revolverImage.getHeight(null));
                 
                 // Efeito de tremer (shake)
                 int offsetX = (int)(Math.random() * 6 - 3);
                 int offsetY = (int)(Math.random() * 6 - 3);
                 
                 Graphics2D g2d = (Graphics2D) g.create();
                 g2d.translate(cx + offsetX, cy + offsetY);
                 g2d.drawImage(revolverImage, -revW/2, -revH/2, revW, revH, null);
                 g2d.dispose();
             }

        } else if (rouletteState == RouletteState.FIRING) {
             // Mantém a imagem do revólver centralizada como base (sem shake agora, ou com muito shake se for tiro)
             if (revolverImage != null) {
                 int revW = 300;
                 int revH = (int)((double)revW / revolverImage.getWidth(null) * revolverImage.getHeight(null));
                 g.drawImage(revolverImage, cx - revW/2, cy - revH/2, revW, revH, null);
             }

             if (roundDied) {
                 // --- Animação de Tiro (BANG) ---
                 // Clarão laranja/amarelo piscando
                 if (firingFrame % 4 < 2) { // Pisca rápido
                     g.setColor(new Color(255, 200, 50, 180));
                     g.fillOval(cx - 100, cy - 100, 200, 200);
                     g.setColor(new Color(255, 255, 200, 200));
                     g.fillOval(cx - 60, cy - 60, 120, 120);
                 }
                 
                 // Texto Gigante
                 g.setFont(new Font("Impact", Font.BOLD, 120));
                 g.setColor(new Color(255, 50, 50));
                 drawCenteredText(g, "BANG!", cy + 20);
                 
                 // Overlay vermelho piscando na tela toda
                 g.setColor(new Color(255, 0, 0, 60 + (firingFrame % 5) * 20));
                 g.fillRect(0, 0, getWidth(), getHeight());
                 
             } else {
                 // --- Animação de Falha (CLICK) ---
                 // Pequeno balão ou texto saindo da arma
                 g.setFont(new Font("Courier New", Font.BOLD, 40));
                 g.setColor(new Color(200, 200, 200));
                 
                 int yOffset = -(firingFrame * 2); // Texto sobe um pouco
                 drawCenteredText(g, "* click *", cy - 100 + yOffset);
                 
                 // Talvez um pequeno "puff" cinza
                 g.setColor(new Color(100, 100, 100, 100 - firingFrame * 5));
                 g.fillOval(cx + 80, cy - 80, 20 + firingFrame, 20 + firingFrame);
             }

        } else if (rouletteState == RouletteState.RESULT) {
             // Caixa de Resultado
            Color bg = rouletteSuccess ? new Color(20, 50, 20) : new Color(50, 20, 20);
            Color border = rouletteSuccess ? new Color(50, 200, 50) : new Color(200, 50, 50);
            
            g.setColor(bg);
            g.fillRoundRect(cx - 250, cy - 150, 500, 300, 20, 20);
            g.setColor(border);
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(cx - 250, cy - 150, 500, 300, 20, 20);
            
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(rouletteSuccess ? new Color(100, 255, 100) : new Color(255, 50, 50));
            drawCenteredText(g, rouletteSuccess ? "SOBREVIVEU!" : "GAME OVER", cy - 80);
            
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.setColor(Color.WHITE);
            drawCenteredText(g, rouletteResultText, cy);
            
            // Removemos o dado do resultado conforme solicitado
            // int diceSize = 64;
            // diceRenderer.drawFace(g, diceAnimationResult, cx - diceSize/2, cy - 160, diceSize, diceSize);

            rouletteContinueButton.setBounds(cx - 100, cy + 80, 200, 50);
            
            g.translate(rouletteContinueButton.getX(), rouletteContinueButton.getY());
            rouletteContinueButton.paint(g);
            g.translate(-rouletteContinueButton.getX(), -rouletteContinueButton.getY());
            
            if (rouletteContinueButton.getParent() != this) {
               add(rouletteContinueButton);
               revalidate();
            }
        }
    }
    
    private void drawCenteredText(Graphics2D g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
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
        int height = 185;
        
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
        drawStatItem(g, "Mãos:", (gameState.getMaxHands() - gameState.getHandsPlayed()) + "/" + gameState.getMaxHands(), x + 15, startY + gap * 2);
        drawStatItem(g, "Descartes:", String.valueOf(gameState.getDiscards()), x + 15, startY + gap * 3);
        
        // Multiplicador com destaque
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(new Color(200, 200, 200));
        g.drawString("Multi:", x + 15, startY + gap * 4 + 2);
        
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(255, 223, 0));
        g.drawString(formatValue(gameState.getMultiplier()) + "x", x + 110, startY + gap * 4 + 2);
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
        g.drawString("Mão mínima:", x + 180, y + 25);
        
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
        
        g.drawString("D x M: $" + formatValue(currentProjected), x + 180, y + 70);
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
            boolean isHovered = (rouletteState == RouletteState.NONE) && cardArea.contains(mousePos);
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
