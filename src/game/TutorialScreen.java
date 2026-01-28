package game;

import core.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tela de Tutorial
 */
public class TutorialScreen extends Screen {
    private Image backgroundImage;
    private int currentSection = 0; // 0: Menu, 1: Lógica, 2: Mãos
    private CardRenderer cardRenderer;
    
    // Conteúdo de rolagem para mãos de poker
    private JScrollPane scrollPane;
    private JPanel handsContentPanel;
    
    public TutorialScreen(ScreenManager screenManager) {
        super(screenManager);
        this.cardRenderer = CardRenderer.getInstance();
        loadBackground();
    }
    
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
        setLayout(null);
        currentSection = 0;
        showMenu();
        revalidate();
        repaint();
    }
    
    private void showMenu() {
        removeAll();
        
        JLabel titleLabel = new JLabel("TUTORIAL", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        titleLabel.setForeground(new Color(255, 223, 0));
        titleLabel.setBounds(200, 50, 600, 60);
        add(titleLabel);
        
        JButton logicButton = createStyledButton("COMO JOGAR", 350, 200);
        logicButton.addActionListener(e -> showLogic());
        add(logicButton);
        
        JButton handsButton = createStyledButton("MÃOS DE POKER", 350, 300);
        handsButton.addActionListener(e -> showHandsList());
        add(handsButton);
        
        JButton backButton = createStyledButton("VOLTAR", 350, 500);
        backButton.addActionListener(e -> screenManager.changeScreen("menu"));
        add(backButton);
        
        currentSection = 0;
        revalidate();
        repaint();
    }
    
    private void showLogic() {
        removeAll();
        currentSection = 1;
        
        JLabel titleLabel = new JLabel("COMO JOGAR", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(new Color(255, 223, 0));
        titleLabel.setBounds(200, 30, 600, 50);
        add(titleLabel);
        
        JTextArea textArea = new JTextArea();
        textArea.setText(
            "OBJETIVO:\n" +
            "Vença as 'Blinds' acumulando dinheiro suficiente para atingir a meta.\n\n" +
            
            "COMO JOGAR:\n" +
            "1. Receba 8 cartas do baralho.\n" +
            "2. Selecione até 5 cartas para formar uma mão de poker.\n" +
            "3. O jogo calculará o valor da sua mão e aplicará ao seu multiplicador.\n" +
            "4. Se tiver cartas ruins, use a opção DESCARTAR para trocar cartas e encher sua mão novamente.\n\n" +
            
            "REGRAS DA BLIND:\n" +
            "- Meta de Dinheiro: Valor que você precisa atingir para passar de fase.\n" +
            "- Mão Alvo: A Blind exige uma mão mínima (Ex: Dois Pares, Trinca).\n" +
            "- Mãos por Blind: Você tem apenas 3 JOGADAS para bater a meta.\n\n" +
            
            "PONTUAÇÃO vs PENALIDADE:\n" +
            "- SUCESSO: Se sua mão for igual ou superior à Mão Alvo, você ganha pontos no multiplicador.\n" +
            "- FALHA: Se jogar uma mão inferior à Mão Alvo, você sofre uma PENALIDADE e perde multiplicador.\n\n" +
            
            "GAME OVER:\n" +
            "- Se não atingir a meta após 3 mãos.\n" +
            "- Se o baralho acabar (Deck Vazio)."
        );
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));
        textArea.setForeground(Color.WHITE);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBounds(150, 100, 700, 450);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        add(scroll);
        
        JButton backButton = createStyledButton("VOLTAR", 350, 600);
        backButton.addActionListener(e -> showMenu());
        add(backButton);
        
        revalidate();
        repaint();
    }
    
    private void showHandsList() {
        removeAll();
        currentSection = 2;
        
        JLabel titleLabel = new JLabel("MÃOS DE POKER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(new Color(255, 223, 0));
        titleLabel.setBounds(200, 20, 600, 50);
        add(titleLabel);
        
        handsContentPanel = new JPanel();
        handsContentPanel.setLayout(new BoxLayout(handsContentPanel, BoxLayout.Y_AXIS));
        handsContentPanel.setOpaque(false);
        
        // Adiciona cada mão
        addHandExample(PokerHand.ROYAL_FLUSH, new Rank[]{Rank.ACE, Rank.KING, Rank.QUEEN, Rank.JACK, Rank.TEN}, Suit.HEARTS);
        addHandExample(PokerHand.STRAIGHT_FLUSH, new Rank[]{Rank.NINE, Rank.EIGHT, Rank.SEVEN, Rank.SIX, Rank.FIVE}, Suit.SPADES);
        addHandExample(PokerHand.FOUR_OF_KIND, new Rank[]{Rank.ACE, Rank.ACE, Rank.ACE, Rank.ACE, Rank.KING}, Suit.DIAMONDS);
        addHandExample(PokerHand.FULL_HOUSE, new Rank[]{Rank.KING, Rank.KING, Rank.KING, Rank.NINE, Rank.NINE}, Suit.CLUBS);
        addHandExample(PokerHand.FLUSH, new Rank[]{Rank.ACE, Rank.JACK, Rank.EIGHT, Rank.SIX, Rank.TWO}, Suit.HEARTS);
        addHandExample(PokerHand.STRAIGHT, new Rank[]{Rank.TEN, Rank.NINE, Rank.EIGHT, Rank.SEVEN, Rank.SIX}, null);
        addHandExample(PokerHand.THREE_OF_KIND, new Rank[]{Rank.QUEEN, Rank.QUEEN, Rank.QUEEN, Rank.SEVEN, Rank.TWO}, null);
        addHandExample(PokerHand.TWO_PAIR, new Rank[]{Rank.JACK, Rank.JACK, Rank.TEN, Rank.TEN, Rank.ACE}, null);
        addHandExample(PokerHand.PAIR, new Rank[]{Rank.ACE, Rank.ACE, Rank.KING, Rank.QUEEN, Rank.JACK}, null);
        addHandExample(PokerHand.HIGH_CARD, new Rank[]{Rank.ACE, Rank.JACK, Rank.NINE, Rank.FIVE, Rank.TWO}, null);
        
        scrollPane = new JScrollPane(handsContentPanel);
        scrollPane.setBounds(100, 80, 800, 500);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
        
        JButton backButton = createStyledButton("VOLTAR", 350, 600);
        backButton.addActionListener(e -> showMenu());
        add(backButton);
        
        revalidate();
        repaint();
    }
    
    private void addHandExample(PokerHand handType, Rank[] ranks, Suit suit) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(750, 160));
        panel.setMaximumSize(new Dimension(750, 160));
        
        // Título e Descrição
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        
        JLabel nameLabel = new JLabel(handType.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 22));
        nameLabel.setForeground(new Color(255, 223, 0));
        
        JLabel scoreLabel = new JLabel("Mult: " + handType.getMultiplier() + "x");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        scoreLabel.setForeground(Color.WHITE);
        
        infoPanel.add(nameLabel);
        infoPanel.add(scoreLabel);
        
        // Cartas
        JPanel cardsPanel = new JPanel() {
             @Override
             protected void paintComponent(Graphics g) {
                 super.paintComponent(g);
                 int x = 0;
                 int width = 70;  // Carta menor para caber
                 int height = 96;
                 
                 for (int i = 0; i < ranks.length; i++) {
                     PlayingCard card;
                     if (suit != null) {
                        card = new PlayingCard(ranks[i], suit);
                     } else {
                        // Varia os naipes se não for flush
                        Suit currentSuit = Suit.values()[i % 4];
                        card = new PlayingCard(ranks[i], currentSuit);
                     }
                     cardRenderer.drawCard((Graphics2D)g, card, x + (i * (width + 5)), 20, width, height);
                 }
             }
        };
        cardsPanel.setPreferredSize(new Dimension(400, 140));
        cardsPanel.setOpaque(false);
        
        panel.add(infoPanel, BorderLayout.WEST);
        panel.add(cardsPanel, BorderLayout.CENTER);
        
        // Separador
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 50));
        
        handsContentPanel.add(panel);
        handsContentPanel.add(sep);
    }
    
    private JButton createStyledButton(String text, int x, int y) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Desenha o fundo estilo MenuScreen
                if (getModel().isRollover()) {
                    g2.setColor(new Color(90, 140, 190, 230));
                } else {
                    g2.setColor(new Color(50, 90, 140, 210));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Borda
                g2.setStroke(new BasicStroke(2));
                g2.setColor(new Color(255, 223, 0));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setBounds(x, y, 300, 50);
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
        if (backgroundImage != null) {
            // Desenha com um overlay escuro para melhorar legibilidade
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
             g.setColor(new Color(20, 25, 35));
             g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
    @Override
    public void dispose() {
    }
}
