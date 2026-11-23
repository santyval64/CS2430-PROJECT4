# CS2430-PROJECT4
# Monopoly Movement Simulation
 
**Members:** Santiago Valenzuela, Helen Lee, and Ana Hurtado 
**Course / section:** CS 2430 — Discrete Structures 
**Primary language:** Java

## What this program does

Simulates **one player** moving around the classic 40-square U.S. Monopoly board using two six-sided dice, Chance and Community Chest, the three-doubles rule, and Jail. It does **not** simulate purchases, rent, other players, auctions, or money. The $50 Jail payment is assumed, not deducted from an account.

It compares two project-required policies:

- **A_IMMEDIATE:** Use a held Get Out of Jail Free card immediately; otherwise assume the player pays $50 at the start of the next turn and then rolls normally.
- **B_TRY_DOUBLES:** Use a held card immediately; otherwise try to roll doubles on three Jail turns. Successful doubles move the player without an extra roll. After **three failed turns**, assume the player pays $50 and moves on the **fourth turn**. **Important:** This fourth-turn payment is the assignment's explicit simplification; Hasbro's standard game instead requires payment and movement on the third failed turn.

An observation is **the square where the player ends a complete turn** (after resolving all movement effects). Extra doubles rolls within a turn are not additional observations. A failed Jail-escape turn ends at Jail (index 10), and is counted there; the Jail index combines true incarceration and Just Visiting. This counting convention should be described in the report.

## Build and run


**macOS / Linux / Chromebook terminal:**

```bash
mkdir -p build
javac -d build src/*.java
java -cp build Main
```

**Windows (PowerShell):**

```powershell
New-Item -ItemType Directory -Force build | Out-Null
javac -d build src\*.java
java -cp build Main
```

Command-line options:

```bash
java -cp build Main --out output --seed 24302026
java -cp build Main --help
```

Change `--seed` to generate a different reproducible set of runs. Each run uses fresh player state, decks, and random generators. The default seed is 24302026.

## Output files

The runner performs **10 independent runs of 1,000,000 turns for each strategy**, recording cumulative snapshots at 1,000; 10,000; 100,000; and 1,000,000 turns. This is **20 runs and 80 snapshots**, with 40 board-square rows per snapshot.

- `output/landing_results.csv` — **all 80 datasets**, 40 raw landing counts and percentages each (3,200 rows plus header).
- `output/run_stats.csv` — final totals for dice rolls, moves, Jail turns, final position per run.
- `output/square_summary.csv` — 10-run mean, SD, minimum and maximum percentage for each square at each checkpoint.
- `output/strategy_comparison.csv` — mean landing percentages for A and B at 1,000,000 turns, with signed and absolute differences in percentage points.
- `output/run_summary.txt` — top squares, convergence diagnostics, and largest A/B differences, intended as **evidence**, not a finished written report.
- `output/landing_chart.png` — grouped horizontal bar chart for potential extra credit.

The CSV percentage is already multiplied by 100: for example, `3.250000` means **3.25%**, calculated as `100 × count / number_of_turns`.

## Source files

| File | Responsibility |
| --- | --- |
| `Board.java`, `Square.java` | Board order and square types |
| `Dice.java` | Rolling two fair six-sided dice |
| `Card.java`, `Deck.java` | Card effects, shuffling, discard and held cards |
| `Player.java` | Current square, Jail state, held cards, movement count |
| `JailStrategy.java` | Selection of A or B |
| `TurnEngine.java` | One complete turn, doubles, Jail, chain-resolving cards |
| `Simulation.java` | Batch loops, output files, summaries and chart |
| `Main.java` | Command-line entry point |

## Implementation conventions

1. Positions are **0–39** starting at GO (0). After a normal move, position becomes `(oldPosition + diceTotal) % 40`.
2. The third consecutive doubles roll within a single turn sends the player to Jail **before moving for that third roll**.
3. Card-induced movement can trigger another card, for example Chance 36 → Back Three → Community Chest 33. No movement cards beyond the documented classic deck are assumed.
4. Decks are shuffled at the start. Drawn non-held cards enter a discard pile, which is shuffled into a new draw pile once exhausted. A Get Out of Jail Free card stays with the player until used, then returns to its originating deck's discard pile. This implements the **project-requested discard/reshuffle interpretation**, rather than Hasbro's physical-deck bottom placement.
5. Non-movement card effects are ignored because buying and money are out of scope. The 2018 Chance Money Grab card is represented as `NONE` because it does not move the player.
6. Every turn contributes **exactly one** landing count. Initial setup on GO is not a landing. Do not treat each dice roll or pass-through square as another count.

## Team workflow

**Implementation Lead** owns the `src/` implementation and main-branch integration. **Verification Lead** independently owns the test plan, test execution, edge cases and verification evidence. **Communications Lead** owns the final report assembly, instructions/packaging, and screencast. The README and contributions record here are **starter material** for the Communications Lead to finalize; the `docs/ProjectPlan.md` is a template requiring team input.

The final report must be submitted **outside** the code ZIP. The team must also submit a screencast (10 minutes maximum), and each person submits separate weekly status report(s) and peer review. Update the placeholders and verify that the names, files, roles and evidence pointers match your actual work.

## Rules sources

- Hasbro (Jail): https://hasbro-new.custhelp.com/app/answers/detail/a_id/14/kw/exits/related/1
- Classic 2008–2021 cards including 2018 Money Grab: https://www.monopolyland.com/list-monopoly-chance-community-chest-cards/
- Assignment-provided 2018 U.S. rules and board reference are authoritative for the project. The fourth-turn Strategy B convention follows the assignment text.
