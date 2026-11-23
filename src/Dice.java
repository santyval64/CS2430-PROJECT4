/*
 * Team name: [ADD TEAM NAME]
 * Team members: [ADD ALL THREE NAMES]
 * Course: CS 2430, section [ADD SECTION]
 * Project: Programming Project 4 - Fall 2026
 * Primary author: [ADD AUTHOR]
 */

import java.util.Random;

/** Two independent, uniform six-sided dice. */
public class Dice {
    private final Random random;

    public Dice(Random random) { this.random = random; }

    public Roll roll() {
        return new Roll(random.nextInt(6) + 1, random.nextInt(6) + 1);
    }

    public static final class Roll {
        private final int first;
        private final int second;

        public Roll(int first, int second) {
            if (first < 1 || first > 6 || second < 1 || second > 6) throw new IllegalArgumentException("Dice must be 1..6");
            this.first = first;
            this.second = second;
        }
        public int getFirst() { return first; }
        public int getSecond() { return second; }
        public int sum() { return first + second; }
        public boolean isDouble() { return first == second; }
    }
}
