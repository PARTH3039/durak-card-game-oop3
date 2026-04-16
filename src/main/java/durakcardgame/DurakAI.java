package durakcardgame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class DurakAI {
    private final Random rng = new Random();
    private final String difficulty;

    public DurakAI() {
        this("easy");
    }

    public DurakAI(String difficulty) {
        this.difficulty = difficulty == null ? "easy" : difficulty.toLowerCase();
    }

    public int sampleAction(int[] validActions) {
        return validActions[rng.nextInt(0, validActions.length)];
    }

    public int chooseAction(DurakGame game, int[] validActions) {
        if (validActions == null || validActions.length == 0) {
            return -1;
        }

        return switch (difficulty) {
            case "medium" -> chooseMedium(game, validActions);
            case "hard" -> chooseHard(game, validActions);
            default -> sampleAction(validActions);
        };
    }

    private int chooseMedium(DurakGame game, int[] validActions) {
        List<Integer> playable = getPlayableCardIndices(validActions);
        if (playable.isEmpty()) {
            return -1;
        }

        return playable.stream()
                .min(Comparator.comparingInt(i -> game.getPlayerHand(game.getActivePlayer()).get(i).rank()))
                .orElse(playable.getFirst());
    }

    private int chooseHard(DurakGame game, int[] validActions) {
        List<Integer> playable = getPlayableCardIndices(validActions);
        if (playable.isEmpty()) {
            return -1;
        }

        Card trump = game.getTrumpCard();
        List<Card> pile = game.getPile();

        if (game.getState() == DurakState.DEFEND && !pile.isEmpty()) {
            Card attackCard = pile.getLast();

            return playable.stream()
                    .min((a, b) -> {
                        Card ca = game.getPlayerHand(game.getActivePlayer()).get(a);
                        Card cb = game.getPlayerHand(game.getActivePlayer()).get(b);

                        boolean aTrump = ca.suite() == trump.suite();
                        boolean bTrump = cb.suite() == trump.suite();

                        if (aTrump != bTrump) {
                            return aTrump ? 1 : -1;
                        }
                        return Integer.compare(ca.rank(), cb.rank());
                    })
                    .orElse(playable.getFirst());
        }

        return playable.stream()
                .min((a, b) -> {
                    Card ca = game.getPlayerHand(game.getActivePlayer()).get(a);
                    Card cb = game.getPlayerHand(game.getActivePlayer()).get(b);

                    boolean aTrump = ca.suite() == trump.suite();
                    boolean bTrump = cb.suite() == trump.suite();

                    if (aTrump != bTrump) {
                        return aTrump ? 1 : -1;
                    }
                    return Integer.compare(ca.rank(), cb.rank());
                })
                .orElse(playable.getFirst());
    }

    private List<Integer> getPlayableCardIndices(int[] validActions) {
        List<Integer> result = new ArrayList<>();
        for (int action : validActions) {
            if (action != -1) {
                result.add(action);
            }
        }
        return result;
    }
}