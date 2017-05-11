import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class SAT {

	private static final String MINISAT_INPUT_FILE = "minisat_input.txt";
	private static final String MINISAT_OUTPUT_FILE = "minisat_output.txt";

	private static void diff(final int i, final int j, final int i2, final int j2) throws IOException {
		/* The two variables (i, j) and (i2, j2) can't have same values */
		for (int k = 1; k <= 9; k++) {
			/* Constraint for each value */
			sb.append(String.format("c x[%d][%d]!=%d || x[%d][%d]!=%d \n", i, j, k, i2, j2, k));
			sb.append("-" + val(i, j, k) + " -" + val(i2, j2, k) + " 0\n");
			clauses++;

		}
	}

	/**
	 * Solves sudoku using MINISAT
	 * 
	 * @param args
	 *            <input-file> <output-file> <minisat-binary>
	 */
	public static void main(final String[] args) throws IOException, InterruptedException {
		/* input and output */
		final BufferedReader in = new BufferedReader(new FileReader(args[0]));

		/* reading file and solving, then printing it */
		String line = null;

		FileWriter fw = new FileWriter(args[1]);
		while ((line = in.readLine()) != null) {
			/* create file to be fed to MiNISAT */
			writeMiniSATInput(line);
			/* Run MINISAT */
			String[] args1 = new String[] { args[2], MINISAT_INPUT_FILE, MINISAT_OUTPUT_FILE };
			new ProcessBuilder(args1).start()
				.waitFor();
			/*Parse MINISAT output*/
			readMiniSAToutput(fw);
		}
		/* Close File Streams */
		in.close();
		fw.close();
	}

	private static void readMiniSAToutput(FileWriter fw) throws IOException {
		/* Read output of MINISAT */
		final BufferedReader in = new BufferedReader(new FileReader(MINISAT_OUTPUT_FILE));
		String line = null;
		if ((line = in.readLine()) != null) {
			/* If no solution found by MINISAT */
			if (line.contains("UNSAT")) {
				System.out.println("Could not solve!");
				in.close();
				return;
			}
		}
		if ((line = in.readLine()) != null) {

			fw.write(
					/*Parse the true booleans*/
					parse(
							/* Split booleans in the line */
							Arrays.stream(line.split(" "))
								/*Convert to int from string*/
								.mapToInt(Integer::parseInt)
								/*Take positive ones, because 0 is line ending and negative ones are false*/
								.filter(i -> i > 0)
								/*Convert to Array*/
								.toArray())
							+ "\n");
		}
		in.close();
	}

	private static String parse(int[] array) {
		int[][] grid = new int[9][9];
		/* Find inverse mapping of val, i.e. finding row, col, val s.t. val(row, col, val) = ind */
		for (int ind : array) {
			int val = (ind - 1) % 9 + 1;
			int col = (ind - val) / 9 % 9;
			int row = (ind - val) / 9 / 9;
			/* Implement this constraint */
			grid[row][col] = val;
		}
		/* Convert to row-rasterized form */
		String out = "";
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				out += grid[i][j];
			}
		}
		return out;
	}

	private static StringBuilder sb;
	private static int clauses = 0;

	private static void writeMiniSATInput(String line) throws IOException {
		/* Input file fed to minisat */
		final FileWriter fw = new FileWriter(MINISAT_INPUT_FILE);
		/* StriBuilder to write to that file */
		sb = new StringBuilder();
		/* Total clauses occured */
		clauses = 0;
		/* Initial Processing Grid */
		for (int r = 0; r < 9; r++) {
			for (int c = 0; c < 9; c++) {
				/* Check if filled Cell */
				if (line.charAt((9 * r) + c) != '.') {
					/* For filled cell only a fixed value possible */
					sb.append(String.format("c x[%d][%d]=%d\n", r, c, line.charAt((9 * r) + c) - '0'));
					sb.append(val(r, c, line.charAt((9 * r) + c) - '0') + " 0\n");
					clauses++;
				} else {
					/* For not filled cell, it can't have two different values v, v2 from 1 to 9 */
					for (int v = 1; v <= 9; v++) {
						for (int v2 = v + 1; v2 <= 9; v2++) {
							sb.append(String.format("c x[%d][%d]!=%d || x[%d][%d]!=%d \n", r, c, v, r, c, v2));
							sb.append("-" + val(r, c, v) + " -" + val(r, c, v2) + " 0\n");
							clauses++;
						}
					}
					/* But also the cell must have atleast one value out of 1..9 */
					String stringComment = "";
					String stringClauses = "";
					/* Value can be 1 or 2 ... */
					stringComment += String.format("c x[%d][%d]=%d || x[%d][%d]=%d ", r, c, 1, r, c, 2);
					stringClauses += val(r, c, 1) + " " + val(r, c, 2) + " ";
					/* ... or 3 to 9 */
					for (int v = 3; v <= 9; v++) {
						stringComment += String.format("|| x[%d][%d]=%d", r, c, v);
						stringClauses += val(r, c, v) + " ";
					}
					stringComment += " \n";
					stringClauses += " 0\n";
					/* Add these to string builder */
					sb.append(stringComment + stringClauses);
					clauses++;
				}
			}
		}
		/* Row-Column-Box Constraints */
		/* Cell 1 */
		for (int r1 = 0; r1 < 9; r1++) {
			for (int c1 = 0; c1 < 9; c1++) {
				/* Cell 2 */
				for (int r2 = 0; r2 < 9; r2++) {
					for (int c2 = 0; c2 < 9; c2++) {
						/* unordered pairs, so WLOG assume r1<=r2 & c1<=c2, also both different */
						if ((r1 > r2) || (c1 > c2) || (r1 == r2 && c1 == c2)) {
							continue;
						}
						/* If they have same row or column or same box */
						if ((r1 == r2) || (c1 == c2) || (((r1 / 3) == (r2 / 3)) && ((c1 / 3) == (c2 / 3)))) {
							diff(r1, c1, r2, c2);
						}
					}
				}
			}
		}
		/* Preamble (729 = 9 x 9 x 9) */
		fw.write("p cnf " + 729 + " " + clauses + "\n");
		/* Remaining clauses and comments */
		fw.write(sb.toString());
		fw.close();
	}

	/**
	 * Returns corresponding boolean constraint index from row, cell and value,
	 * i.e. {@code index = 81 x row + 9 x col + val}, then X<sub>index</sub> =
	 * ({@code grid[row][col] == value}).
	 */
	private static int val(final int i, final int j, final int v) {
		/* Bijective map from {0..8}x{0..8}x{1..9} to {1..729} */
		return (9 * ((9 * i) + j)) + v;
	}
}
