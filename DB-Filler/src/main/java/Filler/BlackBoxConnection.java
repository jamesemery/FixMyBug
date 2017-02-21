/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/**
 * This program enables you to connect to sshd server and get the shell prompt.
 *   $ CLASSPATH=.:../build javac Shell.java
 *   $ CLASSPATH=.:../build java Shell
 * You will be asked username, hostname and passwd.
 * If everything works fine, you will get the shell prompt. Output may
 * be ugly because of lacks of terminal-emulation, but you can issue commands.
 *
 */
package Filler;

import com.jcraft.jsch.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;
import org.apache.commons.io.IOUtils;

public class BlackBoxConnection {
  public static Integer filenumber = 0;

/**
 * Queries the local blackbox database.
 *    startID: adds a "where id > x" clause to avoid duplication
 *    maxVals: adds a "limit x" clause for testing
 * Both values can be set to -1 to not put the clause in at all.
 */
public static ResultSet LocalQuery(int startID, int maxVals) {
    System.out.println("Starting to query local DB...");
    String url = "jdbc:sqlite:./blackboxDataDB";
    String tableName = "BLACKBOX_ENTRIES";
    boolean initialize = false;

    //try to connect to the DB
    try {
        initialize = SQLiteJDBCLoader.initialize();
        if (!initialize) throw new Exception("SQLite Library Not Loaded\n");
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        Connection connection = dataSource.getConnection();
        String query = "select * from (session1 S1 JOIN session2 S2 on S1.id = S2.id)" +
                      "where S1.success = 0 and S2.success = 1 and S1.session_id = S2.session_id";
        String qualifier = " and S1.id > " + startID;
        String limit = " limit " + maxVals;
        //String min = "where "
        StringBuilder finalQuery = new StringBuilder(query);
        if(startID > 0) {
          finalQuery.append(qualifier);
        }
        if(maxVals > 0) {
          finalQuery.append(limit);
        }
        finalQuery.append(";");
        //System.out.println("Executing query: ..." + finalQuery.toString() + "...");
        ResultSet rs = connection.createStatement().executeQuery(finalQuery.toString());
        //connection.close();
        return rs;
    } catch (Exception ex) { //SQLException ex) {
        System.out.println(ex.getMessage());
        return null;
    }
}

  //public static String remoteExec(Session session, int source_file_id, int master_id) throws Exception {
  //  String command = "/tools/nccb/bin/print-compile-input /data/compile-inputs " + source_file_id + " " + master_id;

/**
 *  Calls an executable on the blackbox server. Opens a new channel within the
 *  session, runs the command it's given, and returns the result.
 */
  public static String remoteExec(Session session, String command) throws Exception {
    Channel channel=session.openChannel("exec");
    ((ChannelExec)channel).setCommand(command);

    // X Forwarding
    // channel.setXForwarding(true);

    //channel.setInputStream(System.in);
    channel.setInputStream(null);

    channel.setOutputStream(System.out);

    //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
    //((ChannelExec)channel).setErrStream(fos);
    ((ChannelExec)channel).setErrStream(System.err);

    InputStream in=channel.getInputStream();

    channel.connect(2000);

    byte[] tmp=new byte[1024];
    StringBuilder inputString = new StringBuilder();
    while(true){
      while(in.available()>0){
        int i=in.read(tmp, 0, 1024);
        if(i<0)break;
        inputString.append(new String(tmp,0,i));
        //System.out.print(new String(tmp, 0, i));
      }
      if(channel.isClosed()){
        if(in.available()>0) continue;
        //if (channel.getExitStatus() != 0) {
          //channel.disconnect();
          //throw new Exception();
        //}
        //System.out.println("exit-status: "+channel.getExitStatus());
        break;
      }
      try{Thread.sleep(1000);}catch(Exception ee){}
    }
    channel.disconnect();
    return inputString.toString();
  }

  /**
   * A simple function to get a password from the user.
   */
  public static String getPassword(String prompt)
  {
    Console console = System.console();
    System.out.print(prompt);
    char[] passwordChars = console.readPassword();
    String passwordString = new String(passwordChars);
    return passwordString;
  }

  /**
   * Pulls source code from the server and adds it to the database. This isn't
   * really all that distinct from the main method, but it needs to happen in
   * two places due to limitations in the main loop's logic (SQLite being a pain),
   * so it's pulled out into a helper method for the sake of clarity/consistency.
   */
  public static void gatherSourceCode(Session session,
                                      BashScriptBuilder scriptBuilder,
                                      DBFillerInterface dbfiller)
  {
    System.out.println("Getting source code!");
    String command = scriptBuilder.generateBashScript();
    //System.out.println(command);
    try {
      //System.out.println("Running remoteExec!\n");
      String resultString = remoteExec(session,command);

      String filepath = "/Users/fixmybug/FixMyBugData/";

      BufferedWriter out = null;
      try
      {
        FileWriter fstream = new FileWriter(filepath + "bugsandfixes" + filenumber.toString() + ".txt", true); //true tells to append data.
        out = new BufferedWriter(fstream);
        out.write(resultString);
      }
      catch (IOException e)
      {
        System.err.println("Error: " + e.getMessage());
      }
      finally
      {
    if(out != null) {
        out.close();
    }
    System.out.println("Wrote file number " + filenumber.toString());
    filenumber += 1;
}
      //System.out.println(resultString);
      //LinkedList<BugFixFile> resultCode = scriptBuilder.parseResultString(resultString);
      //for(BugFixFile result : resultCode)
      //{
      //  System.out.println("BUG!");
        //System.out.print("Bug Code: ");
        //System.out.println(result.bug);
        //System.out.print("Fix Code: ");
        //System.out.println(result.fix);
        //System.out.print("\nStart Line: ");
        //System.out.println(result.startLine);

        //dbFiller.uploadToDatabase(result.bug,result.fix,result.startLine);
      //}
    } catch (Exception e) {
      //e.getMessage();
      //System.out.println(e.getStackTrace());
      System.out.println("Missing data in blackbox: " + e.getMessage());
      System.out.println(e.getLocalizedMessage());
      System.out.println(e.getStackTrace());
      System.out.println(e);
    }
  }

