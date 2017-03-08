import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class SudokuSolver {
  /**
   * Compares variables based on which occurs earlier in row-rasterized form.
   * 
   * @param var1
   *          First variable
   * 
   * @param var2
   *          Second variable
   */
  private class DefaultValueComparator implements Comparator<Variable> {
    @Override
    public int compare(Variable v1, Variable v2) {
      return v1.row == v2.row ? Integer.compare(v1.col, v2.col) : Integer.compare(v1.row, v2.row);
    }
  }

  /**
   * Compares values based on which allows most consistent values for other variables that are
   * related to the current variable {@code currVar}.
   * 
   * @param val1
   *          First Value
   * 
   * @param val2
   *          Second Value
   */
  private class LeastConstrainingValueCompartaor implements Comparator<Integer> {
    @Override
    public int compare(Integer val1, Integer val2) {
      int cnt1 = 0;
      int cnt2 = 0;
      /* Try 1st value */
      grid[currVar.row][currVar.col] = val1;
      for (Variable var : currVar.unassignedNeighbours()) {
        for (int v : domains.get(var)) {
          if (isConsistent(var, v)) {
            cnt1++;
          }
        }
      }
      /* Try 2nd value */
      grid[currVar.row][currVar.col] = val2;
      for (Variable var : currVar.unassignedNeighbours()) {
        for (int v : domains.get(var)) {
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
  }

  /**
   * Compares variables based on which has most consistent values possible in the current
   * assignment.
   * 
   * @param var1
   *          First variable
   * 
   * @param var2
   *          Second variable
   */
  private class MinmumRemainingValueComparator implements Comparator<Variable> {
    @Override
    public int compare(Variable v1, Variable v2) {
      int rv1 = 0;
      int rv2 = 0;
      for (int i : domains.get(v1)) {
        if (isConsistent(v1, i)) {
          rv1++;
        }
      }
      for (int i : domains.get(v2)) {
        if (isConsistent(v2, i)) {
          rv2++;
        }
      }
      return Integer.compare(rv1, rv2);
    }
  }

  /**
   * Variable class represents the positions in the Sudoku-grid, which starts from top-left at (0,0)
   * to (8,8) at bottom-right corner.
   */
  private class Variable {
    int row;
    int col;

    public Variable(int prow, int pcol) {
      row = prow;
      col = pcol;
    }

    @Override
    public boolean equals(Object obj) {
      Variable other = (Variable) obj;
      if (col != other.col || row != other.row) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + col;
      result = prime * result + row;
      return result;
    }

    @Override
    public String toString() {
      return "(" + row + ", " + col + ")";
    }

    /** Returns a list of unassigned Neighbours, i.e. which can't have same value as this. */
    public Set<Variable> unassignedNeighbours() {
      Set<Variable> neighbours = new HashSet<>();
      /* row */
      for (int j = 0; j < 9; j++) {
        if (j != this.col && grid[this.row][j] == 0) {
          neighbours.add(new Variable(this.row, j));
        }
      }
      /* column */
      for (int i = 0; i < 9; i++) {
        if (i != this.row && grid[i][this.col] == 0) {
          neighbours.add(new Variable(i, this.col));
        }
      }
      /* block */
      for (int i = 3 * (this.row / 3); i < 3 * (this.row / 3) + 3; i++) {
        for (int j = 3 * (this.col / 3); j < 3 * (this.col / 3) + 3; j++) {
          if (i != this.row && j != this.col && grid[i][j] == 0) {
            neighbours.add(new Variable(i, j));
          }
        }
      }
      return neighbours;
    }

  }

  /** A Pair of Variables. */
  private class VariablesPair {
    public Variable var1;
    public Variable var2;

    public VariablesPair(Variable v1, Variable v2) {
      var1 = v1;
      var2 = v2;
    }

    @Override
    public String toString() {
      return "(" + var1 + ", " + var2 + ")";
    }

  }

  private static final int CASE_NONE = 0;
  private static final int CASE_MRV = 1;
  private static final int CASE_LCV = 2;
  private static final int CASE_MAC = 3;

  /* The last assigned variable */
  private static Variable currVar;

  private static Map<Variable, Set<Integer>> domains;

  /**
   * This does the following:
   * <ul>
   * <li>Parses arguments for heuristic and input-output files.</li>
   * <li>Reads the files and for each line, solves it and prints it to the output file</li>
   * </ul>
   * 
   * @param args
   *          <ul>
   *          <li>Input File</li>
   *          <li>Output File</li>
   *          <li>Heuristic Id
   *          <ul>
   *          <li>0: None</li>
   *          <li>1: MRV</li>
   *          <li>2: MRV+LCV</li>
   *          <li>3: MRV+LCV+MAC</li>
   *          </ul>
   *          </li>
   *          </ul>
   * @throws IOException
   *           if input-output file could not be opened or closed.
   */
  public static void main(String[] args) throws IOException {
    /* arguments check */
    int heuristic = -1;
    if (args.length < 3 || (heuristic = Integer.parseInt(args[2])) < 0 || heuristic > 3) {
      System.out.println("There should be three input arguments in the format:\n"
          + "\t<input file> <output file> <heuristic id>");
      System.out.println("\tHeuristic id: " + CASE_NONE + ". None, " + CASE_MRV + ". MRV, "
          + CASE_LCV + ". MRV+LCV, " + CASE_MAC + ". MRV+LCV+MAC");
      return;
    }

    /* input and output */
    BufferedReader in = new BufferedReader(new FileReader(args[0]));
    FileWriter fw = new FileWriter(args[1]);

    /* reading file and solving, then printing it */
    String line = null;

    long t0 = System.currentTimeMillis();

    while ((line = in.readLine()) != null) {
      SudokuSolver sudoku = new SudokuSolver(line, heuristic);
      boolean solved = sudoku.solve();
      if (!solved) {
        System.out.println("Could not solve: " + line);
        break;
      }
      fw.write(sudoku.toSimpleString() + "\n");
    }
    System.out.println((double) (System.currentTimeMillis() - t0) / 1000);

    /* close input and output */
    in.close();
    fw.close();

  }

  private int[][] grid;

  private PriorityQueue<Variable> unassignedVariables;

  private Comparator<Integer> valueComparator;

  private boolean caseMaintainingArcConsistency = false;

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
   * @param heuristic
   *          The heuristic to be used:
   *          <ul>
   *          <li>0: None</li>
   *          <li>1: Minimum Remaining Value(MRV) Heuristic, Most constrained variable/fail-first
   *          heuristic.</li>
   *          <li>2: Least Constraining Value (LCV) Heuristic, One that rules out the fewest choices
   *          for the remaining variables. Includes MRV.</li>
   *          <li>3: Maintaining Arc Consistency (MAC), Start with only the arcs (X<sub>i</sub> ,
   *          X<sub>j</sub> ) for all X [ that are unassigned variables that are neighbors of
   *          X<sub>i</sub>. Proceed with AC-3 (Arc-Consistency) as usual to propagate the
   *          constraints. If any variable has its domain reduced to &phi;, AC-3 returns a
   *          failure.</li>
   *          </ul>
   */
  public SudokuSolver(String line, int heuristic) {
    grid = new int[9][9];
    Comparator<Variable> variableComparator = null;

    /* Variable Comparator */
    switch (heuristic) {
      case CASE_NONE:
        variableComparator = new DefaultValueComparator();
        break;
      case CASE_MRV:
      case CASE_LCV:
      case CASE_MAC:
        variableComparator = new MinmumRemainingValueComparator();
        break;
      default:
        break;
    }

    /* Variable Comparator */
    switch (heuristic) {
      case CASE_NONE:
      case CASE_MRV:
        valueComparator = Integer::compare;
        break;
      case CASE_LCV:
      case CASE_MAC:
        valueComparator = new LeastConstrainingValueCompartaor();
        break;
      default:
        break;
    }
    unassignedVariables = new PriorityQueue<>(variableComparator);
    /* Parse Line */
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        /* Value at each cell */
        char cval = line.charAt(9 * i + j);
        grid[i][j] = (cval == '.') ? 0 : (cval - '0');
        /* if not assigned */
        if (cval == '.') {
          unassignedVariables.add(new Variable(i, j));
        }
      }
    }
    /* Case MAC heuristic */
    caseMaintainingArcConsistency = (heuristic == 3);
    /* Initialize domains */
    domains = new HashMap<>();
    for (Variable var : unassignedVariables) {
      Set<Integer> domain = new HashSet<>();
      for (int i = 1; i <= 9; i++) {
        if (isConsistent(var, i)) {
          domain.add(i);
        }
      }
      domains.put(var, domain);
    }
  }

  /** Returns whether the Sudoku has been solved. */
  private boolean isComplete() {
    /* Our assignment is always legal so if there are no unassigned variables we are good to go. */
    return unassignedVariables.size() == 0;
  }

  /**
   * Checks whether a values is consistent with the current assignment for a given variable.
   * 
   * @param var
   *          is the given variable.
   * @param v
   *          is the value to check for.
   */
  private boolean isConsistent(Variable var, int v) {
    /* row check */
    for (int j = 0; j < 9; j++) {
      if (j != var.col && grid[var.row][j] == v) {
        return false;
      }
    }
    /* column check */
    for (int i = 0; i < 9; i++) {
      if (i != var.row && grid[i][var.col] == v) {
        return false;
      }
    }
    /* block check */
    for (int i = 3 * (var.row / 3); i < 3 * (var.row / 3) + 3; i++) {
      for (int j = 3 * (var.col / 3); j < 3 * (var.col / 3) + 3; j++) {
        if (i != var.row && j != var.col && grid[i][j] == v) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Finds a solution, no solution or does nothing.
   * 
   * @param varI
   *          The currently assigned variable with which all arcs thus so formed with all other
   *          unassigned variables must not lead to empty domain upon AC-3.
   */
  private boolean maintainArcConsistency(Variable varI) {
    /* Initialize arcs and domains */
    Queue<VariablesPair> arcs = new LinkedList<>();
    Map<Variable, Set<Integer>> domains = new HashMap<>();

    /* Add all arcs containing "var",i.e. (varNeighbour, var) */
    for (Variable varJ : varI.unassignedNeighbours()) {
      arcs.add(new VariablesPair(varJ, varI));
    }

    /* Proceed with AC-3 */
    while (!arcs.isEmpty()) {
      /* Remove pair (varNeighbour, var) */
      VariablesPair pair = arcs.poll();
      /* Check if pair.val1, i.e. varNeightbour is arc consistent with pair.val2, i.e. var */
      if (reviseDomains(pair.var1, pair.var2)) {
        /* Revised some domains */
        for (Variable varK : varI.unassignedNeighbours()) {
          if (!varK.equals(pair.var2)) {
            /* Add arcs that have related variables */
            arcs.add(new VariablesPair(varK, pair.var1));
          }
        }
      }
    }

    /* Check any empty domain */
    for (Set<Integer> s : domains.values()) {
      if (s.isEmpty()) {
        return false;
      }
    }

    /* No empty domain */
    return true;
  }

  /**
   * Removes values in the domain of a variable that do not satisfy the binary constraints on the
   * variable, i.e. if they are related (lie in same row, column or box) they must not have same
   * values.
   * </p>
   * 
   * @param pair
   *          The pair ({@code var1},{@code var2}) that must be arc-consistent.
   */
  private boolean reviseDomains(Variable varI, Variable varJ) {
    boolean revised = false;
    List<Integer> toRemove = new ArrayList<>();
    for (int x : domains.get(varI)) {
      /* Temporarily assign the value x */
      grid[varI.row][varI.col] = x;
      /* Assume no value satisfies contraints */
      boolean anySatisfy = false;
      for (int y : domains.get(varJ)) {
        if (isConsistent(varJ, y)) {
          /* We found a consistent value */
          anySatisfy = true;
          break;
        }
      }
      /* Restore value */
      grid[varI.row][varI.col] = 0;
      /* If no value in domain of var2 that satisfies for var1. */
      if (!anySatisfy) {
        toRemove.add(x);
        revised = true;
      }
    }
    for (int rem : toRemove) {
      domains.get(varI).remove(rem);
    }
    return revised;
  }

  /**
   * Until the assignment is complete, takes a new unassigned variable according to the heuristic
   * and tries to solve using recursion with all consistent values in its domain sorted according to
   * the heuristic, putting it (the variable) back if it is unsolvable for all consistent values in
   * its domain.
   */
  private boolean solve() {
    if (!isComplete()) {
      /* Takes an unassigned variable */
      Variable var = unassignedVariables.poll();
      currVar = var;
      /* Initializes domain of variables */
      List<Integer> orderedValues = new ArrayList<>();
      orderedValues.addAll(domains.get(var));
      /* Sorts values based on comparator decided by heuristic */
      Collections.sort(orderedValues, valueComparator);
      /* Loop over each value in domain */
      for (int value : orderedValues) {
        if (isConsistent(var, value)) {
          grid[var.row][var.col] = value;
          if (caseMaintainingArcConsistency) {
            if (!maintainArcConsistency(var)) {
              return false;
            }
          }
          boolean solved = solve();
          /* check if found a solution */
          if (solved) {
            return solved;
          }
          /* remove variable = i from assignment */
          grid[var.row][var.col] = 0;
        }
      }
      /* none of the values 1..9 worked put back this value as unassigned */
      unassignedVariables.add(var);
      // TODO Maybe update domains
      return false;
    } else {
      /* assignment is complete */
      return true;
    }
  }

  /** Prints the Sudoku. */
  public String toSimpleString() {
    String string = "";
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        string += grid[i][j];
      }
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
        string += (grid[i][j] != 0) ? grid[i][j] : ".";
        /* Box-Separator */
        if (j % 3 == 2) {
          string += "|";
        }
      }
      /* Box-Separator */
      if (i % 3 == 2) {
        string += "\n +---+---+---+";
      }
      /* Next row */
      string += "\n";
    }
    return string;

  }

}
