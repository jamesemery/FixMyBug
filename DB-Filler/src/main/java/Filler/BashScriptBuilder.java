// Initial stab at getting multiple source files at a time
package Filler;

import com.jcraft.jsch.*;
import java.io.*;
import java.sql.*;
import java.util.LinkedList;


public class BashScriptBuilder {

  public static String DELIMITER = "--CARLETON-COMPS-2017-BUGDATA--"; // separates between pairs
  public static String DELIMITER2 = "--CARLETON-COMPS-2017-SUBDELIMITER--"; // separates bug code, source code, and startLine

    public LinkedList<BugFix> bugFixIds = null;
    public int bufferSize = 0;
    public Session ssh_session;

    /**
     * Constructs a new SourceCodeGrabber, with a queue of the designated size.
     */
    public BashScriptBuilder(int bufferSize, Session session) {
        this.bufferSize = bufferSize;
        ssh_session = session;
        bugFixIds = new LinkedList<BugFix>();
    }

    /**
     * Adds a bug-fix pair to the queue, if there's room. Returns false if
     * the queue was full (as determined by bufferSize)
     */
    public boolean addToQueue(BugFix data) {
        //System.out.println("adding to queue - size " + bugFixIds.size() + ", bufferSize "+ bufferSize);
        if (bugFixIds.size() >= bufferSize) return false;
        try {
          bugFixIds.addLast(data);
          //System.out.println("new size " + bugFixIds.size());
          return true;
        }
        catch (Exception e) {
            System.out.println("Exception caught adding to queue");
            return false;
        }
    }

    /**
     * Empties the queue, and generates the executable command for the ids.
     */
    public String generateBashScript() {
      StringBuilder script = new StringBuilder();
      String command = "";
      while (!bugFixIds.isEmpty()) {
        BugFix curr_ids = bugFixIds.remove();
        String bug_header = "echo \"" + DELIMITER + curr_ids.startLine + DELIMITER2 +"\";\n";

        String fix_header = "echo \"" + DELIMITER2 + "\";\n";

        String bug_command = "timeout 2s /tools/nccb/bin/print-compile-input /data/compile-inputs " +
                                              curr_ids.sourceFileId + " " + curr_ids.bugMasterId + ";\n";

        String fix_command = "timeout 2s /tools/nccb/bin/print-compile-input /data/compile-inputs " +
                            curr_ids.sourceFileId + " " + curr_ids.fixMasterId + ";\n";
        String footer = ""; // maybe echo something there too?
        script.append(bug_header);
        script.append(bug_command);
        script.append(fix_header);
        script.append(fix_command);
      }
      return script.toString();
    }

    /**
     * Clears the queue of ids, returns an array of BugFixFile objects.
     * Each of these contains the files for a bug and its fix.
     */
    public static LinkedList<BugFixFile> parseResultString(String resultString) {
        LinkedList<BugFixFile> results = new LinkedList<BugFixFile>();
        // separate into distinct blocks of data; each contains the source files
        // for a bug and its fix, preceded by the start line.
        String[] blocks = resultString.split(DELIMITER);
        // go through the blocks (start at 1 b/c the 0th is an empty string)
        for (int i = 1; i < blocks.length; i++)
        {
          // System.out.println("Block number " + i + "...");
          // System.out.println(blocks[i]);
          String[] dataFields = blocks[i].split(DELIMITER2);
          boolean bad_data = false;
          for(String field : dataFields) {
            if(field.contains("Searching rest") || field.equals("\n")) { bad_data = true;}
          }
          if(!bad_data) {results.add(new BugFixFile(dataFields));}
        }
        return results;
    }



}
