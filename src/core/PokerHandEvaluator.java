package core;

import java.util.*;

/**
 * Avalia mãos de pôquer e retorna o tipo de mão
 */
public class PokerHandEvaluator {
    
    /**
     * Avalia uma lista de cartas e retorna o melhor tipo de mão de pôquer
     */
    public static PokerHand evaluateHand(List<PlayingCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return PokerHand.HIGH_CARD;
        }
        int cardCount = cards.size();
        
        // Ordena as cartas por valor
        List<PlayingCard> sortedCards = new ArrayList<>(cards);
        sortedCards.sort((a, b) -> Integer.compare(b.getRankEnum().getValue(), a.getRankEnum().getValue()));
        
        boolean isFlush = cardCount == 5 && checkFlush(sortedCards);
        boolean isStraight = cardCount == 5 && checkStraight(sortedCards);
        
        // Royal Flush
        if (isFlush && isStraight && isRoyalSequence(sortedCards)) {
            return PokerHand.ROYAL_FLUSH;
        }
        
        // Straight Flush
        if (isFlush && isStraight) {
            return PokerHand.STRAIGHT_FLUSH;
        }
        
        // Conta as ocorrências de cada rank
        Map<Rank, Integer> rankCounts = new HashMap<>();
        for (PlayingCard card : sortedCards) {
            rankCounts.put(card.getRankEnum(), rankCounts.getOrDefault(card.getRankEnum(), 0) + 1);
        }
        
        List<Integer> counts = new ArrayList<>(rankCounts.values());
        counts.sort(Collections.reverseOrder());
        
        // Four of a Kind
        if (counts.get(0) == 4) {
            return PokerHand.FOUR_OF_KIND;
        }
        
        // Full House
        if (cardCount == 5 && counts.size() >= 2 && counts.get(0) == 3 && counts.get(1) == 2) {
            return PokerHand.FULL_HOUSE;
        }
        
        // Flush
        if (isFlush) {
            return PokerHand.FLUSH;
        }
        
        // Straight
        if (isStraight) {
            return PokerHand.STRAIGHT;
        }
        
        // Three of a Kind
        if (counts.get(0) == 3) {
            return PokerHand.THREE_OF_KIND;
        }
        
        // Two Pair
        if (counts.size() >= 2 && counts.get(0) == 2 && counts.get(1) == 2) {
            return PokerHand.TWO_PAIR;
        }
        
        // Pair
        if (counts.get(0) == 2) {
            return PokerHand.PAIR;
        }
        
        // High Card
        return PokerHand.HIGH_CARD;
    }
    
    /**
     * Verifica se todas as cartas são do mesmo naipe
     */
    private static boolean checkFlush(List<PlayingCard> cards) {
        Suit firstSuit = cards.get(0).getSuitEnum();
        for (PlayingCard card : cards) {
            if (card.getSuitEnum() != firstSuit) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Verifica se as cartas formam uma sequência
     */
    private static boolean checkStraight(List<PlayingCard> cards) {
        if (cards.size() != 5) {
            return false;
        }
        // Verifica sequência normal
        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i).getRankEnum().getValue() - cards.get(i + 1).getRankEnum().getValue() != 1) {
                // Verifica sequência especial A-2-3-4-5
                if (cards.get(0).getRankEnum() == Rank.ACE &&
                    cards.get(1).getRankEnum() == Rank.FIVE &&
                    cards.get(2).getRankEnum() == Rank.FOUR &&
                    cards.get(3).getRankEnum() == Rank.THREE &&
                    cards.get(4).getRankEnum() == Rank.TWO) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se a sequência é 10-J-Q-K-A (Royal)
     */
    private static boolean isRoyalSequence(List<PlayingCard> cards) {
        return cards.size() == 5 &&
               cards.get(0).getRankEnum() == Rank.ACE &&
               cards.get(1).getRankEnum() == Rank.KING &&
               cards.get(2).getRankEnum() == Rank.QUEEN &&
               cards.get(3).getRankEnum() == Rank.JACK &&
               cards.get(4).getRankEnum() == Rank.TEN;
    }
    
    /**
     * Encontra a melhor mão de 5 cartas dentro de uma lista maior
     */
    public static PokerHand evaluateBestHand(List<PlayingCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return PokerHand.HIGH_CARD;
        }
        
        if (cards.size() <= 5) {
            return evaluateHand(cards);
        }
        
        // Gera todas as combinações de 5 cartas e encontra a melhor
        PokerHand bestHand = PokerHand.HIGH_CARD;
        List<List<PlayingCard>> combinations = generateCombinations(cards, 5);
        
        for (List<PlayingCard> combo : combinations) {
            PokerHand hand = evaluateHand(combo);
            if (hand.getMultiplier() > bestHand.getMultiplier()) {
                bestHand = hand;
            }
        }
        
        return bestHand;
    }
    
    /**
     * Gera todas as combinações de tamanho k a partir de uma lista
     */
    private static List<List<PlayingCard>> generateCombinations(List<PlayingCard> cards, int k) {
        List<List<PlayingCard>> combinations = new ArrayList<>();
        generateCombinationsHelper(cards, k, 0, new ArrayList<>(), combinations);
        return combinations;
    }
    
    private static void generateCombinationsHelper(List<PlayingCard> cards, int k, int start, 
                                                   List<PlayingCard> current, List<List<PlayingCard>> combinations) {
        if (current.size() == k) {
            combinations.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            generateCombinationsHelper(cards, k, i + 1, current, combinations);
            current.remove(current.size() - 1);
        }
    }
}
