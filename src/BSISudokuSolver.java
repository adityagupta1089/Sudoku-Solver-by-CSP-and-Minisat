import java.util.HashSet;
import java.util.Set;

public class BSISudokuSolver extends SudokuSolver {

  public final Set<Variable> unassignedVariables;

  /** Initializes set of unassigned variables. */
  public BSISudokuSolver(final String line) {
    super(line);
    /* Easy removal, O(1), of values after finding min variable */
    this.unassignedVariables = new HashSet<>(super.unassignedVariables);
  }

  @Override
  public boolean solve() {
    if (!this.isComplete()) {
      /* Select unassigned variable */
      final Variable var = this.unassignedVariables.stream().min((v1, v2) -> this.compare(v1, v2))
          .get();
      this.unassignedVariables.remove(var);
      /* Check all values in domain */
      for (int value = 1; value <= 9; value++) {
        if (this.isConsistent(var, value)) {
          /* Try this value */
          this.setValue(var, value);
          /* Solve recursively */
          final boolean solved = this.solve();
          /* Found a solution */
          if (solved) {
            return solved;
          }
          /* No solution for this value, reset value */
          this.setValue(var, 0);
        }
      }
      /* none of the values in the domain worked put back this value as unassigned, backtrack */
      this.unassignedVariables.add(var);
      backTracks++;
      return false;
    } else {
      return true;
    }
  }

  /** Compares two variables according to minimum remaining values. */
  public int compare(final Variable v1, final Variable v2) {
    /* Remaining values for variables */
    int rv1 = 0;
    int rv2 = 0;
    /* Loop over all values in domain */
    for (int i = 1; i <= 9; i++) {
      /* Count consistent values of both */
      if (this.isConsistent(v1, i)) {
        rv1++;
      }
      if (this.isConsistent(v2, i)) {
        rv2++;
      }
    }
    /* If one has less consistent values, that is smaller */
    if (Integer.compare(rv1, rv2) != 0) {
      return Integer.compare(rv1, rv2);
    } else {
      /* Both have same consistent values, count degress with other unassigned variables */
      return Integer.compare(this.degree(v2), this.degree(v1));
    }
  }

  /**
   * Finds degree of a variables, i.e. the number of constraints this variables has with other
   * unassigned variables.
   */
  public int degree(final Variable currVar) {
    /* Initially degree is 0 */
    int deg = 0;
    /* For each variable check if they have a constraint */
    for (final Variable var : this.unassignedVariables) {
      if (this.isRelated(var, currVar)) {
        deg++;
      }
    }
    return deg;
  }

  @Override
  public boolean isComplete() {
    /*
     * If no unassigned variable is remaining, then since our assignment is always legal, it is
     * complete
     */
    return this.unassignedVariables.size() == 0;
  }
}
