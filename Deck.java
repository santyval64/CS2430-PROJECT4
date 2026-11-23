/*
 * Team name: [ADD TEAM NAME]
 * Team members: [ADD ALL THREE NAMES]
 * Course: CS 2430, section [ADD SECTION]
 * Project: Programming Project 4 - Fall 2026
 * Primary author: [ADD AUTHOR]
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Each deck begins shuffled and has a draw pile + discard pile. Once the
 * draw pile empties, it is replenished by shuffling only the discarded cards.
 * Held Get Out of Jail Free cards do not reenter until they have been used.
 * This is the assignment's discard/reshuffle model (as opposed to keeping
 * used cards at the bottom of one circulating physical deck).
 */
public final class Deck {
    private final Card.DeckType type;
    private final Deque<Card> drawPile = new ArrayDeque<Card>();
    private final List<Card> discarded = new ArrayList<Card>();
    private final Random random;

    public Deck(Card.DeckType type, List<Card> cards, Random random, boolean shuffle) {
        this.type = type;
        this.random = random;
        List<Card> initial = new ArrayList<Card>(cards);
        for (Card card : initial) {
            if (card.getDeckType() != type) throw new IllegalArgumentException("Card belongs to the wrong deck");
        }
        if (initial.isEmpty()) throw new IllegalArgumentException("A deck cannot start empty");
        if (shuffle) Collections.shuffle(initial, random);
        for (Card card : initial) drawPile.addLast(card);
    }

    public static Deck createChance(Random random) {
        List<Card> cards = new ArrayList<Card>();
        Card.DeckType t = Card.DeckType.CHANCE;
        cards.add(new Card(t, "Advance to Boardwalk", Card.Action.ADVANCE_TO_BOARDWALK));
        cards.add(new Card(t, "Advance to GO", Card.Action.ADVANCE_TO_GO));
        cards.add(new Card(t, "Advance to Illinois Avenue", Card.Action.ADVANCE_TO_ILLINOIS));
        cards.add(new Card(t, "Advance to St. Charles Place", Card.Action.ADVANCE_TO_ST_CHARLES));
        cards.add(new Card(t, "Advance to nearest railroad A", Card.Action.NEAREST_RAILROAD));
        cards.add(new Card(t, "Advance to nearest railroad B", Card.Action.NEAREST_RAILROAD));
        cards.add(new Card(t, "Advance to nearest utility", Card.Action.NEAREST_UTILITY));
        cards.add(new Card(t, "Money Grab / bank dividend (money ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Get Out of Jail Free", Card.Action.GET_OUT_OF_JAIL_FREE));
        cards.add(new Card(t, "Go back three spaces", Card.Action.BACK_THREE));
        cards.add(new Card(t, "Go directly to Jail", Card.Action.GO_TO_JAIL));
        cards.add(new Card(t, "Property repairs (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Speeding fine (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Advance to Reading Railroad", Card.Action.ADVANCE_TO_READING));
        cards.add(new Card(t, "Chairman payment (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Building loan matures (ignored)", Card.Action.NONE));
        return new Deck(t, cards, random, true);
    }

    public static Deck createCommunityChest(Random random) {
        List<Card> cards = new ArrayList<Card>();
        Card.DeckType t = Card.DeckType.COMMUNITY_CHEST;
        cards.add(new Card(t, "Advance to GO", Card.Action.ADVANCE_TO_GO));
        cards.add(new Card(t, "Bank error in your favor (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Doctor's fee (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Sale of stock (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Get Out of Jail Free", Card.Action.GET_OUT_OF_JAIL_FREE));
        cards.add(new Card(t, "Go directly to Jail", Card.Action.GO_TO_JAIL));
        cards.add(new Card(t, "Holiday fund matures (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Income tax refund (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Birthday collection (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Life insurance (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Hospital fees (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "School fees (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Consultancy fee (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Street repairs (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Beauty contest prize (ignored)", Card.Action.NONE));
        cards.add(new Card(t, "Inheritance (ignored)", Card.Action.NONE));
        return new Deck(t, cards, random, true);
    }

    /** Draw one card; caller must either discard it or hold it with player. */
    public Card draw() {
        if (drawPile.isEmpty()) {
            if (discarded.isEmpty()) throw new IllegalStateException("No cards available in " + type);
            Collections.shuffle(discarded, random);
            for (Card card : discarded) drawPile.addLast(card);
            discarded.clear();
        }
        return drawPile.removeFirst();
    }

    /** Used cards go into the discard pile, including previously held jail cards. */
    public void discard(Card card) {
        if (card.getDeckType() != type) throw new IllegalArgumentException("Wrong deck for card return");
        discarded.add(card);
    }

    public int getDrawPileSize() { return drawPile.size(); }
    public int getDiscardPileSize() { return discarded.size(); }
}
