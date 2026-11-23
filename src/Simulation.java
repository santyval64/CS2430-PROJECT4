import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Runs 10 independent 1-million-turn simulations for each strategy.
 * Saving four checkpoints within each run produces 80 distinct datasets.
 * The simulation counts exactly ONE final square per completed player turn.
 */
public final class Simulation {
    public static final int RUNS = 10;
    public static final int[] CHECKPOINTS = {1000, 10000, 100000, 1000000};
    private final Board board = new Board();

    public static final class Snapshot {
        public final JailStrategy strategy;
        public final int run;
        public final int turns;
        public final long[] counts;
        Snapshot(JailStrategy strategy, int run, int turns, long[] counts) {
            this.strategy = strategy;
            this.run = run;
            this.turns = turns;
            this.counts = Arrays.copyOf(counts, counts.length);
        }
        public double percent(int square) { return 100.0 * counts[square] / turns; }
    }

    /** Run a smaller, testable simulation without touching the filesystem. */
    public Snapshot runSingle(JailStrategy strategy, int runNumber, int turns, long seed) {
        Random master = new Random(seed);
        Dice dice = new Dice(new Random(master.nextLong()));
        Deck chance = Deck.createChance(new Random(master.nextLong()));
        Deck chest = Deck.createCommunityChest(new Random(master.nextLong()));
        Player player = new Player();
        TurnEngine engine = new TurnEngine(board, player, chance, chest, dice, strategy);
        long[] counts = new long[Board.SIZE];
        for (int i = 0; i < turns; i++) counts[engine.takeTurn()]++;
        validateCountTotal(counts, turns);
        return new Snapshot(strategy, runNumber, turns, counts);
    }

    /** Main batch runner: writes the CSV datasets and an interpretation summary. */
    public void runAll(Path outputFolder, long seed) throws IOException {
        Files.createDirectories(outputFolder);
        List<Snapshot> all = new ArrayList<Snapshot>();
        Random runSeedSource = new Random(seed);
        Path rawFile = outputFolder.resolve("landing_results.csv");
        Path statsFile = outputFolder.resolve("run_stats.csv");
        try (BufferedWriter raw = Files.newBufferedWriter(rawFile, StandardCharsets.UTF_8);
             BufferedWriter stats = Files.newBufferedWriter(statsFile, StandardCharsets.UTF_8)) {
            raw.write("strategy,run,turns,square_index,square_name,landing_count,percentage\n");
            stats.write("strategy,run,turns,dice_rolls,position_moves,jailed_turns,final_position,ended_in_jail\n");

            for (JailStrategy strategy : JailStrategy.values()) {
                for (int run = 1; run <= RUNS; run++) {
                    // Separate random generators per run ensure independent sequences.
                    Random master = new Random(runSeedSource.nextLong());
                    Dice dice = new Dice(new Random(master.nextLong()));
                    Deck chance = Deck.createChance(new Random(master.nextLong()));
                    Deck chest = Deck.createCommunityChest(new Random(master.nextLong()));
                    Player player = new Player();
                    TurnEngine engine = new TurnEngine(board, player, chance, chest, dice, strategy);
                    long[] counts = new long[Board.SIZE];
                    int nextCheckpoint = 0;

                    for (int turn = 1; turn <= CHECKPOINTS[CHECKPOINTS.length - 1]; turn++) {
                        counts[engine.takeTurn()]++;
                        if (turn == CHECKPOINTS[nextCheckpoint]) {
                            validateCountTotal(counts, turn);
                            Snapshot snap = new Snapshot(strategy, run, turn, counts);
                            all.add(snap);
                            writeRawSnapshot(raw, snap);
                            nextCheckpoint++;
                        }
                    }
                    stats.write(String.format(Locale.US, "%s,%d,%d,%d,%d,%d,%d,%s%n",
                            strategy, run, CHECKPOINTS[CHECKPOINTS.length - 1],
                            engine.getDiceRolls(), player.getPositionMoves(),
                            engine.getJailedTurns(), player.getPosition(), player.isJailed()));
                    System.out.printf(Locale.US, "Finished %s run %d/%d%n", strategy, run, RUNS);
                }
            }
        }

        if (all.size() != 2 * RUNS * CHECKPOINTS.length) {
            throw new IllegalStateException("Expected 80 snapshots, got " + all.size());
        }
        writeMeans(outputFolder.resolve("square_summary.csv"), all);
        writeComparison(outputFolder.resolve("strategy_comparison.csv"), all);
        writeTextSummary(outputFolder.resolve("run_summary.txt"), all, seed);
        writeBarChart(outputFolder.resolve("landing_chart.png"), all);
        System.out.println("Saved 80 datasets and summaries to " + outputFolder.toAbsolutePath());
    }

