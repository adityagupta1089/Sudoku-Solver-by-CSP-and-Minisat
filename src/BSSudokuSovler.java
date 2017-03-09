import java.util.LinkedList;

public class BSSudokuSovler extends SudokuSolver {

  LinkedList<Variable> unassignedVariables;

  public BSSudokuSovler(final String line) {
    super(line);
    unassignedVariables = new LinkedList<>(super.unassignedVariables);
  }

  @Override
  public boolean solve() {
    if (!isComplete()) {
      final Variable var = unassignedVariables.removeFirst();
      for (int value = 1; value <= 9; value++)
        if (isConsistent(var, value)) {
          setValue(var, value);
          final boolean solved = solve();
          if (solved)
            return solved;
          setValue(var, 0);
        }
      /* none of the values in the domain worked put back this value as unassigned */
      unassignedVariables.addFirst(var);
      backTracks++;
      return false;
    } else
      return true;
  }

  @Override
  public boolean isComplete() {
    return unassignedVariables.size() == 0;
  }

}
