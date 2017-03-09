import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BSIISudokuSolver extends BSISudokuSolver {

  public BSIISudokuSolver(final String line) {
    super(line);
  }

  @Override
  public boolean solve() {
    if (!isComplete()) {
      final Variable currVar = unassignedVariables.stream().min((v1, v2) -> compare(v1, v2)).get();
      unassignedVariables.remove(currVar);
      final List<Integer> values = new ArrayList<>();
      for (int value = 1; value <= 9; value++)
        if (isConsistent(currVar, value))
          values.add(value);
      Collections.sort(values, (val1, val2) -> {
        int cnt1 = 0;
        int cnt2 = 0;
        /* Try 1st value */
        setValue(currVar, val1);
        for (final Variable var1 : unassignedVariables)
          if (isRelated(var1, currVar))
            for (int v1 = 1; v1 <= 9; v1++)
              if (isConsistent(var1, v1))
                cnt1++;
        /* Try 2nd value */
        setValue(currVar, val2);
        for (final Variable var2 : unassignedVariables)
          if (isRelated(var2, currVar))
            for (int v2 = 1; v2 <= 9; v2++)
              if (isConsistent(var2, v2))
                cnt2++;
        /* Restore value */
        setValue(currVar, 0);
        /* Compare reverse */
        return Integer.compare(cnt2, cnt1);
      });
      for (final int value : values) {
        setValue(currVar, value);
        final boolean solved = solve();
        if (solved)
          return solved;
        setValue(currVar, 0);
      }
      /* none of the values in the domain worked put back this value as unassigned */
      unassignedVariables.add(currVar);
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
