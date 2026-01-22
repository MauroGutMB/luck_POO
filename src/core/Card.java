package core;

/**
 * Classe abstrata base para todas as cartas do jogo
 */
public abstract class Card {
    protected String name;
    protected String description;
    protected int value;
    protected String suit; // Naipe: Hearts, Diamonds, Clubs, Spades
    protected String rank; // Valor: A, 2-10, J, Q, K
    
    public Card(String rank, String suit) {
        this.rank = rank;
        this.suit = suit;
        this.name = rank + " de " + suit;
    }
    
    /**
     * Aplica o efeito da carta
     */
    public abstract void applyEffect(GameState gameState);
    
    /**
     * Calcula o valor base da carta
     */
    public abstract int calculateValue();
    
    /**
     * Retorna a descrição da carta
     */
    public String getDescription() {
        return description;
    }
    
    public String getName() {
        return name;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getSuit() {
        return suit;
    }
    
    public String getRank() {
        return rank;
    }
}
