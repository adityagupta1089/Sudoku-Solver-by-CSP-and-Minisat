import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BSMACSudokuSolver extends BSIISudokuSolver {

	Map<Variable, Set<Integer>> domains;

	/** Utility function to clone domains, for passing down recursion. */
	private Map<Variable, Set<Integer>> domainClone(Map<Variable, Set<Integer>> domainOriginal) {
		Map<Variable, Set<Integer>> domainsCopy = new HashMap<>();
		for (Variable v : domainOriginal.keySet()) {
			domainsCopy.put(v, new HashSet<Integer>(domainOriginal.get(v)));
		}
		return domainsCopy;
	}

	/** Initializes domains and values. */
	public BSMACSudokuSolver(final String line) {
		super(line);
		this.domains = new HashMap<>();

		for (final Variable var : this.unassignedVariables) {
			this.domains.put(var, new HashSet<>());
			for (int i = 1; i <= 9; i++) {
				if (this.isConsistent(var, i)) {
					this.domains.get(var)
						.add(i);
				}
			}
		}
	}

	@Override
	public boolean solve() {
		return solve(domains);
	}

	private boolean solve(Map<Variable, Set<Integer>> currDomain) {
		if (!this.isComplete()) {
			/* Select unassigned variable */
			final Variable currVar = this.unassignedVariables.stream()
				.min((v1, v2) -> this.compare(v1, v2))
				.get();
			this.unassignedVariables.remove(currVar);
			/* List for consistent values */
			final List<Integer> values = new ArrayList<>();
			/* Add all consistent values */
			for (int value : currDomain.get(currVar)) {
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
						for (final int v1 : currDomain.get(var1)) {
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
						for (final int v2 : currDomain.get(var2)) {
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
				/* Clone domains to pass down recursion */
				Map<Variable, Set<Integer>> newDomains = domainClone(currDomain);
				/* Domain of assigned variable has reduced and is just the value we assigned */
				newDomains.put(currVar, new HashSet<>());
				newDomains.get(currVar)
					.add(value);
				/* Maintain Arc-Consistency */
				this.maintainArcConsistency(currVar, newDomains);
				/* If graph is arc-consistent */
				if (newDomains != null) {
					/* Solve recursively using new domains */
					final boolean solved = this.solve(newDomains);
					/* Found a solution */
					if (solved) {
						return solved;
					}
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

	@Override
	public int compare(final Variable v1, final Variable v2) {
		/* Remaining values for variables */
		int rv1 = 0;
		int rv2 = 0;
		/* Loop over all values in domain */
		for (final int i : this.domains.get(v1)) {
			if (this.isConsistent(v1, i)) {
				/* Count consistent values */
				rv1++;
			}
		}
		/* Loop over all values in domain */
		for (final int i : this.domains.get(v2)) {
			if (this.isConsistent(v2, i)) {
				/* Count consistent values */
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
	 * Maintains arc consistency and returns reduced domains if no domain
	 * reduced to &Phi; else returns {@code null}.
	 *
	 * @param varI
	 *            The currently assigned variable with which all arcs thus so
	 *            formed with all other unassigned variables must not lead to
	 *            empty domain upon AC3.
	 * @param currDomains
	 *            The copy of current domains
	 */

	private Map<Variable, Set<Integer>> maintainArcConsistency(final Variable varI,
			Map<Variable, Set<Integer>> currDomains) {
		/* Initialize arcs and domains */
		final Queue<VariablesPair> arcs = new LinkedList<>();

		/* Add all arcs containing "var",i.e. (varNeighbour, var) */
		for (final Variable varJ : this.unassignedVariables) {
			if (this.isRelated(varJ, varI)) {
				arcs.add(new VariablesPair(varJ, varI));
			}
		}

		/* Proceed with AC3 */
		while (!arcs.isEmpty()) {
			/* Remove pair (varNeighbour, var) */
			final VariablesPair pair = arcs.poll();
			/* Check if pair.val1, i.e. varNeightbour is arc consistent with pair.val2, i.e. var */
			if (this.reviseDomains(pair.var1, pair.var2, currDomains)) {
				/* Revised some domains */
				for (final Variable varK : this.unassignedVariables) {
					if (this.isRelated(varK, pair.var1)) {
						if (!varK.equals(pair.var2)) {
							/* Add arcs that have related variables */
							arcs.add(new VariablesPair(varK, pair.var1));
						}
					}
				}
			}
		}

		/* Check any empty domain */
		for (final Set<Integer> s : currDomains.values()) {
			if (s.isEmpty()) {
				return null;
			}
		}

		/* No empty domain */
		return currDomains;
	}

	/**
	 * Removes values in the domain of a variable that do not satisfy the binary
	 * constraints on the variable, i.e. if they are related (lie in same row,
	 * column or box) they must not have same values.
	 * 
	 * @param currDomains
	 *            The reference to domains that needs to be revised
	 *
	 * @param varI
	 *            (varI,varJ) should be arc consistent.
	 * @param varJ
	 *            (varI,varJ) should be arc consistent.
	 */
	private boolean reviseDomains(final Variable varI, final Variable varJ, Map<Variable, Set<Integer>> currDomains) {
		/* No domains revised yet */
		boolean revised = false;
		final List<Integer> toRemove = new ArrayList<>();
		for (final int x : currDomains.get(varI)) {
			/* Temporarily assign the value x */
			this.setValue(varI, x);
			/* Assume no value satisfies contraints */
			boolean anySatisfy = false;
			for (final int y : currDomains.get(varJ)) {
				if (this.isConsistent(varJ, y)) {
					/* We found a consistent value */
					anySatisfy = true;
					break;
				}
			}
			/* Restore value */
			this.setValue(varI, 0);
			/* If no value in domain of var2 that satisfies for var1. */
			if (!anySatisfy) {
				toRemove.add(x);
				revised = true;
			}
		}
		/* Remove values from domain of x (avoids ConcurrentModificationException) */
		for (final int rem : toRemove) {
			currDomains.get(varI)
				.remove(rem);
		}
		return revised;
	}
}
