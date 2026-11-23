/** One of the 40 indexed spaces on the standard US Monopoly board. */
public final class Square {
    public enum Type {
        GO, PROPERTY, RAILROAD, UTILITY, CHANCE, COMMUNITY_CHEST,
        JAIL, GO_TO_JAIL, FREE_PARKING, TAX
    }

    private final int index;
    private final String name;
    private final Type type;

    public Square(int index, String name, Type type) {
        this.index = index;
        this.name = name;
        this.type = type;
    }

    public int getIndex() { return index; }
    public String getName() { return name; }
    public Type getType() { return type; }
}
