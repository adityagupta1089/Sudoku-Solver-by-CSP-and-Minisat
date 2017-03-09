import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

public abstract class SudokuSolver {

  // ================================================================================
  // Internal Classes
  // ================================================================================
  /**
   * Variable class represents the positions in the Sudoku-grid, which starts from top-left at (0,0)
   * to (8,8) at bottom-right corner.
   */
  public class Variable {
    public int row;
    public int col;

    public Variable(final int prow, final int pcol) {
      this.row = prow;
      this.col = pcol;
    }

    @Override
    public boolean equals(final Object obj) {
      final Variable other = (Variable) obj;
      if ((this.col != other.col) || (this.row != other.row)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.col;
      result = (prime * result) + this.row;
      return result;
    }

    @Override
    public String toString() {
      return "(" + this.row + ", " + this.col + ")";
    }

  }

  /** A Pair of Variables. */
  public class VariablesPair {
    public Variable var1;
    public Variable var2;

    public VariablesPair(final Variable v1, final Variable v2) {
      this.var1 = v1;
      this.var2 = v2;
    }

    @Override
    public String toString() {
      return "(" + this.var1 + ", " + this.var2 + ")";
    }

  }

  // ================================================================================
  // Static Variables
  // ================================================================================
  private static final int CASE_NONE = 0; /* None */
  private static final int CASE_MINIMUM_REMAINING_VALUE = 1; /* Minimum Remaining Value */
  private static final int CASE_LEAST_CONSTRAINING_VALUE = 2; /* Least Constraining Value */
  private static final int CASE_MAINTAINING_ARC_CONSISTENCY = 3; /* Maintaining Arc Consistency */

  public static long backTracks = 0;

  // ================================================================================
  // Other Variables
  // ================================================================================
  private final int[] grid;

  public Collection<Variable> unassignedVariables;

  // ================================================================================
  // Main
  // ================================================================================

  /**
   * This does the following:
   * <ul>
   * <li>Parses arguments for heuristic and input-output files.</li>
   * <li>Reads the files and for each line, solves it and prints it to the output file</li>
   * </ul>
   *
   * @param argsInput
   *          File, Output File, Heuristic Id (0: None, 1: MRV, 2: MRV+LCV, 3: MRV+LCV+MAC)
   * @throws IOException
   *           if input-output file could not be opened or closed.
   */
  public static void main(final String[] args) throws IOException {
    /* arguments check */
    int heuristic = -1;
    if ((args.length < 3) || ((heuristic = Integer.parseInt(args[2])) < 0) || (heuristic > 3)) {
      System.out.println("There should be three input arguments in the format:\n"
          + "\t<input file> <output file> <heuristic id>");
      System.out.println("\tHeuristic id: " + CASE_NONE + ". None, " + CASE_MINIMUM_REMAINING_VALUE
          + ". MRV, " + CASE_LEAST_CONSTRAINING_VALUE + ". MRV+LCV, "
          + CASE_MAINTAINING_ARC_CONSISTENCY + ". MRV+LCV+MAC");
      return;
    }

    /* input and output */
    final BufferedReader in = new BufferedReader(new FileReader(args[0]));
    final FileWriter fw = new FileWriter(args[1]);

    /* reading file and solving, then printing it */
    String line = null;

    /* Time and Memory */
    final long t0 = System.currentTimeMillis();

    while ((line = in.readLine()) != null) {
      SudokuSolver solver = null;
      /* Select Solver */
      switch (heuristic) {
        case CASE_NONE:
          solver = new BSSudokuSovler(line);
          break;
        case CASE_MINIMUM_REMAINING_VALUE:
          solver = new BSISudokuSolver(line);
          break;
        case CASE_LEAST_CONSTRAINING_VALUE:
          solver = new BSIISudokuSolver(line);
          break;
        case CASE_MAINTAINING_ARC_CONSISTENCY:
          solver = new BSMACSudokuSolver(line);
          break;
      }
      /* Solve */
      final boolean solved = solver.solve();
      if (!solved) {
        System.out.println("Could not solve: " + line);
        break;
      }
      fw.write(solver.solution() + "\n");
    }

    /* Time Calculation */
    double sec = (double) (System.currentTimeMillis() - t0) / 1000;
    final int min = (int) (sec / 60);
    sec -= 60 * min;
    System.out.println("Took " + min + " minute(s) and " + sec + " second(s).");
    /* Backtracks */
    System.out.println(
        "Total backtracks: " + NumberFormat.getNumberInstance(Locale.US).format(backTracks) + ".");
    /* Memory Usage */

    /* close input and output */
    in.close();
    fw.close();

  }

