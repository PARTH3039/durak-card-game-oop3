package model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    protected List<Card> hand;   // cards the player holds
    protected String name;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    // Add a card to player's hand
    public void addCard(Card card) {
        hand.add(card);
    }

    // Remove a card from hand
    public void removeCard(Card card) {
        hand.remove(card);
    }

    // Get all cards
    public List<Card> getHand() {
        return hand;
    }

    public String getName() {
        return name;
    }

    // Show player's cards (for testing / console)
    public void showHand() {
        System.out.println(name + "'s Cards:");
        for (Card c : hand) {
            System.out.println(c);
        }
    }

    // Basic attack (for human, usually selected manually in UI)
    public Card playAttackCard() {
        if (hand.isEmpty()) return null;

        // just return first card (UI will replace this later)
        return hand.get(0);
    }

    // Basic defense (for human, UI will override this)
    public Card playDefenseCard(Card attackCard) {
        for (Card c : hand) {
            // simple rule: same suit and higher value
            if (c.getSuit().equals(attackCard.getSuit()) &&
                c.getValue() > attackCard.getValue()) {
                return c;
            }
        }
        return null; // cannot defend
    }
}