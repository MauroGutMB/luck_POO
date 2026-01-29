package core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Renderiza cartas usando o sprite sheet de cartas
 */
public class CardRenderer {
    private static CardRenderer instance;
    private BufferedImage cardSpriteSheet;
    private Map<String, BufferedImage> cardCache;
    
    // Dimensões das cartas no sprite sheet (medidas da imagem real)
    // A imagem tem 14 colunas e 5 linhas total
    // Cada carta tem 96x128 pixels (ajustado para evitar overlap)
    private static final int CARD_WIDTH = 96;
    private static final int CARD_HEIGHT = 128;
    private static final int COLS = 13; // 13 ranks (A até K)
    private static final int ROWS = 4;  // 4 suits (Hearts, Diamonds, Spades, Clubs)
    
    private CardRenderer() {
        cardCache = new HashMap<>();
        loadSpriteSheet();
    }
    
    public static CardRenderer getInstance() {
        if (instance == null) {
            instance = new CardRenderer();
        }
        return instance;
    }
    
    private void loadSpriteSheet() {
        try {
            cardSpriteSheet = ImageIO.read(getClass().getResourceAsStream("/assets/cartas.png"));
            System.out.println("Sprite sheet de cartas carregado!");
        } catch (IOException e) {
            System.err.println("Erro ao carregar sprite sheet de cartas: " + e.getMessage());
        }
    }
    
    /**
     * Obtém a imagem de uma carta específica
     */
    public BufferedImage getCardImage(PlayingCard card) {
        if (cardSpriteSheet == null) {
            return createPlaceholderCard(card);
        }
        
        String key = card.getRankEnum().name() + "_" + card.getSuitEnum().name();
        
        if (cardCache.containsKey(key)) {
            return cardCache.get(key);
        }
        
        // Calcula posição no sprite sheet
        int col = getRankColumn(card.getRankEnum());
        int row = getSuitRow(card.getSuitEnum());
        
        int x = col * CARD_WIDTH;
        int y = row * CARD_HEIGHT;
        
        try {
            BufferedImage cardImage = cardSpriteSheet.getSubimage(x, y, CARD_WIDTH, CARD_HEIGHT);
            cardCache.put(key, cardImage);
            return cardImage;
        } catch (Exception e) {
            System.err.println("Erro ao extrair carta: " + e.getMessage());
            return createPlaceholderCard(card);
        }
    }
    
    /**
     * Retorna a coluna no sprite sheet baseada no rank
     */
    private int getRankColumn(Rank rank) {
        switch (rank) {
            case ACE: return 0;
            case TWO: return 1;
            case THREE: return 2;
            case FOUR: return 3;
            case FIVE: return 4;
            case SIX: return 5;
            case SEVEN: return 6;
            case EIGHT: return 7;
            case NINE: return 8;
            case TEN: return 9;
            case JACK: return 10;
            case QUEEN: return 11;
            case KING: return 12;
            default: return 0;
        }
    }
    
    /**
     * Retorna a linha no sprite sheet baseada no suit
     */
    private int getSuitRow(Suit suit) {
        switch (suit) {
            case HEARTS: return 0;
            case DIAMONDS: return 1;
            case CLUBS: return 2;
            case SPADES: return 3;
            default: return 0;
        }
    }
    
    /**
     * Cria uma carta placeholder caso o sprite não carregue
     */
    private BufferedImage createPlaceholderCard(PlayingCard card) {
        BufferedImage placeholder = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, 8, 8);
        g.setColor(Color.BLACK);
        g.drawRoundRect(0, 0, CARD_WIDTH - 1, CARD_HEIGHT - 1, 8, 8);
        
        // Desenha símbolo da carta
        g.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g.getFontMetrics();
        String text = card.toString();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        g.drawString(text, (CARD_WIDTH - textWidth) / 2, (CARD_HEIGHT + textHeight) / 2);
        
        g.dispose();
        return placeholder;
    }
    
    /**
     * Desenha uma carta em uma posição específica
     */
    public void drawCard(Graphics2D g, PlayingCard card, int x, int y, int width, int height) {
        BufferedImage cardImage = getCardImage(card);
        g.drawImage(cardImage, x, y, width, height, null);
    }
    
    /**
     * Desenha o verso de uma carta
     */
    public void drawCardBack(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(new Color(180, 0, 0));
        g.fillRoundRect(x, y, width, height, 8, 8);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x + 2, y + 2, width - 4, height - 4, 8, 8);
        
        // Desenha padrão no verso
        g.setColor(new Color(220, 220, 220, 100));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                g.fillOval(x + 10 + i * 20, y + 10 + j * 20, 15, 15);
            }
        }
    }
    
    public int getCardWidth() {
        return CARD_WIDTH;
    }
    
    public int getCardHeight() {
        return CARD_HEIGHT;
    }
}
