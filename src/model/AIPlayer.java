package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer extends Player {

    private String difficulty;
    private Random random;

    public AIPlayer(String name, String difficulty) {
        super(name);
        this.difficulty = difficulty.toLowerCase();
        this.random = new Random();
    }

    // ---------------- ATTACK ----------------
    @Override
    public Card playAttackCard() {
        if (hand.isEmpty()) return null;

        switch (difficulty) {
            case "easy":
                return easyAttack();

            case "medium":
                return mediumAttack();

            case "hard":
                return hardAttack();

            default:
                return mediumAttack();
        }
    }

    // ---------------- DEFENSE ----------------
    @Override
    public Card playDefenseCard(Card attackCard) {
        switch (difficulty) {
            case "easy":
                return easyDefense(attackCard);

            case "medium":
                return mediumDefense(attackCard);

            case "hard":
                return hardDefense(attackCard);

            default:
                return mediumDefense(attackCard);
        }
    }

    // ---------------- EASY ----------------
    // Random attack
    private Card easyAttack() {
        return hand.get(random.nextInt(hand.size()));
    }

    // First valid defense
    private Card easyDefense(Card attackCard) {
        List<Card> valid = getValidDefenseCards(attackCard);
        if (valid.isEmpty()) return null;
        return valid.get(0);
    }

    // ---------------- MEDIUM ----------------
    // Lowest card attack
    private Card mediumAttack() {
        Card lowest = hand.get(0);

        for (Card c : hand) {
            if (c.getValue() < lowest.getValue()) {
                lowest = c;
            }
        }
        return lowest;
    }

    // Smallest valid defense
    private Card mediumDefense(Card attackCard) {
        List<Card> valid = getValidDefenseCards(attackCard);
        if (valid.isEmpty()) return null;

        Card best = valid.get(0);

        for (Card c : valid) {
            if (c.getValue() < best.getValue()) {
                best = c;
            }
        }
        return best;
    }

    // ---------------- HARD ----------------
    // Highest card attack
    private Card hardAttack() {
        Card highest = hand.get(0);

        for (Card c : hand) {
            if (c.getValue() > highest.getValue()) {
                highest = c;
            }
        }
        return highest;
    }

    // Same as medium (keeps strong cards smartly)
    private Card hardDefense(Card attackCard) {
        return mediumDefense(attackCard);
    }

    // ---------------- HELPER ----------------
    // Get all valid defense cards
    private List<Card> getValidDefenseCards(Card attackCard) {
        List<Card> valid = new ArrayList<>();

        for (Card c : hand) {
            if (c.getSuit().equals(attackCard.getSuit()) &&
                c.getValue() > attackCard.getValue()) {
                valid.add(c);
            }
        }

        return valid;
    }

    // Decide if AI should take card
    public boolean shouldTakeCard(Card attackCard) {
        return playDefenseCard(attackCard) == null;
    }
}