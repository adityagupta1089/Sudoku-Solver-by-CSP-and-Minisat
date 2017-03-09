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
      final Variable var = this.unassignedVariables.removeFirst();
      for (int value = 1; value <= 9; value++) {
        if (this.isConsistent(var, value)) {
          this.setValue(var, value);
          final boolean solved = this.solve();
          if (solved) {
            return solved;
          }
          this.setValue(var, 0);
        }
      }
      /* none of the values in the domain worked put back this value as unassigned */
      this.unassignedVariables.addFirst(var);
      backTracks++;
      return false;
    } else {
      return true;
    }
  }

  @Override
  public boolean isComplete() {
    return this.unassignedVariables.size() == 0;
  }

}
