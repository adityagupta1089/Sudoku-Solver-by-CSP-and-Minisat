**Note:** Sample output can be found in `code/output/`
# 1. Sudoku Solver using CSP
## Running Instructions:

* Change Directory to `code/jar/`
* To run Sudoku Solver `java -jar SudokuSolver.jar <input-file> <output-file> <heuristic>`
	* `0` (No Heuristic, BS), `1` (MRV, BSI), `2` (MRV+LCV, BSII), `3` (MRV+LCV+MAC, BSMAC)
* **Note:** Requires jre version 1.8

## Source Files:
Source files can be found in `code/src/` and include:

* `SudokuSolver.java`
* `BSSudokuSolver.java`
* `BSISudokuSolver.java`
* `BSIISudokuSolver.java`
* `BSMACSudokuSolver.java`

# 2. Sudoku Solver using MiniSAT
## Running Instructions:

* Change Directory to `code/jar/`
* To run Sudoku Solver `java -jar SAT.jar <input-file> <output-file> <minisat_static_binary>`
* **Note #1:** Requires jre version 1.8
* **Note #2:** `minisat_static` provided in `code/jar/` folder might not work, you may need to compile on the system itself.

## Source Files:
Source files can be found in `code/src/` and include:

* `SAT.java`