/** The original US classic 40-square board, indexed clockwise from GO. */
public final class Board {
    public static final int SIZE = 40;
    public static final int GO = 0;
    public static final int JAIL = 10;
    public static final int GO_TO_JAIL = 30;
    private final Square[] squares = new Square[SIZE];

    public Board() {
        add(0, "GO", Square.Type.GO);
        add(1, "Mediterranean Avenue", Square.Type.PROPERTY);
        add(2, "Community Chest", Square.Type.COMMUNITY_CHEST);
        add(3, "Baltic Avenue", Square.Type.PROPERTY);
        add(4, "Income Tax", Square.Type.TAX);
        add(5, "Reading Railroad", Square.Type.RAILROAD);
        add(6, "Oriental Avenue", Square.Type.PROPERTY);
        add(7, "Chance", Square.Type.CHANCE);
        add(8, "Vermont Avenue", Square.Type.PROPERTY);
        add(9, "Connecticut Avenue", Square.Type.PROPERTY);
        add(10, "Jail / Just Visiting", Square.Type.JAIL);
        add(11, "St. Charles Place", Square.Type.PROPERTY);
        add(12, "Electric Company", Square.Type.UTILITY);
        add(13, "States Avenue", Square.Type.PROPERTY);
        add(14, "Virginia Avenue", Square.Type.PROPERTY);
        add(15, "Pennsylvania Railroad", Square.Type.RAILROAD);
        add(16, "St. James Place", Square.Type.PROPERTY);
        add(17, "Community Chest", Square.Type.COMMUNITY_CHEST);
        add(18, "Tennessee Avenue", Square.Type.PROPERTY);
        add(19, "New York Avenue", Square.Type.PROPERTY);
        add(20, "Free Parking", Square.Type.FREE_PARKING);
        add(21, "Kentucky Avenue", Square.Type.PROPERTY);
        add(22, "Chance", Square.Type.CHANCE);
        add(23, "Indiana Avenue", Square.Type.PROPERTY);
        add(24, "Illinois Avenue", Square.Type.PROPERTY);
        add(25, "B. & O. Railroad", Square.Type.RAILROAD);
        add(26, "Atlantic Avenue", Square.Type.PROPERTY);
        add(27, "Ventnor Avenue", Square.Type.PROPERTY);
        add(28, "Water Works", Square.Type.UTILITY);
        add(29, "Marvin Gardens", Square.Type.PROPERTY);
        add(30, "Go to Jail", Square.Type.GO_TO_JAIL);
        add(31, "Pacific Avenue", Square.Type.PROPERTY);
        add(32, "North Carolina Avenue", Square.Type.PROPERTY);
        add(33, "Community Chest", Square.Type.COMMUNITY_CHEST);
        add(34, "Pennsylvania Avenue", Square.Type.PROPERTY);
        add(35, "Short Line", Square.Type.RAILROAD);
        add(36, "Chance", Square.Type.CHANCE);
        add(37, "Park Place", Square.Type.PROPERTY);
        add(38, "Luxury Tax", Square.Type.TAX);
        add(39, "Boardwalk", Square.Type.PROPERTY);
    }

    private void add(int index, String name, Square.Type type) {
        squares[index] = new Square(index, name, type);
    }

    public Square get(int index) {
        if (index < 0 || index >= SIZE) throw new IllegalArgumentException("Bad index: " + index);
        return squares[index];
    }

    /** Advance around the board clockwise, even when the target wraps past GO. */
    public static int nearestForward(int current, int... destinations) {
        int chosen = -1;
        int shortestDistance = SIZE + 1;
        for (int target : destinations) {
            int distance = (target - current + SIZE) % SIZE;
            if (distance == 0) distance = SIZE;
            if (distance < shortestDistance) {
                shortestDistance = distance;
                chosen = target;
            }
        }
        return chosen;
    }
}
