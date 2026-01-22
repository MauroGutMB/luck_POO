package core;

/**
 * Enumerador para os naipes das cartas
 */
public enum Suit {
    HEARTS("Copas", "♥"),
    DIAMONDS("Ouros", "♦"),
    CLUBS("Paus", "♣"),
    SPADES("Espadas", "♠");
    
    private final String namePt;
    private final String symbol;
    
    Suit(String namePt, String symbol) {
        this.namePt = namePt;
        this.symbol = symbol;
    }
    
    public String getNamePt() {
        return namePt;
    }
    
    public String getSymbol() {
        return symbol;
    }
}
