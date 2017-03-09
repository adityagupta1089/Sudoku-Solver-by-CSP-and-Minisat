import java.util.HashSet;
import java.util.Set;

public class BSISudokuSolver extends SudokuSolver {

  public final Set<Variable> unassignedVariables;

  public BSISudokuSolver(final String line) {
    super(line);
    this.unassignedVariables = new HashSet<>(super.unassignedVariables);
  }

  @Override
  public boolean solve() {
    if (!this.isComplete()) {
      final Variable var = this.unassignedVariables.stream().min((v1, v2) -> this.compare(v1, v2))
          .get();
      this.unassignedVariables.remove(var);
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
      this.unassignedVariables.add(var);
      backTracks++;
      return false;
    } else {
      return true;
    }
  }

  public int compare(final Variable v1, final Variable v2) {
    int rv1 = 0;
    int rv2 = 0;
    for (int i = 1; i <= 9; i++) {
      if (this.isConsistent(v1, i)) {
        rv1++;
      }
      if (this.isConsistent(v2, i)) {
        rv2++;
      }
    }
    if (Integer.compare(rv1, rv2) != 0) {
      return Integer.compare(rv1, rv2);
    } else {
      return Integer.compare(this.degree(v2), this.degree(v1));
    }
  }

  public int degree(final Variable v) {
    int deg = 0;
    for (final Variable var : this.unassignedVariables) {
      if (this.isRelated(var, v)) {
        deg++;
      }
    }
    return deg;
  }

  @Override
  public boolean isComplete() {
    return this.unassignedVariables.size() == 0;
  }
}
