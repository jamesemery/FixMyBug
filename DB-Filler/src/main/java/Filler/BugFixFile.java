
package Filler;

public class BugFixFile {
  public String bug;
  public String fix;
  public int startLine;

  public BugFixFile(String bugCode, String fixCode, int line) {
    bug = bugCode;
    fix = fixCode;
    startLine = line;
  }
  /**
   * Construct via the string data that returns from blackbox.
   */
  public BugFixFile(String[] dataFields) {
    startLine = Integer.parseInt(dataFields[0]);
    bug = dataFields[1];
    fix = dataFields[2];
  }

}
