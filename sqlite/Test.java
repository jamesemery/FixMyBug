import java.sql.ResultSet;
import java.sql.SQLException;
//import javax.swing.JOptionPane;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;

//to create database
import java.sql.*;

public class Test {



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

    public static final void Connect(String databaseName) {
        boolean initialize = false;
        try {
            initialize = SQLiteJDBCLoader.initialize();

            SQLiteDataSource dataSource = new SQLiteDataSource();
            //dataSource.setUrl("jdbc:sqlite:.\\TEST_DATABASE.sqlite");
            dataSource.setUrl("jdbc:sqlite:./" + databaseName);

            System.out.println("connected!");
            ResultSet rs = dataSource.getConnection()
                    .createStatement().executeQuery("select * from \"master_table\"");


            while(rs.next()){
             //Retrieve by column name
             int id  = rs.getInt("id");
             String buggy_code = rs.getString("buggy_code");
             String fixed_code = rs.getString("fixed_code");
             int count = rs.getInt("count");
             int bug_type = rs.getInt("bug_type");

             //Display values
             System.out.print("ID: " + id);
             System.out.print(", Bug Type: " + bug_type);
             System.out.print(", Buggy: '" + buggy_code + "'");
             System.out.print(", Fixed: '" + fixed_code + "'");
             System.out.println(", Count: " + count);
          }

            // while (executeQuery.next()) {
            //     System.out.println("out: "+executeQuery.getMetaData().getColumnLabel(i));
            // }
        }
        catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static final void addToTable(String databaseName, int id, int bug_type, String buggy_code, String fixed_code, int count) {
        boolean initialize = false;
        try {
            initialize = SQLiteJDBCLoader.initialize();

            SQLiteDataSource dataSource = new SQLiteDataSource();
            //dataSource.setUrl("jdbc:sqlite:.\\TEST_DATABASE.sqlite");
            dataSource.setUrl("jdbc:sqlite:./" + databaseName);

            System.out.println("connected!");
            int rs = dataSource.getConnection().createStatement()
                .executeUpdate("INSERT INTO \"master_table\" VALUES ("
                + id + ", " + bug_type + ", \"" + buggy_code + "\", \"" + fixed_code + "\", " + count + ");");
            System.out.println("Changes: " + rs);
        }
        catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }


    public static void main(String[] args) {
        //createNewDatabase(args[0]);
        Connect(args[0]);
        //addToTable(args[0], 6, 5, "somebuggg", "somefffix", 2);
        //addToTable(args[0], 4, 2, "somebug2", "somefix2", 2);
        //addToTable(args[0], 5, 4, "somebug3", "somefix3", 2);
    }
}
