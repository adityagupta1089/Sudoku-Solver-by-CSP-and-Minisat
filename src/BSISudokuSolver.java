import java.util.HashSet;
import java.util.Set;

public class BSISudokuSolver extends SudokuSolver {

  public final Set<Variable> unassignedVariables;

  public BSISudokuSolver(final String line) {
    super(line);
    unassignedVariables = new HashSet<>(super.unassignedVariables);
  }

  @Override
  public boolean solve() {
    if (!isComplete()) {
      final Variable var = unassignedVariables.stream().min((v1, v2) -> compare(v1, v2)).get();
      unassignedVariables.remove(var);
      for (int value = 1; value <= 9; value++)
        if (isConsistent(var, value)) {
          setValue(var, value);
          final boolean solved = solve();
          if (solved)
            return solved;
          setValue(var, 0);
        }
      /* none of the values in the domain worked put back this value as unassigned */
      unassignedVariables.add(var);
      backTracks++;
      return false;
    } else
      return true;
  }

  public int compare(final Variable v1, final Variable v2) {
    int rv1 = 0;
    int rv2 = 0;
    for (int i = 1; i <= 9; i++) {
      if (isConsistent(v1, i))
        rv1++;
      if (isConsistent(v2, i))
        rv2++;
    }
    if (Integer.compare(rv1, rv2) != 0)
      return Integer.compare(rv1, rv2);
    else
      return Integer.compare(degree(v2), degree(v1));
  }

  public int degree(final Variable v) {
    int deg = 0;
    for (final Variable var : unassignedVariables)
      if (isRelated(var, v))
        deg++;
    return deg;
  }

  @Override
  public boolean isComplete() {
    return unassignedVariables.size() == 0;
  }
}
