/**
 * Data storage for a bug-fix pair.
 **/
package Filler;

public class BugFix {

  public int bugMasterId = 0;
  public int fixMasterId = 0;
  public int sourceFileId = 0;
  public int startLine = 0;

  public BugFix(int bugId, int fixId, int fileId, int line) {
    bugMasterId = bugId;
    fixMasterId = fixId;
    sourceFileId = fileId;
    startLine = line;
  }
}
