import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class SudokuSolver {
  private static final int CASE_NONE = 0;
  private static final int CASE_MRV = 1;
  private static final int CASE_LCV = 2;
  private static final int CASE_MAC = 3;

  private int[][] grid;
  private PriorityQueue<Variable> unassignedVariables;
  private Comparator<Integer> valueComparator;

  private class Variable {
    int row;
    int col;

    public Variable(int prow, int pcol) {
      row = prow;
      col = pcol;
    }

    @Override
    public String toString() {
      return "(" + row + ", " + col + ")";
    }

  }

  private class VariableValuePair {
    Variable var;
    int val;

    public VariableValuePair(Variable pvar, int pval) {
      var = pvar;
      val = pval;
    }

    @Override
    public String toString() {
      return "(" + var + ", " + val + ")";
    }
  }

  public SudokuSolver(String line, int heuristic) {
    grid = new int[9][9];
    Comparator<Variable> variableComparator = null;
    switch (heuristic) {
      /* Variable Comparator */
      case CASE_NONE:
        variableComparator = new Comparator<Variable>() {
          @Override
          public int compare(Variable v1, Variable v2) {
            return v1.row == v2.row ? Integer.compare(v1.col, v2.col)
                : Integer.compare(v1.row, v2.row);
          }
        };
        break;
      case CASE_MRV:
      case CASE_LCV:
      case CASE_MAC:
        variableComparator = new Comparator<Variable>() {
          @Override
          public int compare(Variable v1, Variable v2) {
            int rv1 = 0;
            int rv2 = 0;
            for (int i = 1; i <= 9; i++) {
              if (isConsistent(v1, i)) {
                rv1++;
              }
              if (isConsistent(v2, i)) {
                rv2++;
              }
            }
            return Integer.compare(rv1, rv2);
          }
        };
      default:
        break;
    }
    switch (heuristic) {
      /* Variable Comparator */
      case CASE_NONE:
      case CASE_MRV:
        /* Default Value */
        break;
      case CASE_LCV:
      case CASE_MAC:
        valueComparator = new Comparator<Integer>() {
          @Override
          public int compare(Integer val1, Integer val2) {
            int cnt1 = 0;
            int cnt2 = 0;
            /* Try 1st value */
            grid[currVar.row][currVar.col] = val1;
            for (Variable var : unassignedVariables) {
              for (int v = 1; v <= 9; v++) {
                if (isConsistent(var, v)) {
                  cnt1++;
                }
              }
            }
            /* Try 2nd value */
            grid[currVar.row][currVar.col] = val2;
            for (Variable var : unassignedVariables) {
              for (int v = 1; v <= 9; v++) {
                if (isConsistent(var, v)) {
                  cnt2++;
                }
              }
            }
            /* Restore value */
            grid[currVar.row][currVar.col] = 0;
            /* Compare reverse */
            return Integer.compare(cnt2, cnt1);
          }
        };
        break;
      default:
        break;
    }

    unassignedVariables = new PriorityQueue<>(variableComparator);
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        char cval = line.charAt(9 * i + j);
        grid[i][j] = (cval == '.') ? 0 : (cval - '0');
        // if not assigned
        if (cval == '.') {
          unassignedVariables.add(new Variable(i, j));
        }
      }
    }
  }

  static Variable currVar;

  private boolean solve() {
    if (!isComplete()) {
      // take an unassigned var
      Variable var = unassignedVariables.poll();
      currVar = var;
      List<Integer> orderedValues = new ArrayList<>();
      for (int i = 0; i < 9; i++) {
        if (isConsistent(var, i + 1)) {
          orderedValues.add(i + 1);
        }
      }
      Collections.sort(orderedValues, valueComparator);
      for (int value : orderedValues) {
        if (isConsistent(var, value)) {
          grid[var.row][var.col] = value;
          boolean solved = solve();
          // check if found a solution
          if (solved) {
            return solved;
          }
          // remove var = i from assignment
          grid[var.row][var.col] = 0;
        }
      }
      // none of the values 1..9 worked put back this value as unassigned
      unassignedVariables.add(var);
      return false;
    } else {
      // assignment is complete
      return true;
    }
  }

  private boolean isConsistent(Variable var, int v) {
    // row check
    for (int j = 0; j < 9; j++) {
      if (j != var.col && grid[var.row][j] == v) {
        return false;
      }
    }
    // col check
    for (int i = 0; i < 9; i++) {
      if (i != var.row && grid[i][var.col] == v) {
        return false;
      }
    }
    // block check
    for (int i = 3 * (var.row / 3); i < 3 * (var.row / 3) + 3; i++) {
      for (int j = 3 * (var.col / 3); j < 3 * (var.col / 3) + 3; j++) {
        if (i != var.row && j != var.col && grid[i][j] == v) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean isComplete() {
    return unassignedVariables.size() == 0;
  }

  @Override
  public String toString() {
    String string = "";
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        string += grid[i][j];
      }
    }
    return string;
  }

  void prettyPrint() {

    System.out.println("\n+---+---+---+");

    for (int i = 0; i < 9; i++) {

      System.out.print("|");
      for (int j = 0; j < 9; j++) {

        System.out.print((grid[i][j] != 0) ? grid[i][j] : ".");
        if (j % 3 == 2) {
          System.out.print("|");
        }

      }
      if (i % 3 == 2) {
        System.out.print("\n+---+---+---+");
      }
      System.out.println();

    }
  }

  public static void main(String[] args) throws IOException {
    // arguments check
    int heuristic = -1;
    if (args.length < 3 || (heuristic = Integer.parseInt(args[2])) < 0 || heuristic > 3) {
      System.out.println("There should be three input arguments in the format:\n"
          + "\t<input file> <output file> <heuristic id>");
      System.out.println("\tHeuristic id: " + CASE_NONE + ". None, " + CASE_MRV + ". MRV, "
          + CASE_LCV + ". MRV+LCV, " + CASE_MAC + ". MRV+LCV+MAC");
      return;
    }
    // input and output

    BufferedReader in = new BufferedReader(new FileReader(args[0]));
    FileWriter fw = new FileWriter(args[1]);

    // reading file and solving, then printing it
    String line = null;
    long totalTime = 0;

    while ((line = in.readLine()) != null) {
      SudokuSolver sudoku = new SudokuSolver(line, heuristic);

      long start = System.currentTimeMillis();

      boolean solved = sudoku.solve();

      long end = System.currentTimeMillis();

      totalTime += end - start;

      if (!solved) {
        System.out.println("Could not solve: " + line);
        break;
      }

      fw.write(sudoku.toString() + "\n");
    }

    // time
    double seconds = totalTime / 1000.0;
    int minutes = (int) (seconds / 60);
    seconds -= 60 * minutes;
    System.out.println("Took " + minutes + " minute(s) " + seconds + " seconds.");

    // close input and output
    in.close();
    fw.close();

  }

}
