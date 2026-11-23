import java.util.EnumMap;
import java.util.Map;

/** The single player's position, jail status, card holdings, and move count. */
public final class Player {
    private int position = Board.GO;
    private boolean jailed;
    private int failedJailAttempts;
    private long positionMoves;
    private final Map<Card.DeckType, Card> jailCards =
            new EnumMap<Card.DeckType, Card>(Card.DeckType.class);

    public int getPosition() { return position; }
    public boolean isJailed() { return jailed; }
    public int getFailedJailAttempts() { return failedJailAttempts; }
    public long getPositionMoves() { return positionMoves; }

    /** Count each actual relocation as one movement event, including card jumps. */
    public void moveTo(int newPosition) {
        if (newPosition < 0 || newPosition >= Board.SIZE) throw new IllegalArgumentException("Bad board position");
        if (newPosition != position) positionMoves++;
        position = newPosition;
    }

    public void sendToJail() {
        moveTo(Board.JAIL);
        jailed = true;
        failedJailAttempts = 0;
    }

    public void releaseFromJail() {
        jailed = false;
        failedJailAttempts = 0;
    }

    public void recordFailedJailAttempt() {
        if (!jailed) throw new IllegalStateException("Not currently in jail");
        failedJailAttempts++;
    }

    public void holdJailCard(Card card) {
        if (card.getAction() != Card.Action.GET_OUT_OF_JAIL_FREE) throw new IllegalArgumentException("Not a jail card");
        if (jailCards.containsKey(card.getDeckType())) throw new IllegalStateException("Holding a second card from same deck");
        jailCards.put(card.getDeckType(), card);
    }

    public boolean hasJailCard() { return !jailCards.isEmpty(); }

    /** Prefer Chance if both decks' cards are in hand, for deterministic output. */
    public Card useJailCard() {
        Card c = jailCards.remove(Card.DeckType.CHANCE);
        if (c != null) return c;
        return jailCards.remove(Card.DeckType.COMMUNITY_CHEST);
    }
}
