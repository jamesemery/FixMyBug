import java.sql.ResultSet;
import java.sql.SQLException;
//import javax.swing.JOptionPane;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;

public class Test {

    

    public static final void Connect() {
        boolean initialize = false;
        try {
            initialize = SQLiteJDBCLoader.initialize();

            SQLiteDataSource dataSource = new SQLiteDataSource();
            //dataSource.setUrl("jdbc:sqlite:.\\TEST_DATABASE.sqlite");
            dataSource.setUrl("jdbc:sqlite:.\\TEST_DATABASE");
            ResultSet rs = dataSource.getConnection()
                    .createStatement().executeQuery("select * from \"master_table\"");


            while(rs.next()){
             //Retrieve by column name
             int id  = rs.getInt("id");
             String buggy_code = rs.getString("buggy_code");
             String fixed_code = rs.getString("fixed_code");
             int count = rs.getInt("count");

             //Display values
             System.out.print("ID: " + id);
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

    public static void main(String[] args) {
        Connect();
        //why the fuck is this even a boolean
    }
}