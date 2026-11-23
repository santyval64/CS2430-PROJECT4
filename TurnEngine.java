/*
 * Team name: [ADD TEAM NAME]
 * Team members: [ADD ALL THREE NAMES]
 * Course: CS 2430, section [ADD SECTION]
 * Project: Programming Project 4 - Fall 2026
 * Primary author: [ADD AUTHOR]
 */

/**
 * Executes ONE complete turn (potentially including extra rolls for doubles).
 * The only square counted is the final square at the end of that turn.
 */
public final class TurnEngine {
    private final Board board;
    private final Player player;
    private final Deck chance;
    private final Deck chest;
    private final Dice dice;
    private final JailStrategy strategy;
    private long diceRolls;
    private long jailedTurns;

    public TurnEngine(Board board, Player player, Deck chance, Deck chest,
                      Dice dice, JailStrategy strategy) {
        this.board = board;
        this.player = player;
        this.chance = chance;
        this.chest = chest;
        this.dice = dice;
        this.strategy = strategy;
    }

    /** The caller records this final position exactly once per completed turn. */
    public int takeTurn() {
        // Handle incarceration BEFORE the ordinary roll/doubles loop.
        if (player.isJailed()) {
            jailedTurns++;
            if (player.hasJailCard()) {
                Card card = player.useJailCard();
                deckFor(card.getDeckType()).discard(card);
                player.releaseFromJail();
            } else if (strategy == JailStrategy.A_IMMEDIATE) {
                // Simulate paying $50 at the start of the next turn.
                player.releaseFromJail();
            } else {
                // Project instruction: 3 FAILED jail turns; payment on turn 4.
                if (player.getFailedJailAttempts() < 3) {
                    Dice.Roll attempt = rollDice();
                    if (attempt.isDouble()) {
                        player.releaseFromJail();
                        advance(attempt.sum());
                        resolveSquareEffects();
                        // Doubles that RELEASE from Jail do NOT grant another roll.
                    } else {
                        player.recordFailedJailAttempt();
                        // Player stays on index 10; this turn still counts as Jail.
                    }
                    return player.getPosition();
                }
                // Three failures occurred on earlier turns. Pay on 4th turn.
                player.releaseFromJail();
            }
        }

        // Normal turn: doubles grant another roll, up to the third double.
        int successiveDoubles = 0;
        while (true) {
            Dice.Roll roll = rollDice();
            if (roll.isDouble()) {
                successiveDoubles++;
                if (successiveDoubles == 3) {
                    player.sendToJail(); // No movement from third roll.
                    return player.getPosition();
                }
            } else {
                successiveDoubles = 0;
            }

            advance(roll.sum());
            resolveSquareEffects();
            if (player.isJailed() || !roll.isDouble()) return player.getPosition();
        }
    }

    private Dice.Roll rollDice() {
        diceRolls++;
        return dice.roll();
    }

    private void advance(int steps) {
        player.moveTo((player.getPosition() + steps) % Board.SIZE);
    }

    private Deck deckFor(Card.DeckType type) {
        return type == Card.DeckType.CHANCE ? chance : chest;
    }

    /**
     * A movement card may trigger another effect (e.g., Chance 36 -> back 3
     * -> Community Chest 33). Repeat until the player has no more effects.
     */
    private void resolveSquareEffects() {
        for (int depth = 0; depth < 12; depth++) {
            Square.Type type = board.get(player.getPosition()).getType();
            if (type == Square.Type.GO_TO_JAIL) {
                player.sendToJail();
                return;
            }
            if (type != Square.Type.CHANCE && type != Square.Type.COMMUNITY_CHEST) return;

            Deck deck = type == Square.Type.CHANCE ? chance : chest;
            Card card = deck.draw();
            if (card.getAction() == Card.Action.GET_OUT_OF_JAIL_FREE) {
                player.holdJailCard(card); // Withheld from deck until used.
                return;
            }
            deck.discard(card);

            int oldPosition = player.getPosition();
            if (card.getAction() == Card.Action.GO_TO_JAIL) {
                player.sendToJail();
                return;
            }
            switch (card.getAction()) {
                case ADVANCE_TO_GO: player.moveTo(0); break;
                case ADVANCE_TO_BOARDWALK: player.moveTo(39); break;
                case ADVANCE_TO_ILLINOIS: player.moveTo(24); break;
                case ADVANCE_TO_ST_CHARLES: player.moveTo(11); break;
                case ADVANCE_TO_READING: player.moveTo(5); break;
                case NEAREST_RAILROAD:
                    player.moveTo(Board.nearestForward(oldPosition, 5, 15, 25, 35));
                    break;
                case NEAREST_UTILITY:
                    player.moveTo(Board.nearestForward(oldPosition, 12, 28));
                    break;
                case BACK_THREE:
                    player.moveTo((oldPosition + Board.SIZE - 3) % Board.SIZE);
                    break;
                case NONE: return;
                default: throw new IllegalStateException("Unhandled card action: " + card.getAction());
            }
            // The new square is checked again on the next loop iteration.
        }
        throw new IllegalStateException("Unexpectedly long card-effect chain");
    }

    public long getDiceRolls() { return diceRolls; }
    public long getJailedTurns() { return jailedTurns; }
}