    private void writeRawSnapshot(BufferedWriter writer, Snapshot snap) throws IOException {
        for (int i = 0; i < Board.SIZE; i++) {
            writer.write(String.format(Locale.US, "%s,%d,%d,%d,%s,%d,%.6f%n",
                    snap.strategy, snap.run, snap.turns, i,
                    board.get(i).getName(), snap.counts[i], snap.percent(i)));
        }
    }

    /** Verify that exactly one final landing was recorded per complete turn. */
    private void validateCountTotal(long[] counts, long turns) {
        long sum = 0;
        for (long c : counts) sum += c;
        if (sum != turns) throw new IllegalStateException("Counts " + sum + " do not match " + turns + " turns");
    }

    private List<Snapshot> select(List<Snapshot> all, JailStrategy strategy, int turns) {
        List<Snapshot> selected = new ArrayList<Snapshot>();
        for (Snapshot snap : all) if (snap.strategy == strategy && snap.turns == turns) selected.add(snap);
        if (selected.size() != RUNS) throw new IllegalStateException("Missing snapshots for " + strategy + "/" + turns);
        return selected;
    }

    private double meanPercent(List<Snapshot> snaps, int square) {
        double sum = 0;
        for (Snapshot s : snaps) sum += s.percent(square);
        return sum / snaps.size();
    }

    private double standardDeviation(List<Snapshot> snaps, int square) {
        double avg = meanPercent(snaps, square);
        double sum = 0;
        for (Snapshot s : snaps) sum += Math.pow(s.percent(square) - avg, 2);
        return Math.sqrt(sum / (snaps.size() - 1));
    }

    private double rangePercent(List<Snapshot> snaps, int square) {
        double low = Double.POSITIVE_INFINITY;
        double high = Double.NEGATIVE_INFINITY;
        for (Snapshot s : snaps) {
            low = Math.min(low, s.percent(square));
            high = Math.max(high, s.percent(square));
        }
        return high - low;
    }

