package core;

import java.util.ArrayList;
import java.util.List;

/**
 * Armazena o estado atual do jogo
 */
public class GameState {
    private int score;
    private int currentRound;
    private int currentBlind; // 1, 2 ou 3
    private double multiplier;
    private int money;
    private int discards;
    private Deck gameDeck;
    private List<PlayingCard> playerHand;
    private List<PlayingCard> selectedCards;
    private PokerHand requiredHand; // Mão mínima necessária
    
    public GameState() {
        this.score = 0;
        this.currentRound = 1;
        this.currentBlind = 1;
        this.multiplier = 1.0;
        this.money = 10; // Dinheiro inicial
        this.discards = 5;
        this.gameDeck = new Deck();
        this.playerHand = new ArrayList<>();
        this.selectedCards = new ArrayList<>();
        this.requiredHand = PokerHand.PAIR;
    }
    
    public void reset() {
        score = 0;
        currentRound = 1;
        currentBlind = 1;
        multiplier = 1.0;
        money = 10;
        discards = 5;
        gameDeck.reset();
        playerHand.clear();
        selectedCards.clear();
        requiredHand = PokerHand.PAIR;
    }
    
    public void startNewRound() {
        gameDeck.reset();
        currentBlind = 1;
        multiplier = 1.0;
        discards = 5;
        playerHand.clear();
        selectedCards.clear();
        updateRequiredHand();
    }
    
    public void nextBlind() {
        currentBlind++;
        playerHand.clear();
        selectedCards.clear();
    }
    
    private void updateRequiredHand() {
        // Define mão necessária baseada na rodada
        switch (currentRound) {
            case 1: requiredHand = PokerHand.PAIR; break;
            case 2: requiredHand = PokerHand.PAIR; break;
            case 3: requiredHand = PokerHand.TWO_PAIR; break;
            case 4: requiredHand = PokerHand.THREE_OF_KIND; break;
            case 5: requiredHand = PokerHand.FLUSH; break;
            case 6: requiredHand = PokerHand.FULL_HOUSE; break;
            default: requiredHand = PokerHand.FULL_HOUSE; break;
        }
    }
    
    // Getters e Setters
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public void addScore(int points) { this.score += points; }
    
    public int getCurrentRound() { return currentRound; }
    public void setCurrentRound(int round) { 
        this.currentRound = round;
        updateRequiredHand();
    }
    public void nextRound() { 
        this.currentRound++;
        updateRequiredHand();
    }
    
    public int getCurrentBlind() { return currentBlind; }
    public void setCurrentBlind(int blind) { this.currentBlind = blind; }
    
    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
    public void addMultiplier(double amount) { this.multiplier += amount; }
    
    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }
    public void addMoney(int amount) { this.money += amount; }
    
    public int getDiscards() { return discards; }
    public void setDiscards(int discards) { this.discards = discards; }
    public void decrementDiscards() { this.discards--; }
    
    public Deck getGameDeck() { return gameDeck; }
    public List<PlayingCard> getPlayerHand() { return playerHand; }
    public List<PlayingCard> getSelectedCards() { return selectedCards; }
    
    public PokerHand getRequiredHand() { return requiredHand; }
    public void setRequiredHand(PokerHand hand) { this.requiredHand = hand; }
}
