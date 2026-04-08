package durakcardgame;

import java.util.List;
import java.util.Random;

public class DurakAI {
    private final Random rng = new Random();

    public int sampleAction(int[] validActions) {
        return validActions[rng.nextInt(0, validActions.length)];
    }
}
