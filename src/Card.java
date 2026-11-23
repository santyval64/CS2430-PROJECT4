/*
 * Team name: [ADD TEAM NAME]
 * Team members: [ADD ALL THREE NAMES]
 * Course: CS 2430, section [ADD SECTION]
 * Project: Programming Project 4 - Fall 2026
 * Primary author: [ADD AUTHOR]
 */

/**
 * We store the movement effect, not the full commercial card text.
 * Non-movement cards are present in decks but have action NONE, because
 * money, property ownership, and transactions are outside project scope.
 */
public final class Card {
    public enum DeckType { CHANCE, COMMUNITY_CHEST }
    public enum Action {
        NONE, ADVANCE_TO_GO, ADVANCE_TO_BOARDWALK, ADVANCE_TO_ILLINOIS,
        ADVANCE_TO_ST_CHARLES, NEAREST_RAILROAD, NEAREST_UTILITY,
        ADVANCE_TO_READING, BACK_THREE, GO_TO_JAIL, GET_OUT_OF_JAIL_FREE
    }

    private final DeckType deckType;
    private final String description;
    private final Action action;

    public Card(DeckType deckType, String description, Action action) {
        this.deckType = deckType;
        this.description = description;
        this.action = action;
    }

    public DeckType getDeckType() { return deckType; }
    public String getDescription() { return description; }
    public Action getAction() { return action; }
}
