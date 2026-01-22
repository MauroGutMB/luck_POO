package core;

/**
 * Enumerador para os tipos de mãos de pôquer e seus multiplicadores
 */
public enum PokerHand {
    HIGH_CARD("High Card", 0.1),
    PAIR("Par", 0.4),
    TWO_PAIR("Dois Pares", 0.8),
    THREE_OF_KIND("Trinca", 1.5),
    STRAIGHT("Sequência", 3.0),
    FLUSH("Flush", 6.0),
    FULL_HOUSE("Full House", 10.0),
    FOUR_OF_KIND("Quadra", 25.0),
    STRAIGHT_FLUSH("Straight Flush", 50.0),
    ROYAL_FLUSH("Royal Flush", 100.0);
    
    private final String name;
    private final double multiplier;
    
    PokerHand(String name, double multiplier) {
        this.name = name;
        this.multiplier = multiplier;
    }
    
    public String getName() {
        return name;
    }
    
    public double getMultiplier() {
        return multiplier;
    }
}
