import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BSIISudokuSolver extends BSISudokuSolver {

  public BSIISudokuSolver(final String line) {
    super(line);
  }

  @Override
  public boolean solve() {
    if (!this.isComplete()) {
      /* Select unassigned variable */
      final Variable currVar = this.unassignedVariables.stream()
          .min((v1, v2) -> this.compare(v1, v2)).get();
      this.unassignedVariables.remove(currVar);
      /* List for consistent values */
      final List<Integer> values = new ArrayList<>();
      /* Add all consistent values */
      for (int value = 1; value <= 9; value++) {
        if (this.isConsistent(currVar, value)) {
          values.add(value);
        }
      }
      /* Sort according to least constraining value */
      Collections.sort(values, (val1, val2) -> {
        /* **************************** Sorting *****************************/
        int cnt1 = 0;
        int cnt2 = 0;
        /* Try 1st value */
        this.setValue(currVar, val1);
        for (final Variable var1 : this.unassignedVariables) {
          if (this.isRelated(var1, currVar)) {
            for (int v1 = 1; v1 <= 9; v1++) {
              if (this.isConsistent(var1, v1)) {
                cnt1++;
              }
            }
          }
        }
        /* Try 2nd value */
        this.setValue(currVar, val2);
        for (final Variable var2 : this.unassignedVariables) {
          if (this.isRelated(var2, currVar)) {
            for (int v2 = 1; v2 <= 9; v2++) {
              if (this.isConsistent(var2, v2)) {
                cnt2++;
              }
            }
          }
        }
        /* Restore value */
        this.setValue(currVar, 0);
        /* Compare reverse */
        return Integer.compare(cnt2, cnt1);
        /* **************************** Sorting *****************************/
      });
      /* After sorting try each value */
      for (final int value : values) {
        this.setValue(currVar, value);
        /* Solve recursively */
        final boolean solved = this.solve();
        /* Found a solution */
        if (solved) {
          return solved;
        }
        /* No solution for this value, reset value */
        this.setValue(currVar, 0);
      }
      /* none of the values in the domain worked put back this value as unassigned, backtrack */
      this.unassignedVariables.add(currVar);
      backTracks++;
      return false;
    } else {
      return true;
    }
  }

}
