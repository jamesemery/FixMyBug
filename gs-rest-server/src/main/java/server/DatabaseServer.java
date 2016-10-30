package server;

import java.sql.ResultSet;
import java.sql.SQLException;
//import javax.swing.JOptionPane;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;

//to create database
import java.sql.*;

public class DatabaseServer {
    
    SQLiteDataSource dataSource;
    String tableName;
    
    public DatabaseServer(String fileName) {
        String url = "jdbc:sqlite:" + fileName;
        System.out.println("Making new database, url = " + url);
        tableName = "master_table";
        boolean initialize = false;
        try {
            initialize = SQLiteJDBCLoader.initialize();
            if (!initialize) throw new Exception("SQLite Library Not Loaded\n");
            dataSource = new SQLiteDataSource();
            dataSource.setUrl(url);
        }
        catch (Exception e) {
            System.out.println("Exception caught during database setup: \n");
            System.out.println(e.getMessage());
        }      
    }

    public static void createNewDatabase(String fileName) {
        String url = "jdbc:sqlite:./" + fileName;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is: " + meta.getDriverName());

                System.out.println("A new database has been created.");
            }

        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public final String SelectFrom(String column, String inputStr) {
        
        try { 
            System.out.println("Input String: " + inputStr + ".");
            ResultSet rs = dataSource.getConnection().createStatement()
                    .executeQuery("select " + column + " from \"" + tableName + "\" where buggy_code = \"" + inputStr + "\";");

            String result = rs.getString(column);
            if(result == null) {
                System.out.println("No results found\n");
                return "";
            }
            return result;
        }
        catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
            return "";
        }    
    }
    
    public final DatabaseEntry SelectAll(String inputStr) {
        DatabaseEntry queryResult = new DatabaseEntry();
        
        try {
            System.out.println("Input String: " + inputStr + ".");
            ResultSet rs = dataSource.getConnection()
                    .createStatement().executeQuery("select * from \"" + tableName + "\" where buggy_code = \"" + inputStr + "\";");

            //Retrieve by column name
            queryResult.setId(rs.getInt("id"));
            queryResult.setBuggyCode(rs.getString("buggy_code"));
            queryResult.setFixedCode(rs.getString("fixed_code"));
            queryResult.setCount(rs.getInt("count"));
            queryResult.setErrorType(rs.getInt("bug_type"));
            
            if(queryResult.getId() == 0) { //ID starts at 1, so 0 marks a null return value (i.e. no results)
                System.out.println("No results found\n");
                return queryResult;
            }

            //Display values
            /*System.out.print("ID: " + id);
            System.out.print(", Bug Type: " + bug_type);
            System.out.print(", Buggy: '" + buggy_code + "'");
            System.out.print("Sending Fixed Code: '" + fixed_code + "'");
            System.out.println(", Count: " + count);
            String result = "ID: " + id + ", Buggy Code: " + buggy_code +
                          ", Fixed Code: " + fixed_code + ", Count: " + count;
            return queryResult;*/
        }
        catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
            return queryResult;
        }
        return queryResult;
    }

    public final void Insert(int id, int bug_type, String buggy_code, String fixed_code, int count) {
        try {
            int rs = dataSource.getConnection().createStatement()
                .executeUpdate("INSERT INTO \"" + tableName + "\" VALUES ("
                + id + ", " + bug_type + ", \"" + buggy_code + "\", \"" + fixed_code + "\", " + count + ");");
            System.out.println("Changes: " + rs);
        }
        catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }
        
    public final void RemoveByID(int id) {
        try {
            int rs = dataSource.getConnection().createStatement()
                .executeUpdate("DELETE FROM \"" + tableName + "\" WHERE id="
                + id + ";");
            System.out.println("Changes: " + rs);
        }
        catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }
        
    public final void RemoveByBug(String bug) {
        try {
            int rs = dataSource.getConnection().createStatement()
                .executeUpdate("DELETE FROM \"" + tableName + "\" WHERE buggy_code="
                + bug + ";");
            System.out.println("Changes: " + rs);
        }
        catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }
    
    /*public static void main(String[] args) {
        DatabaseServer db = new DatabaseServer("/FixMyBug/TEST_DATABASE");
        String fix1 = db.SelectFrom("fixed_code","somebuggg");
        System.out.println("somefffix - " + fix1);
        String fix2 = db.SelectAll("somebug2");
        System.out.println(fix2);
        db.RemoveByID(4);
        db.RemoveByID(5);
        db.RemoveByID(6);
        //addToTable(args[0], 6, 5, "somebuggg", "somefffix", 2);
        //addToTable(args[0], 4, 2, "somebug2", "somefix2", 2);
        //addToTable(args[0], 5, 4, "somebug3", "somefix3", 2);
    }*/

}