  public static String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit)
      return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  // ================================================================================
  // Constructor
  // ================================================================================
  /**
   * Parses the input line.
   * <ul>
   * <li>Creates a 2-dimensional integer grid from the input line.</li>
   * <li>Selects appropriate comparators corresponding to the heuristic.</li>
   * <li>Populates unassigned variables</li>
   * <li>Initialize domains</li>
   * </ul>
   *
   * @param line
   *          corresponds to a single puzzle. The puzzles are all rasterised row-wise. Empty squares
   *          in the puzzle are represented as ‘.’
   *
   */
  public SudokuSolver(final String line) {
    this.grid = new int[81];

    this.unassignedVariables = new LinkedList<>();

    /* Parse Line */
    for (int i = 0; i < 81; i++) {
      /* Value at each cell */
      final char cval = line.charAt(i);
      this.grid[i] = cval == '.' ? 0 : cval - '0';
      /* if not assigned */
      if (cval == '.') {
        final Variable var = new Variable(i / 9, i % 9);
        this.unassignedVariables.add(var);
      }
    }
  }
  // ================================================================================
  // Solve
  // ================================================================================

  /** Children classes override this method and solve the sudoku */
  public abstract boolean solve();

  /** Returns whether the Sudoku has been solved. */
  public abstract boolean isComplete();

  /**
   * Checks whether a values is consistent with the current assignment for a given variable.
   *
   * @param var
   *          is the given variable.
   * @param v
   *          is the value to check for.
   */
  public boolean isConsistent(final Variable var, final int v) {
    /* row check */
    for (int j = 0; j < 9; j++) {
      if ((j != var.col) && (this.getValue(var.row, j) == v)) {
        return false;
      }
    }
    /* column check */
    for (int i = 0; i < 9; i++) {
      if ((i != var.row) && (this.getValue(i, var.col) == v)) {
        return false;
      }
    }
    /* block check */
    for (int i = 3 * (var.row / 3); i < ((3 * (var.row / 3)) + 3); i++) {
      for (int j = 3 * (var.col / 3); j < ((3 * (var.col / 3)) + 3); j++) {
        if (((i != var.row) || (j != var.col)) && (this.getValue(i, j) == v)) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean isRelated(final Variable var, final Variable var2) {
    return !var.equals(var2) && ((var.row == var2.row) || (var.col == var2.col)
        || (((var.row / 3) == (var2.row / 3)) && ((var.col / 3) == (var2.col / 3))));
  }

  public void setValue(final Variable var, final int val) {
    this.grid[(var.row * 9) + var.col] = val;
  }

  // ================================================================================
  // Other Helper Methods
  // ================================================================================
  public int getValue(final int row, final int col) {
    return this.grid[(row * 9) + col];
  }

  // ================================================================================
  // String Methods
  // ================================================================================
  public String solution() {
    String string = "";
    for (int i = 0; i < 81; i++) {
      string += this.grid[i];
    }
    return string;
  }

  @Override
  public String toString() {
    String string = "";
    /* Header */
    string += " +012+345+678+\n";
    string += " +---+---+---+\n";
    /* Loop over rows */
    for (int i = 0; i < 9; i++) {
      /* Left Boundary */
      string += i + "|";
      for (int j = 0; j < 9; j++) {
        /* Value in the cell */
        string += this.getValue(i, j) != 0 ? this.getValue(i, j) : ".";
        /* Box-Separator */
        if ((j % 3) == 2) {
          string += "|";
        }
      }
      /* Box-Separator */
      if ((i % 3) == 2) {
        string += "\n +---+---+---+";
      }
      /* Next row */
      string += "\n";
    }
    return string;

  }

}
