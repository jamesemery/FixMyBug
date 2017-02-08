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



  //public static String remoteExec(Session session, int source_file_id, int master_id) throws Exception {
  //  String command = "/tools/nccb/bin/print-compile-input /data/compile-inputs " + source_file_id + " " + master_id;
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




  public static String getPassword(String prompt)
  {
    Console console = System.console();
    System.out.print(prompt);
    char[] passwordChars = console.readPassword();
    String passwordString = new String(passwordChars);
    return passwordString;
  }


  /**
   * Creating a new database
   */
  // public static void createNewDatabase(String fileName) {
  //     String url = "jdbc:sqlite:./" + fileName;
  //
  //     try (Connection conn = DriverManager.getConnection(url)) {
  //         if (conn != null) {
  //             DatabaseMetaData meta = conn.getMetaData();
  //             System.out.println("The driver name is: " + meta.getDriverName());
  //
  //             System.out.println("A new database has been created.");
  //         }
  //
  //     } catch (SQLException e) {
  //         System.out.println(e.getMessage());
  //     }
  // }

  /**
   * Insert method that takes in parameters that match the master_table columns
   * This method then connects to the database and adds the data to the master_table
   */
  public static void Insert(ResultSet results, String fileName) {
      System.out.println("Starting to add results to local DB...");
      String url = "jdbc:sqlite:./" + fileName;
      String tableName = "BLACKBOX_ENTRIES";
      boolean initialize = false;

      //try to connect to the DB
      try {
          initialize = SQLiteJDBCLoader.initialize();
          if (!initialize) throw new Exception("SQLite Library Not Loaded\n");
          SQLiteDataSource dataSource = new SQLiteDataSource();
          dataSource.setUrl(url);

          results.first();
          int currentID = 0;
          Connection connection = dataSource.getConnection();
          while(!results.isAfterLast())
          {
              currentID++;
              if(currentID % 5000 == 0) {
                  System.out.println("Adding entry number: " + currentID);
              }
              int rs = connection.createStatement()
                      .executeUpdate("INSERT INTO " + tableName + " VALUES (" +
                       results.getInt(1) + "," +
                       results.getInt(2) + "," +
                       results.getInt(3) + "," +
                       results.getInt(4) + "," +
                       results.getInt(5) + "," +
                       results.getInt(6) + ");");
              /*System.out.println("INSERT INTO \"" + tableName + "\" VALUES ("
                      + currentID++ + ", \"" + entry.getBuggyCode() + "\" , \"" + entry
                      .getBuggyCodeAssignments() + "\", \"" + entry.getFixedCode() + "\", \"" +
                      entry.getFixedCodeAssignments() + "\");");*/

              results.next();
          }
          connection.close();
      } catch (Exception ex) { //SQLException ex) {
          System.out.println(ex.getMessage());
      }
  }


  public static void main(String[] arg){
    try{
      JSch jsch=new JSch();

      //jsch.setKnownHosts("/Users/fixmybug/.ssh/known_hosts");

      String host=null;
      if(arg.length>0){
          host=arg[0];
      } else if (arg[0].equals("test")){
          Main.main(arg);
      } else {
          System.out.println("Needs an argument, in the form username@white.kent.ac.uk");
          System.exit(0);
      }


      String user=host.substring(0, host.indexOf('@'));
      host=host.substring(host.indexOf('@')+1);

      Session session=jsch.getSession(user, host, 22);

      // Get password from user
      System.out.println("User: " + user);
      String passwd = getPassword("SSH Password: ");

      //String passwd = arg[1];
      //System.out.println("Password: " + passwd);
      session.setPassword(passwd);

      // skip host-key check
      session.setConfig("StrictHostKeyChecking", "no");

      session.connect(30000);   // making a connection with timeout.

      // Get database password in preparation for setting that up
      String passwdDB = getPassword("Database Password: ");

      //Port Forwarding
      String foo="3307:localhost:3306";
      int lport=Integer.parseInt(foo.substring(0, foo.indexOf(':')));
      foo=foo.substring(foo.indexOf(':')+1);
      String rhost=foo.substring(0, foo.indexOf(':'));
      int rport=Integer.parseInt(foo.substring(foo.indexOf(':')+1));
      int assigned_port=session.setPortForwardingL(lport, rhost, rport);

      //Get bugs and fixes from BlackBox (exec Query);
      BlackboxSolicitor mysql_interface = new BlackboxSolicitor(passwdDB);
      ResultSet results = mysql_interface.GetRawData();
      //ResultSet results = mysql_interface.GetBugIDs(7);
      //CreateNewDatabase("BLACKBOX_DATA")
      Insert(results, "blackboxDataDB");

      /*
      // Executable Calls
      BashScriptBuilder scriptBuilder = new BashScriptBuilder(1,session);
      results.first();
      //TODO call to the sqlite database rather than blackbox
      DBFillerInterface dbFiller = new DBFillerInterface("uploadTestDB");
      // Sort through results and get source files
      while(!results.isAfterLast()) {
        int fileId = results.getInt("source_file_id");
        int failId = results.getInt("Fail_id");
        int successId = results.getInt("Success_id");
        int startLine = results.getInt("start_line");
        String bug = "";
        String fix = "";
        BugFix temp = new BugFix(failId,successId,fileId,startLine);
        // System.out.print("SOURCE FILE ID" + fileId + "...");
        boolean success = scriptBuilder.addToQueue(temp);
        // System.out.print("ADDING TO QUEUE...");
        //System.out.println(success);

        // if the queue is full, or we're out of results, get source code
        if (!success || results.isLast()) {
          // System.out.println("Ready to gather source code!");
          String command = scriptBuilder.generateBashScript();
          //System.out.println(command);
          try {
            String resultString = remoteExec(session,command);
            //System.out.println(resultString);
            LinkedList<BugFixFile> resultCode = scriptBuilder.parseResultString(resultString);
            for(BugFixFile result : resultCode)
            {
              System.out.println("\n\nBUG INFORMATION...................\n");
              System.out.print("Bug Code: ");
              System.out.println(result.bug);
              System.out.print("Fix Code: ");
              System.out.println(result.fix);
              System.out.print("\nStart Line: ");
              System.out.println(result.startLine);
              dbFiller.uploadToDatabase(result.bug,result.fix,result.startLine);
            }
          } catch (Exception e) {
            //e.getMessage();
            //System.out.println(e.getStackTrace());
            System.out.println("Missing data in blackbox: " + e.getMessage());
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getStackTrace());
            System.out.println(e);
          }
          // DO THINGS
        }
        // This isn't just an else case b/c the last data point needs to only
        // get processed once.
        if (success) {
          results.next();
        }



        // Put it in the queue and do stuff

        // Run the scripts to get source code
        // try {
        //   System.out.println("Getting bug...\nFile ID: " + fileId + " Fail ID: " + failId);
        //   bug = remoteExec(session, fileId, failId);
        //   System.out.println("Getting fix...\nFile ID: " + fileId + " Success ID: " + successId);
        //   fix = remoteExec(session, fileId, successId);
        //   System.out.println("Error on line: " + startLine);
        //   //PUT IN DB HERE
        //   dbFiller.uploadToDatabase(bug,fix,startLine);
        //
        //   //System.out.println(bug);
        //   //System.out.println(fix);
        // } catch(Exception e) {
        //
        // }

      }
*/
      session.disconnect();

    }
    catch(Exception e){
      System.out.println(e);
    }
  }
}
