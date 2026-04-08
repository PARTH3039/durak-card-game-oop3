package durakcardgame;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DurakGame {
    private final List<Card> deck = new ArrayList<>();
    private final List<Card>[] hands;
    private final List<Card> pile = new ArrayList<>();
    private Card trumpCard;
    private DurakState state;
    private int attacker, defender;
    private int attackerDiscardCountdown;
    private final Set<Integer> attackerPassed = new HashSet<>();

    public static final int NUM_PLAYERS = 2;

    private static final int NUM_ATTACKS = 6;

    public DurakGame() {
        hands = new ArrayList[NUM_PLAYERS];
        reset();
    }

    public void reset() {
        deck.clear();
        pile.clear();
        attackerPassed.clear();

        for (int i = 0; i < hands.length; i++) {
            hands[i] = new ArrayList<>();
        }

        for (int suite = 0; suite < 4; suite++) {
            for (int rank = 6; rank < 15; rank++) {
                deck.add(new Card(rank, suite));
            }
        }

        Collections.shuffle(deck);

        for (List<Card> hand : hands) {
            for (int i = 0; i < 6; i++) {
                hand.add(deck.removeLast());
            }
        }

        trumpCard = deck.removeFirst();
        attacker = findLowestTrumpHolder();
        state = DurakState.ATTACK;
        attackerDiscardCountdown = 0;
        defender = (attacker + 1) % hands.length;
    }

    private int findLowestTrumpHolder() {
        int result = 0;
        Card resultCard = null;
        for (int player = 0; player < hands.length; player++) {
            List<Card> hand = hands[player];
            Card card = hand.stream().filter(c -> c.suite() == trumpCard.suite()).min(Comparator.comparingInt(Card::rank)).orElse(null);
            if (resultCard == null || (card != null && resultCard.rank() > card.rank())) {
                result = player;
                resultCard = card;
            }
        }
        return result;
    }

    public void step(int cardIndex) {
        switch (state) {
            case ATTACK -> {
                if (cardIndex == -1) {
                    do {
                        attacker = (attacker + 1) % hands.length;
                    } while (attacker != defender);
                    attackerPassed.add(attacker);

                    if (attackerPassed.size() == hands.length - 1) {
                        attacker = defender;
                        defender = (attacker + 1) % hands.length;
                        pile.clear();
                        refillHands();
                    }
                } else {
                    Card card = hands[attacker].remove(cardIndex);

                    assert pile.isEmpty() || pile.stream().anyMatch(c -> c.rank() == card.rank());
                    pile.add(card);
                    state = DurakState.DEFEND;
                    attackerPassed.remove(attacker);
                }
            }
            case DEFEND -> {
                if (cardIndex == -1) {
                    hands[defender].addAll(pile);
                    state = DurakState.DISCARD;
                    attackerDiscardCountdown = hands.length - 1;
                } else {
                    Card defendCard = hands[defender].remove(cardIndex);
                    Card attackCard = pile.getLast();
                    assert defendCard.suite() == trumpCard.suite() ||
                            defendCard.suite() == attackCard.suite() && defendCard.rank() > attackCard.rank();

                    pile.add(defendCard);

                    do {
                        attacker = (attacker + 1) % hands.length;
                    } while (attacker == defender);

                    if (pile.size() / 2 == NUM_ATTACKS) {
                        attacker = defender;
                        defender = (attacker + 1) % hands.length;
                        pile.clear();
                        refillHands();
                    }
                    state = DurakState.ATTACK;
                }
            }
            case DISCARD -> {
                if (cardIndex != -1) {
                    Card card = hands[attacker].remove(cardIndex);
                    assert pile.stream().anyMatch(c -> c.rank() == card.rank());
                    hands[defender].add(card);
                }
                do {
                    attacker = (attacker + 1) % hands.length;
                } while (attacker == defender);
                if (--attackerDiscardCountdown == 0) {
                    attacker = (defender + 1) % hands.length;
                    pile.clear();
                    refillHands();
                    state = DurakState.ATTACK;
                }
            }
        }
    }

    public int getDeckSize() {
        return deck.size();
    }

    public Card getTrumpCard() {
        return trumpCard;
    }

    public int[] getActivePlayerValidCardIndices() {
        return switch (state) {
            case ATTACK, DISCARD -> {
                List<Card> hand = hands[attacker];
                Set<Integer> pileRankSet = pile.stream().map(Card::rank).collect(Collectors.toSet());
                yield IntStream.range(-1, hand.size()).filter(i -> pile.isEmpty() || i == -1 || pileRankSet.contains(hand.get(i).rank())).toArray();
            }
            case DEFEND -> {
                List<Card> hand = hands[defender];
                Card attackCard = pile.getLast();
                yield IntStream.range(-1, hand.size()).filter(i -> {
                    if (i == -1) {
                        return true;
                    }
                    Card defendCard = hand.get(i);
                    return defendCard.suite() == trumpCard.suite() ||
                            defendCard.suite() == attackCard.suite() && defendCard.rank() > attackCard.rank();
                }).toArray();
            }
        };
    }

    private void refillHands() {
        for (List<Card> hand : hands) {
            while (hand.size() < 6 && !deck.isEmpty()) {
                hand.add(deck.removeLast());
            }
        }
    }

    public List<Card> getPile() {
        return Collections.unmodifiableList(pile);
    }

    public DurakState getState() {
        return state;
    }

    public int getActivePlayer() {
        return state == DurakState.DEFEND ? defender : attacker;
    }

    public List<Card> getPlayerHand(int player) {
        return Collections.unmodifiableList(hands[player]);
    }

    public int getWinner() {
        for (int i = 0; i < hands.length; i++) {
            if (hands[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
