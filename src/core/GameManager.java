package core;

import java.util.List;

/**
 * Gerencia a lógica principal do jogo
 */
public class GameManager {
    private GameState gameState;
    private static GameManager instance;
    
    private GameManager() {
        this.gameState = new GameState();
    }
    
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    /**
     * Inicia um novo jogo
     */
    public void startNewGame() {
        gameState.reset();
        gameState.startNewRound();
    }
    
    /**
     * Processa uma jogada
     */
    public void playHand(List<PlayingCard> selectedCards) {
        if (selectedCards.size() != 5) {
            System.err.println("Erro: Deve jogar exatamente 5 cartas");
            return;
        }
        
        PokerHand hand = PokerHandEvaluator.evaluateHand(selectedCards);
        gameState.addMultiplier(hand.getMultiplier());
        
        System.out.println("Mão jogada: " + hand.getName() + " (+" + hand.getMultiplier() + "x)");
    }
    
    /**
     * Descarta cartas selecionadas
     */
    public void discardCards(List<PlayingCard> cards) {
        if (gameState.getDiscards() <= 0) {
            System.err.println("Erro: Sem descartes restantes");
            return;
        }
        
        gameState.getPlayerHand().removeAll(cards);
        gameState.getGameDeck().discard(cards);
        
        List<PlayingCard> newCards = gameState.getGameDeck().draw(cards.size());
        gameState.getPlayerHand().addAll(newCards);
        gameState.decrementDiscards();
        
        System.out.println("Descartadas " + cards.size() + " cartas");
    }
    
    public GameState getGameState() {
        return gameState;
    }
}