  /**
   * Reads from files and adds the source code to the database.
   */
  public static void readFiles(int num_files) {
    DBFillerInterface dbFiller = new DBFillerInterface("UploadTestDB");

    for(int i = 0; i <= num_files; i++)
    {
      String filepath = "/Users/fixmybug/FixMyBugData/bugsandfixes" + i + ".txt";
      try(FileInputStream inputStream = new FileInputStream(filepath)) {
        String resultString = IOUtils.toString(inputStream);

        LinkedList<BugFixFile> results = BashScriptBuilder.parseResultString(resultString);
        for(BugFixFile result : results)
        {
          System.out.println("reading file number " + i + "...");
          //System.out.print("Bug Code: ");
          //System.out.println(result.bug);
          //System.out.print("Fix Code: ");
          //System.out.println(result.fix);
          //System.out.print("\nStart Line: ");
          //System.out.println(result.startLine);
          dbFiller.uploadToDatabase(result.bug, result.fix, result.startLine);
        }
        System.out.println("Creating 3gramIndex");
        dbFiller.createIndex(3, dbFiller.tableName);
        System.out.println("Creating 4gramIndex");
        dbFiller.createIndex(4, dbFiller.tableName);
        System.out.println("Creating 5gramIndex");
        dbFiller.createIndex(5, dbFiller.tableName);
        System.out.println("Creating 6gramIndex");
        dbFiller.createIndex(6, dbFiller.tableName);
        System.out.println("Creating 7gramIndex");
        dbFiller.createIndex(7, dbFiller.tableName);

      } catch (Exception e) {
        System.out.println("Caught exception reading file:" + e.getLocalizedMessage());
        System.out.println(e.getStackTrace());
        //System.exit(1);
      }
    }
  }




  /**
   * Main method. Grabs data from our local database, then runs the executable
   * to get the corresponding source files from blackbox.
   */
  public static void main(String[] arg){

    // Command line flag to go to the test code instead.
    if(arg.length > 0 && arg[0].equals("test")){
        Main.main(arg);
        System.exit(0);
    }


    if(arg.length > 0 && arg[0].equals("read"))
    {
      //int NUM_FILES = 10900;
      int NUM_FILES = 20;
      readFiles(NUM_FILES);
      System.exit(0);
    }

    // SET STARTING CONSTANTS
    final int QUEUE_SIZE = 30; // how many items to get from the server at a time
    final int START_ID = 4700; // sets lower limit on id (-1 for no limit)
    final int LIMIT = -1; // sets a max # of results (-1 for no max)

    try {
      // GET RESULTSET FROM OUR DATABASE
      ResultSet results = LocalQuery(START_ID,LIMIT);


      // CONNECT TO BLACKBOX
      Scanner input = new Scanner(System.in);
      System.out.print("SSH Username: ");
      String user = input.next();
      String host = "white.kent.ac.uk";
      JSch jsch=new JSch();
      jsch.setKnownHosts("/Users/fixmybug/.ssh/known_hosts");
      Session session=jsch.getSession(user, host, 22);
      String passwd = getPassword("SSH Password: ");
      session.setPassword(passwd);
      // this line skips host-key check - we don't need to do this here,
      // but on Windows it's a nice alternative to figuring out the known hosts
      //session.setConfig("StrictHostKeyChecking", "no");
      session.connect(30000);   // making a connection with timeout.


      // GO THROUGH DATABASE RESULTS
      BashScriptBuilder scriptBuilder = new BashScriptBuilder(QUEUE_SIZE,session);
      DBFillerInterface dbFiller = new DBFillerInterface("uploadTestDB");
      // SQLite doesn't have any sort of isLast() capability, and because we
      // sometimes need to run the loop twice for a single result, we can't
      // simply do while(results.next()) as is customary.
      boolean stillIterating = results.next();
      while(stillIterating) {
        int failId = results.getInt(4);
        int successId = results.getInt(11);
        int fileId = results.getInt(6);
        int startLine = results.getInt(7);
        int lineId = results.getInt(1);
        BugFix temp = new BugFix(failId,successId,fileId,startLine);
        System.out.println("Line id " + lineId);
        boolean success = scriptBuilder.addToQueue(temp);
        // if addToQueue returns false, the queue is full - time to empty it.
        if (!success) { // queue is full - get source code

          gatherSourceCode(session, scriptBuilder, dbFiller);
        } else { // queue is not full (result was added to it) - keep moving
          stillIterating = results.next();
        }
      }
      // Catch any leftover results that didn't quite fill up the queue.
      gatherSourceCode(session, scriptBuilder, dbFiller);

      session.disconnect();
    }
    catch(Exception e){
      System.out.println(e);
    }
  }
}
