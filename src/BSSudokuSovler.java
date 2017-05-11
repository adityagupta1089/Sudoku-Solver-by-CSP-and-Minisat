import java.util.LinkedList;

public class BSSudokuSovler extends SudokuSolver {

  LinkedList<Variable> unassignedVariables;

  public BSSudokuSovler(final String line) {
    super(line);
    this.unassignedVariables = new LinkedList<>(super.unassignedVariables);
  }

  @Override
  public boolean solve() {
    if (!this.isComplete()) {
      /* Select unassigned variable */
      final Variable var = this.unassignedVariables.removeFirst();
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
      this.unassignedVariables.addFirst(var);
      backTracks++;
      return false;
    } else {
      /* Complete assignment */
      return true;
    }
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
