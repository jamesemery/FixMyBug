package Filler;

import java.sql.*;
import java.util.*;
import com.mysql.jdbc.jdbc2.optional.*;

public class BlackboxSolicitor {

    MysqlDataSource dataSource;

    // Constructor (database password as the argument)
    public BlackboxSolicitor(String passwd) {
        String url = "jdbc:mysql://127.0.0.1:3307/blackbox_production";
        String username = "whitebox";
        boolean initialize = false;

        //try to connect to the DB
        try {
            dataSource = new MysqlDataSource();
            dataSource.setUrl(url);
            dataSource.setUser(username);
            dataSource.setPassword(passwd);
        }
        catch (Exception e) {
            System.out.println("Exception caught during database setup: \n");
            System.out.println(e.getMessage());
        }
    }


    // Run the query! Return a resultset containing the results.
    public final ResultSet GetBugIDs(int numResults) throws SQLException{

        ResultSet rs = dataSource.getConnection().createStatement()
                .executeQuery("select distinct M1.id as Fail_id, " +
                              "M2.id as Success_id, CO1.source_file_id, " +
                              "CO1.start_line from ((compile_outputs CO1 join " +
                              "compile_events C1 on CO1.compile_event_id = C1.id) " +
                              "join master_events M1 on C1.id = M1.event_id)" +
                              "join (compile_events C2 join master_events M2 " +
                              "on C2.id = M2.event_id) on C1.id = C2.id-1 " +
                              "where M1.event_type = 'CompileEvent' and " +
                              "M2.event_type = 'CompileEvent' and C1.success = 0 " +
                              "and C2.success = 1 and M1.session_id = M2.session_id " +
                              "and M1.id > 25000 limit" + " " + numResults + ";");
        return rs;
    }
}
