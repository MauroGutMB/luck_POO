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
    private int handsPlayed;
    private final int MAX_HANDS = 3;
    private Deck gameDeck;
    private List<PlayingCard> playerHand;
    private List<PlayingCard> selectedCards;
    private PokerHand requiredHand; // Mão mínima sugerida
    private int targetMoney; // Dinheiro necessário para avançar
    
    public GameState() {
        this.score = 0;
        this.currentRound = 1;
        this.currentBlind = 1;
        this.multiplier = 1.0;
        this.money = 10; // Dinheiro inicial
        this.discards = 5;
        this.handsPlayed = 0;
        this.gameDeck = new Deck();
        this.playerHand = new ArrayList<>();
        this.selectedCards = new ArrayList<>();
        this.requiredHand = PokerHand.PAIR;
        this.targetMoney = 20;
    }
    
    public void reset() {
        score = 0;
        currentRound = 1;
        currentBlind = 1;
        multiplier = 1.0;
        money = 10;
        discards = 5;
        handsPlayed = 0;
        gameDeck.reset();
        playerHand.clear();
        selectedCards.clear();
        requiredHand = PokerHand.PAIR;
        targetMoney = 20;
    }
    
    public void startNewRound() {
        gameDeck.reset();
        currentBlind = 1;
        multiplier = 1.0;
        discards = 5;
        handsPlayed = 0;
        playerHand.clear();
        selectedCards.clear();
        updateRoundGoals();
    }
    
    public void nextBlind() {
        currentBlind++;
        handsPlayed = 0;
        playerHand.clear();
        selectedCards.clear();
    }
    
    private void updateRoundGoals() {
        // Define mão necessária baseada na rodada e meta de dinheiro
        switch (currentRound) {
            case 1: 
                requiredHand = PokerHand.PAIR;
                targetMoney = 20;
                break;
            case 2:
                requiredHand = PokerHand.TWO_PAIR; 
                targetMoney = 50;
                break;
            case 3: 
                requiredHand = PokerHand.THREE_OF_KIND; 
                targetMoney = 150;
                break;
            case 4: 
                requiredHand = PokerHand.FLUSH; 
                targetMoney = 500;
                break;
            case 5: 
                requiredHand = PokerHand.FULL_HOUSE; 
                targetMoney = 2000;
                break;
            case 6: 
                requiredHand = PokerHand.FOUR_OF_KIND; 
                targetMoney = 10000;
                break;
            default: 
                requiredHand = PokerHand.ROYAL_FLUSH; 
                targetMoney = (int)(targetMoney * 2.5);
                break;
        }
    }
    
    // Getters e Setters
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public void addScore(int points) { this.score += points; }
    
    public int getCurrentRound() { return currentRound; }
    public void setCurrentRound(int round) { 
        this.currentRound = round;
        updateRoundGoals();
    }
    public void nextRound() { 
        this.currentRound++;
        updateRoundGoals();
    }
    
    public int getCurrentBlind() { return currentBlind; }
    public void setCurrentBlind(int blind) { this.currentBlind = blind; }
    
    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
    public void addMultiplier(double amount) { this.multiplier += amount; }
    
    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }
    public void addMoney(int amount) { this.money += amount; }
    
    public int getTargetMoney() { return targetMoney; }
    public void setTargetMoney(int targetMoney) { this.targetMoney = targetMoney; }
    
    public int getDiscards() { return discards; }
    public void setDiscards(int discards) { this.discards = discards; }
    public void decrementDiscards() { this.discards--; }

    public int getHandsPlayed() { return handsPlayed; }
    public void incrementHandsPlayed() { this.handsPlayed++; }
    public int getMaxHands() { return MAX_HANDS; }
    
    public Deck getGameDeck() { return gameDeck; }
    public List<PlayingCard> getPlayerHand() { return playerHand; }
    public List<PlayingCard> getSelectedCards() { return selectedCards; }
    
    public PokerHand getRequiredHand() { return requiredHand; }
    public void setRequiredHand(PokerHand hand) { this.requiredHand = hand; }
}
