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

public class BlackBoxConnection {

  public static String remoteExec(Session session, int source_file_id, int master_id) throws Exception {
    String command = "/tools/nccb/bin/print-compile-input /data/compile-inputs " + source_file_id + " " + master_id;
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
        if (channel.getExitStatus() != 0) {
          channel.disconnect();
          throw new Exception();
        }
        System.out.println("exit-status: "+channel.getExitStatus());
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


  public static void main(String[] arg){
    try{
      JSch jsch=new JSch();

      jsch.setKnownHosts("/Users/fixmybug/.ssh/known_hosts");

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
      //session.setConfig("StrictHostKeyChecking", "no");

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
      //System.out.println("localhost:"+assigned_port+" -> "+rhost+":"+rport);

      //Get bugs and fixes from BlackBox (exec Query);
      BlackboxSolicitor mysql_interface = new BlackboxSolicitor(passwdDB);
      ResultSet results = mysql_interface.GetBugIDs(5);
      results.first();


      DBFillerInterface dbFiller = new DBFillerInterface("uploadTestDB");

      // Sort through results and get source files
      while(!results.isAfterLast()) {
        int fileId = results.getInt("source_file_id");
        int failId = results.getInt("Fail_id");
        int successId = results.getInt("Success_id");
        int startLine = results.getInt("start_line");
        String bug = "";
        String fix = "";
        // Run the scripts to get source code
        try {
          System.out.println("Getting bug...\nFile ID: " + fileId + " Fail ID: " + failId);
          bug = remoteExec(session, fileId, failId);
          System.out.println("Getting fix...\nFile ID: " + fileId + " Success ID: " + successId);
          fix = remoteExec(session, fileId, successId);
          System.out.println("Error on line: " + startLine);
          //PUT IN DB HERE
          dbFiller.uploadToDatabase(bug,fix,startLine);

          //System.out.println(bug);
          //System.out.println(fix);
        } catch(Exception e) {
          //e.getMessage();
          //System.out.println(e.getStackTrace());
          System.out.println("Missing data in blackbox: " + e.getMessage());
          System.out.println(e.getLocalizedMessage());
          System.out.println(e.getStackTrace());
          System.out.println(e);
        }
        results.next();
      }
      session.disconnect();

    }
    catch(Exception e){
      System.out.println(e);
    }
  }
}