    /** Summaries across 10 independent runs for both strategies at every n. */
    private void writeMeans(Path path, List<Snapshot> all) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("strategy,turns,square_index,square_name,mean_percentage,sd_percentage_points,min_percentage,max_percentage\n");
            for (JailStrategy strategy : JailStrategy.values()) {
                for (int n : CHECKPOINTS) {
                    List<Snapshot> snaps = select(all, strategy, n);
                    for (int i = 0; i < Board.SIZE; i++) {
                        double low = Double.POSITIVE_INFINITY;
                        double high = Double.NEGATIVE_INFINITY;
                        for (Snapshot s : snaps) {
                            low = Math.min(low, s.percent(i));
                            high = Math.max(high, s.percent(i));
                        }
                        writer.write(String.format(Locale.US, "%s,%d,%d,%s,%.6f,%.6f,%.6f,%.6f%n",
                                strategy, n, i, board.get(i).getName(), meanPercent(snaps, i),
                                standardDeviation(snaps, i), low, high));
                    }
                }
            }
        }
    }

    private void writeComparison(Path path, List<Snapshot> all) throws IOException {
        List<Snapshot> a = select(all, JailStrategy.A_IMMEDIATE, 1000000);
        List<Snapshot> b = select(all, JailStrategy.B_TRY_DOUBLES, 1000000);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("square_index,square_name,strategy_a_mean_percentage,strategy_b_mean_percentage,b_minus_a_percentage_points,absolute_gap_percentage_points\n");
            for (int i = 0; i < Board.SIZE; i++) {
                double av = meanPercent(a, i);
                double bv = meanPercent(b, i);
                writer.write(String.format(Locale.US, "%d,%s,%.6f,%.6f,%+.6f,%.6f%n",
                        i, board.get(i).getName(), av, bv, bv - av, Math.abs(bv - av)));
            }
        }
    }

    /**
     * Extra-credit visualization: horizontal grouped bar chart of the 40
     * mean landing percentages at 1,000,000 turns for both strategies.
     * Java2D comes with the JDK, so no second programming language is needed.
     */
    private void writeBarChart(Path path, List<Snapshot> all) throws IOException {
        List<Snapshot> a = select(all, JailStrategy.A_IMMEDIATE, 1000000);
        List<Snapshot> b = select(all, JailStrategy.B_TRY_DOUBLES, 1000000);
        final int width = 1600, height = 1710;
        final int left = 315, right = 90, top = 175, rowHeight = 35;
        final int plotWidth = width - left - right;
        final double maxPercent = 15.0;
        final Color blue = new Color(36, 112, 169);
        final Color orange = new Color(222, 119, 51);
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setColor(new Color(30, 42, 54));
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            g.drawString("Monopoly landing percentages by Jail strategy", 42, 52);
            g.setFont(new Font("SansSerif", Font.PLAIN, 19));
            g.drawString("Mean of 10 independent runs, 1,000,000 complete turns per run", 42, 84);
            g.drawString("Percentage of turn-end landings on each board space", 42, 111);
            g.setColor(blue);
            g.fillRect(42, 132, 24, 15);
            g.setColor(new Color(30, 42, 54));
            g.drawString("A: Immediate exit", 76, 147);
            g.setColor(orange);
            g.fillRect(330, 132, 24, 15);
            g.setColor(new Color(30, 42, 54));
            g.drawString("B: Try doubles", 364, 147);
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            for (int tick = 0; tick <= 14; tick += 2) {
                int x = left + (int) Math.round(plotWidth * tick / maxPercent);
                g.setColor(new Color(224, 230, 235));
                g.setStroke(new BasicStroke(1));
                g.drawLine(x, top - 4, x, top + rowHeight * Board.SIZE);
                g.setColor(new Color(75, 88, 99));
                g.drawString(tick + "%", x - 12, top - 12);
            }
            for (int i = 0; i < Board.SIZE; i++) {
                int y = top + i * rowHeight;
                if (i % 2 == 0) {
                    g.setColor(new Color(247, 249, 251));
                    g.fillRect(20, y, width - 35, rowHeight);
                }
                g.setColor(new Color(44, 55, 64));
                g.setFont(new Font("SansSerif", Font.PLAIN, 16));
                g.drawString(String.format(Locale.US, "%02d  %s", i, board.get(i).getName()), 25, y + 23);
                g.setColor(blue);
                g.fillRect(left, y + 5, (int) Math.round(plotWidth * meanPercent(a, i) / maxPercent), 12);
                g.setColor(orange);
                g.fillRect(left, y + 19, (int) Math.round(plotWidth * meanPercent(b, i) / maxPercent), 12);
            }
            g.setColor(new Color(55, 65, 72));
            g.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g.drawString("Note: Jail / Just Visiting combines both statuses at board index 10.", 25, height - 70);
            g.drawString("Strategy B follows the assignment's fourth-turn payment convention.", 25, height - 46);
        } finally {
            g.dispose();
        }
        if (!ImageIO.write(canvas, "png", path.toFile())) {
            throw new IOException("PNG writer unavailable in this Java environment");
        }
    }

    /** Text ready to reference in the team report (not a substitute for analysis). */
    private void writeTextSummary(Path path, List<Snapshot> all, long seed) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("MONOPOLY SIMULATION — BATCH SUMMARY\n");
        out.append("Seed: ").append(seed).append(" | 10 runs/strategy | final checkpoints after 1,000,000 turns\n");
        out.append("Each checkpoint counts one final landing per complete turn.\n");
        out.append("Strategy B: release after up to 3 doubles attempts; on 3 failures, pay and move on turn 4.\n\n");

        for (JailStrategy strat : JailStrategy.values()) {
            out.append(strat).append(" — top five squares by mean landing percentage at n=1,000,000:\n");
            List<Snapshot> snaps = select(all, strat, 1000000);
            List<Integer> indexes = new ArrayList<Integer>();
            for (int i = 0; i < Board.SIZE; i++) indexes.add(i);
            Collections.sort(indexes, new Comparator<Integer>() {
                public int compare(Integer x, Integer y) {
                    return Double.compare(meanPercent(snaps, y), meanPercent(snaps, x));
                }
            });
            for (int k = 0; k < 5; k++) {
                int i = indexes.get(k);
                out.append(String.format(Locale.US, "  %d. [%02d] %-27s %.4f%%%n",
                        k + 1, i, board.get(i).getName(), meanPercent(snaps, i)));
            }
            out.append("\n");
        }

        out.append("Variation across 10 independent runs (Strategy A):\n");
        out.append("n             mean within-square SD (percentage points)    largest within-square range (pp)\n");
        for (int n : CHECKPOINTS) {
            List<Snapshot> a = select(all, JailStrategy.A_IMMEDIATE, n);
            double sdSum = 0, largestRange = 0;
            for (int i = 0; i < Board.SIZE; i++) {
                sdSum += standardDeviation(a, i);
                largestRange = Math.max(largestRange, rangePercent(a, i));
            }
            out.append(String.format(Locale.US, "%-13d %-43.6f %.6f%n", n, sdSum / Board.SIZE, largestRange));
        }

        out.append("\nStrategy B minus A at n=1,000,000 (10-run means):\n");
        List<Snapshot> a = select(all, JailStrategy.A_IMMEDIATE, 1000000);
        List<Snapshot> b = select(all, JailStrategy.B_TRY_DOUBLES, 1000000);
        List<Integer> byGap = new ArrayList<Integer>();
        for (int i = 0; i < Board.SIZE; i++) byGap.add(i);
        Collections.sort(byGap, new Comparator<Integer>() {
            public int compare(Integer x, Integer y) {
                double dx = Math.abs(meanPercent(b, x) - meanPercent(a, x));
                double dy = Math.abs(meanPercent(b, y) - meanPercent(a, y));
                return Double.compare(dy, dx);
            }
        });
        double tvDistance = 0;
        for (int i = 0; i < Board.SIZE; i++) {
            tvDistance += Math.abs(meanPercent(b, i) - meanPercent(a, i)) / 200.0;
        }
        out.append(String.format(Locale.US, "Total variation distance between average distributions: %.6f%n", tvDistance));
        for (int k = 0; k < 8; k++) {
            int i = byGap.get(k);
            out.append(String.format(Locale.US, "  [%02d] %-27s A=%.4f%% B=%.4f%% delta=%+.4f pp%n",
                    i, board.get(i).getName(), meanPercent(a, i), meanPercent(b, i),
                    meanPercent(b, i) - meanPercent(a, i)));
        }
        out.append("\nInterpretation should consider uncertainty across runs and the difference between\n");
        out.append("increasing-n stabilization within a strategy vs matching A and B distributions.\n");
        out.append("Inspect landing_results.csv, square_summary.csv, and strategy_comparison.csv.\n");
        Files.write(path, out.toString().getBytes(StandardCharsets.UTF_8));
    }
}
