package durakcardgame;

public record Card(int rank, int suite) {
    @Override
    public String toString() {
        String displayRank = switch (rank) {
            case 11 -> "Jack";
            case 12 -> "Queen";
            case 13 -> "King";
            case 14 -> "Ace";
            default -> rank + "";
        };
        String displaySuite = switch (suite) {
            case 0 -> "Hearts";
            case 1 -> "Diamonds";
            case 2 -> "Spades";
            case 3 -> "Clubs";
            default -> "(" + suite + ")";
        };
        return displayRank + " of " + displaySuite;
    }
}
