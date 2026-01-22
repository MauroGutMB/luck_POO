package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gerencia o baralho de 52 cartas
 */
public class Deck {
    private List<PlayingCard> cards;
    private List<PlayingCard> discardPile;
    
    public Deck() {
        this.cards = new ArrayList<>();
        this.discardPile = new ArrayList<>();
        initializeDeck();
    }
    
    /**
     * Inicializa um baralho completo de 52 cartas
     */
    private void initializeDeck() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new PlayingCard(rank, suit));
            }
        }
    }
    
    /**
     * Embaralha o deck
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }
    
    /**
     * Compra uma carta do topo do deck
     */
    public PlayingCard draw() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(0);
    }
    
    /**
     * Compra múltiplas cartas
     */
    public List<PlayingCard> draw(int count) {
        List<PlayingCard> drawnCards = new ArrayList<>();
        for (int i = 0; i < count && !cards.isEmpty(); i++) {
            drawnCards.add(draw());
        }
        return drawnCards;
    }
    
    /**
     * Descarta uma carta
     */
    public void discard(PlayingCard card) {
        discardPile.add(card);
    }
    
    /**
     * Descarta múltiplas cartas
     */
    public void discard(List<PlayingCard> cards) {
        discardPile.addAll(cards);
    }
    
    /**
     * Retorna o número de cartas restantes
     */
    public int getRemainingCards() {
        return cards.size();
    }
    
    /**
     * Verifica se o deck está vazio
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
    
    /**
     * Reinicia o deck com 52 cartas novas
     */
    public void reset() {
        cards.clear();
        discardPile.clear();
        initializeDeck();
        shuffle();
    }
    
    public List<PlayingCard> getCards() {
        return new ArrayList<>(cards);
    }
}
