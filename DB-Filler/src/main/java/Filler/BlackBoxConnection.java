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

public class BlackBoxConnection {

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
        System.out.println("Executing query: ..." + finalQuery.toString() + "...");
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
   * Main method. Grabs data from our local database, then runs the executable
   * to get the corresponding source files from blackbox.
   */
  public static void main(String[] arg){
    // Starting constants
    final int QUEUE_SIZE = 10;
    final int START_ID = 25000;
    final int LIMIT = 20;

    if(arg.length > 0 && arg[0].equals("test")){
        Main.main(arg);
        System.exit(0);
    }


    try {
      // GET RESULTSET FROM OUR DATABASE
      ResultSet results = LocalQuery(START_ID,LIMIT); // (-1,-1) for no constraints on data

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
      // skip host-key check - we don't need to do this here, but on Windows
      // it's a nice alternative to figuring the known hosts
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
        BugFix temp = new BugFix(failId,successId,fileId,startLine);

        boolean success = scriptBuilder.addToQueue(temp);
        // if the queue is full, or we're out of results, get source code
        // Note that this doesn't do anything to the result currently in
        if (!success) {
          //System.out.println("Ready to gather source code!");
          String command = scriptBuilder.generateBashScript();
          //System.out.println(command);
          try {
            //System.out.println("Running remoteExec!\n");
            String resultString = remoteExec(session,command);
            //System.out.println(resultString);
            LinkedList<BugFixFile> resultCode = scriptBuilder.parseResultString(resultString);
            for(BugFixFile result : resultCode)
            {
              System.out.println("BUG!");
              //System.out.print("Bug Code: ");
              //System.out.println(result.bug);
              //System.out.print("Fix Code: ");
              //System.out.println(result.fix);
              //System.out.print("\nStart Line: ");
              //System.out.println(result.startLine);
              //dbFiller.uploadToDatabase(result.bug,result.fix,result.startLine);
            }
          } catch (Exception e) {
            //e.getMessage();
            //System.out.println(e.getStackTrace());
            System.out.println("Missing data in blackbox: " + e.getMessage());
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getStackTrace());
            System.out.println(e);
          }
        }
        // Iterate to next result if the queue was full.
        // (if it's the last entry, we still need to iterate so that the program
        // can know that it's past the end of the resultset and end the loop)
        else {
          stillIterating = results.next();
        }
      }

      ////////////////
      // Duplicated code to process any leftovers in the queue.
      // TODO make this nicer - put it all into a method?
      ////////////////
      String command = scriptBuilder.generateBashScript();
      //System.out.println(command);
      try {
        //System.out.println("Running remoteExec!\n");
        String resultString = remoteExec(session,command);
        //System.out.println(resultString);
        LinkedList<BugFixFile> resultCode = scriptBuilder.parseResultString(resultString);
        for(BugFixFile result : resultCode)
        {
          System.out.println("BUG!");
          // System.out.print("Bug Code: ");
          // System.out.println(result.bug);
          // System.out.print("Fix Code: ");
          // System.out.println(result.fix);
          // System.out.print("\nStart Line: ");
          // System.out.println(result.startLine);
          // dbFiller.uploadToDatabase(result.bug,result.fix,result.startLine);
        }
      } catch (Exception e) {
        //e.getMessage();
        //System.out.println(e.getStackTrace());
        System.out.println("Missing data in blackbox: " + e.getMessage());
        System.out.println(e.getLocalizedMessage());
        System.out.println(e.getStackTrace());
        System.out.println(e);
      }
      ////////////////
      // End duplicated code
      ////////////////


      session.disconnect();
    }
    catch(Exception e){
      System.out.println(e);
    }
  }
}
