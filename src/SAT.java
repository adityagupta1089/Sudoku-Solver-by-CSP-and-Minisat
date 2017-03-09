import java.io.FileWriter;
import java.io.IOException;

public class SAT {
  static String s;
  static int cl = 0;

  static int max = 0;

  private static void diff(final int i, final int j, final int i2, final int j2)
      throws IOException {
    for (int k = 1; k <= 9; k++) {
      s += String.format("c x[%d][%d]!=%d || x[%d][%d]!=%d \n", i, j, k, i2, j2, k);
      s += "-" + val(i, j, k) + " -" + val(i2, j2, k) + " 0\n";
      cl++;

    }
  }

  public static void main(final String[] args) throws IOException {
    final String p = "............8.5.492...6.3.1..9..........21.38...3.......5........6..48..13...96.2";
    final FileWriter fw = new FileWriter("out.txt");
    s = "";
    for (int r = 0; r < 9; r++)
      for (int c = 0; c < 9; c++)
        if (p.charAt(9 * r + c) != '.') {
          s += String.format("c x[%d][%d]=%d\n", r, c, p.charAt(9 * r + c) - '0');
          s += val(r, c, p.charAt(9 * r + c) - '0') + " 0\n";
          cl++;
        } else {
          for (int v = 1; v <= 9; v++)
            for (int v2 = v + 1; v2 <= 9; v2++) {
              s += String.format("c x[%d][%d]!=%d || x[%d][%d]!=%d \n", r, c, v, r, c, v2);
              s += "-" + val(r, c, v) + " -" + val(r, c, v2) + " 0\n";
              cl++;
            }
          String s1 = "", s2 = "";
          s1 += String.format("c x[%d][%d]=%d || x[%d][%d]=%d ", r, c, 1, r, c, 2);
          s2 += val(r, c, 1) + " " + val(r, c, 2) + " ";
          for (int v = 3; v <= 9; v++) {
            s1 += String.format("|| x[%d][%d]=%d", r, c, v);
            s2 += val(r, c, v) + " ";
          }
          s1 += " \n";
          s2 += " 0\n";
          s += s1 + s2;
          cl++;
        }
    for (int r1 = 0; r1 < 9; r1++)
      for (int c1 = 0; c1 < 9; c1++)
        for (int r2 = 0; r2 < 9; r2++)
          for (int c2 = 0; c2 < 9; c2++) {
            if (r1 >= r2 || c1 >= c2)
              continue;
            if (r1 == r2 || c1 == c2 || r1 / 3 == r2 / 3 && c1 / 3 == c2 / 3)
              diff(r1, c1, r2, c2);
          }
    fw.write("p cnf " + max + " " + cl + "\n");
    fw.write(s);
    fw.close();
    Runtime.getRuntime().exec("/bin/bash -c minisat out.txt out3.txt");
  }

  private static int val(final int i, final int j, final int v) {
    final int val = 9 * (9 * i + j) + v;
    max = Math.max(max, val);
    return val;
  }
}
