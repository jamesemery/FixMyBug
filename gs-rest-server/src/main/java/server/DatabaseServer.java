package server;

import java.sql.ResultSet;
import java.sql.SQLException;
//import javax.swing.JOptionPane;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;

//to create database
import java.sql.*;
import java.util.*;

public class DatabaseServer {

    SQLiteDataSource dataSource;
    String tableName;

    // Default database operation variables
    public static final int DEFAULT_NGRAM_SIZE = 4;
    public static final String DATABASE_TABLE_NAME = "master_table";
    public static final String[] DATABASE_TABLE_FORMAT = {"id", "buggyCode",
            "fixedCode"};
    public static final int MIN_SIMILAR_TO_PULL = 4;
    public static final double DEFAULT_PERCENT_TO_PULL = 0.5;
    public static final int DEFAULT_USER_RETURN = 2;

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
        return SelectFrom(column, inputStr, tableName);
    }


    public final String SelectFrom(String column, String inputStr, String table) {

        try {
            System.out.println("Input String: " + inputStr + ".");
            ResultSet rs = dataSource.getConnection().createStatement()
                    .executeQuery("select " + column + " from \"" + table + "\" where buggy_code = \"" + inputStr + "\";");

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

    public final String SelectAll(String inputStr) {
        try {
            System.out.println("Input String: " + inputStr + ".");
            ResultSet rs = dataSource.getConnection()
                    .createStatement().executeQuery("select * from \"" + tableName + "\" where buggy_code = \"" + inputStr + "\";");

            //Retrieve by column name
            int id  = rs.getInt("id");
            String buggy_code = rs.getString("buggy_code");
            String fixed_code = rs.getString("fixed_code");
            int count = rs.getInt("count");
            int bug_type = rs.getInt("bug_type");

            if(id == 0) { //ID starts at 1, so 0 marks a null return value (i.e. no results)
                System.out.println("No results found\n");
                return "";
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

    public void createIdex(int ngramsize, String table) throws SQLException {
        // Delete the existing index
        //dataSource.getConnection().createStatement().executeQuery("DROP TABLE table" + "_" + ngramsize + "gramindex;");

        // Create new table
        dataSource.getConnection().createStatement().executeQuery("CREATE TABLE " + table + "_"
                + ngramsize + "gramindex (id INTEGER, hash INTEGER);");

        ResultSet rows = dataSource.getConnection().createStatement().executeQuery("SELECT * from "
        + table);

        while (rows.next()) {
            int id = rows.getInt("id");
            String errCode = rows.getString("error_tokens");
            String[] tokens = errCode.split(",");

            // populate the the array with each ngram
            for (int i = ngramsize; i <= tokens.length; i++) {
                StringBuilder b = new StringBuilder();
                for (int j = i - ngramsize; j < i; j++) {
                    b.append(tokens[j] + ",");
                }
                int hash = b.toString().hashCode();
                dataSource.getConnection().createStatement().executeQuery("INSERT INTO"+ table + "_"
                + ngramsize + "gramindex VALUES ("+id+","+hash+");");
            }


        }
    }

    /**
     * Search that querys the database and returns a list with an Entry pair
     * containing the id of every occurance in the Table of each ngram from
     * the query list sorted by occurance.
     * @param query
     * @param ngramsize
     * @param table
     * @return
     * @throws SQLException
     */
    public List<Map.Entry<Integer, Integer>> querySearch(String query, int ngramsize, String table) throws SQLException {
        //TODO make sure this test works for finding emtpy tables
        String indexTableName = table + "_" + ngramsize + "gramindex";
        Map<Integer, Integer> masterRow = new HashMap<>();

//        if (dataSource.getConnection().createStatement().executeQuery("IF " +
//                        "EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES"
//                "WHERE TABLE_NAME = N'" + table + "')" +
//        "BEGIN" +
//        "PRINT 'Table Exists'" +
//        "END))").get

        String[] qTokens = query.split(" ");
        for (int i = ngramsize; i <= qTokens.length; i++) {
            StringBuilder b = new StringBuilder();
            for (int j = i - ngramsize; j < i; j++) {
                b.append(qTokens[j] + " ");
            }
            int hash = b.toString().hashCode();

            //
            ResultSet ngramSet = dataSource.getConnection().createStatement()
                    .executeQuery("SELECT id FROM " + indexTableName + " " +
                            "WHERE hash = " + hash);

            // Grab every in in result set and put it into the map
            while (ngramSet.next()) {
                int masterid = ngramSet.getInt("id");
                masterRow.put(masterid, masterRow.get(masterid) + 1);
            }
        }

        // Sort the map and return a list of integers sorted
        List<Map.Entry<Integer,Integer>> results = new ArrayList(masterRow.entrySet
                ());
        Collections.sort(results, (Map.Entry o1, Map.Entry o2) -> (int)o1
                .getValue() - (int)o2.getValue()
        );
        return results;
    }

    // TODO figure out what this returns in the grand scheme of things

    /**
     * Method that uses default values of the database server in order to grab
     * all ngram similar code in the database and filter it to return only
     * the most similar code to the user for transmission to the client
     * @param userQuery
     * @return
     * @throws SQLException
     */
    public List<String> getMostSimilarEntries(String userQuery) throws SQLException {
        List<Map.Entry<Integer, Integer>> sortedQuery = querySearch
                (userQuery, DEFAULT_NGRAM_SIZE, DATABASE_TABLE_NAME);

        List<Integer> rowsToPull = new ArrayList<>();
        for (int i = 0; (i < MIN_SIMILAR_TO_PULL || i <
                DEFAULT_PERCENT_TO_PULL*sortedQuery.size()) && i < sortedQuery.size();
             i++) {
            rowsToPull.add(sortedQuery.get(i).getKey());
        }

        StringJoiner j = new StringJoiner("SELECT * FROM " +
                ""+DATABASE_TABLE_NAME+" WHERE id=", "OR " + "id=", "");
        for (int i : rowsToPull) {
            j.add(Integer.toString(i));
        }
        ResultSet shortList = dataSource.getConnection().createStatement()
                .executeQuery(j.toString());

        List<DatabaseEntry> entryList = new ArrayList<>();
        while(shortList.next()) {
            entryList.add(new DatabaseEntry(shortList));
        }

        //TODO do another sort of the rows
        for (DatabaseEntry e : entryList) {
            e.setSimilarity( computeSecondarySimilarity(userQuery, e));
        }

        Collections.sort(entryList, (DatabaseEntry a, DatabaseEntry b) -> (int)(
                a.getSimilarity() - b.getSimilarity()));


        List<String> output = new ArrayList<>();
        for (DatabaseEntry e : entryList) output.add(e.toString());
        return output;
    }

    private double computeSecondarySimilarity(String userQuery, DatabaseEntry e) {
        return 0;
    }


    public static void main(String[] args) {
        //createIdex(args[0], args[1]);
        //DatabaseServer db = new DatabaseServer("/FixMyBug/TEST_DATABASE");
        //String fix1 = db.SelectFrom("fixed_code","somebuggg");
        //System.out.println("somefffix - " + fix1);
        //String fix2 = db.SelectAll("somebug2");
        //System.out.println(fix2);
        //db.RemoveByID(4);
        //db.RemoveByID(5);
        //db.RemoveByID(6);
        //addToTable(args[0], 6, 5, "somebuggg", "somefffix", 2);
        //addToTable(args[0], 4, 2, "somebug2", "somefix2", 2);
        //addToTable(args[0], 5, 4, "somebug3", "somefix3", 2);
    }

}
