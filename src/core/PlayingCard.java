package core;

/**
 * Representa uma carta de baralho de pôquer
 */
public class PlayingCard extends Card {
    private Rank rank;
    private Suit suit;
    
    public PlayingCard(Rank rank, Suit suit) {
        super(rank.getSymbol(), suit.name());
        this.rank = rank;
        this.suit = suit;
        this.value = rank.getValue();
        this.description = rank.getSymbol() + suit.getSymbol();
    }
    
    @Override
    public void applyEffect(GameState gameState) {
        // Cartas normais não têm efeitos especiais por enquanto
    }
    
    @Override
    public int calculateValue() {
        return rank.getValue();
    }
    
    public Rank getRankEnum() {
        return rank;
    }
    
    public Suit getSuitEnum() {
        return suit;
    }
    
    @Override
    public String toString() {
        return rank.getSymbol() + suit.getSymbol();
    }
}
