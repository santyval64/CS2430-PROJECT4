import java.nio.file.Path;
import java.nio.file.Paths;

/** Command-line entry point; Java 8+ and no third-party dependencies needed. */
public final class Main {
    public static void main(String[] args) throws Exception {
        // Allow CSV output and PNG chart creation on terminal-only computers.
        System.setProperty("java.awt.headless", "true");
        Path out = Paths.get("output");
        long seed = 24302026L;
        for (int i = 0; i < args.length; i++) {
            if ("--out".equals(args[i]) && i + 1 < args.length) {
                out = Paths.get(args[++i]);
            } else if ("--seed".equals(args[i]) && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if ("--help".equals(args[i])) {
                System.out.println("Usage: java -cp build Main [--out output-folder] [--seed 24302026]");
                return;
            } else {
                throw new IllegalArgumentException("Unknown/incomplete option: " + args[i]);
            }
        }
        new Simulation().runAll(out, seed);
    }
}
