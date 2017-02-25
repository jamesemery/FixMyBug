package server;

import java.sql.ResultSet;
import java.sql.SQLException;
//import javax.swing.JOptionPane;


//to connect to the database using sqlite3
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;
import server.javaparser.DBAscii;
import server.javaparser.LevScorer;

//to create database
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/*class DatabaseServer holds all of the methods
and parameters for creating a database, creating
a table within that database and executing queries for
that table.

We use this class to store all of our bug/fix code data
as well as for the matching algorithm that compares client
code with the data to suggest a fix.
*/
public class DatabaseServer {

    SQLiteDataSource dataSource;
    String tableName;

    // Default database operation variables
    public static final int DEFAULT_NGRAM_SIZE = 3;
    public static final String DATABASE_TABLE_NAME = "master_table";
    public static final int MIN_SIMILAR_TO_PULL = 4;
    public static final double DEFAULT_PERCENT_TO_PULL = 0.5;
    public static final int MAX_USER_RETURN = 15;

    // The table entries
    public static final String[] DATABASE_TABLE_FORMAT = {"id", "buggyCode",
            "fixedCode"};

    // constructior (takes database name as the argument)
    public DatabaseServer(String fileName) {
        String url = "jdbc:sqlite:" + fileName;
        tableName = DATABASE_TABLE_NAME;
        boolean initialize = false;
        initalizeEscape();

        //try to connect to the DB
        try {
            initialize = SQLiteJDBCLoader.initialize();
            if (!initialize) throw new Exception("SQLite Library Not Loaded\n");
            dataSource = new SQLiteDataSource();
            dataSource.setUrl(url);
        } catch (Exception e) {
            System.out.println("Exception caught during database setup: \n");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Test method for creating a new database
     */
    public static void createNewDatabase(String fileName) {
        String url = "jdbc:sqlite:./" + fileName;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is: " + meta.getDriverName());

                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public final String SelectFrom(String column, String inputStr) {
        return SelectFrom(column, inputStr, tableName);
    }


    /**
     * select from method that generates an sql query in the form of:
     * SELECT column FROM table WHERE buggy_code = input;
     * This is mainly used for testing purposes
     */
    public final String SelectFrom(String column, String inputStr, String table) {

        try {
            ResultSet rs = dataSource.getConnection().createStatement()
                    .executeQuery("select " + column + " from \"" + table + "\" where buggy_code = \"" + inputStr + "\";");

            String result = rs.getString(column);
            if (result == null) {
                System.out.println("No results found\n");
                return "";
            }
            return result;
        } catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
            return "";
        }
    }

    /**
     * select all method that generates an sql query in the form of: TODO TEMPORARY
     * SELECT * FROM table WHERE buggy_code = input;
     * Mainly used for testing.
     */
    public final DatabaseEntry SelectAll(int id) {
        DatabaseEntry queryResult = new DatabaseEntry();

        try {
            System.out.println("ID String: " + id + ".");
            Connection connection = dataSource.getConnection();
            ResultSet rs = connection.createStatement().executeQuery("select * from \"" + tableName + "\" where " +
                    "id = \"" + id + "\";");

            rs.next();
            if (rs.isAfterLast()) {//ID starts at 1, so 0 marks a null return value (i.e.
                // no results)
                System.out.println("No results found\n");
                return queryResult;
            }

            queryResult = new DatabaseEntry(rs);
            connection.close();
        } catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
            return queryResult;
        }
        return queryResult;
    }


    /**
     * RemoveByID method simply removes a tuple from the default table
     * based on the ID that the user passes in. For testing mainly
     */
    public final void RemoveByID(int id) {
        try {
            int rs = dataSource.getConnection().createStatement()
                    .executeUpdate("DELETE FROM \"" + tableName + "\" WHERE id="
                            + id + ";");
        } catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }

    /**
     * RemoveByBug is very similar to RemoveByID
     * Removes the tuple based on the buggy_code match
     */
    public final void RemoveByBug(String bug) {
        try {
            int rs = dataSource.getConnection().createStatement()
                    .executeUpdate("DELETE FROM \"" + tableName + "\" WHERE buggy_code="
                            + bug + ";");
        } catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }

    /**
     * Mehtod that builds a database index table of the supllied table argument in the string
     * based on the Ngrams for error_code in the given table. The size of the Ngrams is determined
     * by the ngramSize argument.
     *
     * @return count of the total number of table rows that were created in the index
     */
    public int createIndex(int ngramsize, String table) throws SQLException {
        // Delete the existing index

        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        Statement statement2 = connection.createStatement();
        statement.executeUpdate("DROP TABLE" +
                " IF EXISTS " + table + "_" + ngramsize + "gramindex;");

        // Create new table
        statement.executeUpdate("CREATE TABLE " + table + "_"
                + ngramsize + "gramindex (id INTEGER, hash INTEGER);");

        ResultSet rows = statement.executeQuery("SELECT * from "
                + table);

        // Iterate through the master table to find ngrams
        int counter = 0;
        while (true) {
            if (rows.next()) {
                int id = rows.getInt("id");
                String errCode = DatabaseEntry.unescapeString(rows.getString("buggy_code"));

                // populate the the array with each ngram
                for (int i = ngramsize; i <= errCode.length(); i++) {
                    StringBuilder b = new StringBuilder();
                    for (int j = i - ngramsize; j < i; j++) {
                        b.append(errCode.charAt(j));
                    }
                    int hash = b.toString().hashCode();
                    counter++;
                    statement2.executeUpdate("INSERT INTO " + table + "_"
                            + ngramsize + "gramindex VALUES (" + id + "," + hash + ");");
                }
            } else break;

        }
        connection.close();
        return counter;
    }

    /**
     * Search that querys the database and returns a list with an Entry pair
     * containing the id of every occurance in the Table of each ngram from
     * the query list sorted by occurance.
     *
     * @param query
     * @param ngramsize
     * @param table
     * @return
     * @throws SQLException
     */
    public List<Integer> querySearch(String query, int ngramsize, String table) throws SQLException {
        String indexTableName = table + "_" + ngramsize + "gramindex";
        //Map<Integer, Integer> masterRow = new HashMap<>();

        Connection indConnection = dataSource.getConnection();

        //Creating an index table if none exists
        ResultSet s = indConnection.getMetaData().getTables(null, null, indexTableName, new String[]{"TABLE"});
        if (!s.next()) {
            System.out.println("Creating ngram index for database");
            createIndex(DEFAULT_NGRAM_SIZE, DATABASE_TABLE_NAME);
        }
        s.close();//;TODO figure out this logic nicely
        Statement indStatement = indConnection.createStatement();

        indStatement.executeUpdate("DROP TABLE" +
                " IF EXISTS " + table + "_" + ngramsize + "comparison;");

        // Create new table for comparisons
        indStatement.executeUpdate("CREATE TABLE " + table + "_"
                + ngramsize + "comparison (id INTEGER, fix VARCHAR(128));");

        String qTokens = DBAscii.toAsciiFormat(Arrays.asList(query.split(" ")).stream().map(Integer::parseInt)
                .collect(Collectors.toList()));
        System.out.println("UserCodeAscii is: " + qTokens);
        for (int i = ngramsize; i <= qTokens.length(); i++) {
            StringBuilder b = new StringBuilder();
            for (int j = i - ngramsize; j < i; j++) {
                b.append(qTokens.charAt(j));
            }
            int hash = b.toString().hashCode();

            // Getting all of table IDS that have the given string hash
            //ResultSet ngramSet = dataSource.getConnection().createStatement()
            //      .executeQuery("SELECT id FROM " + indexTableName + " " +
            //            "WHERE hash = " + hash);
            indStatement.executeUpdate("INSERT INTO " + table + "_" + ngramsize +
                    "comparison SELECT DISTINCT M.id, M.fixed_code FROM  master_table M JOIN " +
                    indexTableName + " I on M.id = I.id WHERE I.hash = " + hash + ";");
            // Grab every in in result set and put it into the map
//            while (ngramSet.next()) {
//                int masterid = ngramSet.getInt("id");
//                masterRow.put(masterid, (masterRow.containsKey(masterid)?
//                        masterRow.get(masterid):0) + 1);
//            }
        }
        ResultSet fixedSet = indStatement.executeQuery("SELECT id, count(*) from " + table + "_" + ngramsize +
                "comparison GROUP BY id ORDER BY count(*) desc LIMIT 100;");

        // Sort the map and return a list of integers sorted
//        List<Map.Entry<Integer,Integer>> results = new ArrayList(masterRow.entrySet());
//        Collections.sort(results, (Map.Entry o1, Map.Entry o2) -> (int)o2.getValue() - (int)o1.getValue());
//        return results;
        List<Integer> results = new ArrayList<>();
        while (fixedSet.next()) {
            int fixedid = fixedSet.getInt("id");
            results.add(fixedid);
        }

        indStatement.executeUpdate("DROP TABLE" +
                " IF EXISTS " + table + "_" + ngramsize + "comparison;");

        return results;
    }

    /**
     * Method that uses default values of the database server in order to grab
     * all ngram similar code in the database and filter it to return only
     * the most similar code to the user for transmission to the client
     *
     * @param userQuery
     * @return
     * @throws SQLException
     */
    public List<DatabaseEntry> getMostSimilarEntries(String userQuery) throws SQLException {
        // Pull database entries based on ngrams
//        List<Map.Entry<Integer, Integer>> sortedQuery = querySearch
//                (userQuery, DEFAULT_NGRAM_SIZE, DATABASE_TABLE_NAME);
        if (userQuery.equals("")){
            return new LinkedList<>();
        }

        // Pull the rows that are most prevalent
        List<Integer> rowsToPull = querySearch(userQuery, DEFAULT_NGRAM_SIZE, DATABASE_TABLE_NAME);

        // Create a query to pull all the rows from the table
        StringJoiner j = new StringJoiner(",", "SELECT * FROM " +
                "" + DATABASE_TABLE_NAME + " WHERE id IN (", ");");
        for (int i : rowsToPull) {
            j.add(Integer.toString(i));
        }
        ResultSet shortList = dataSource.getConnection().createStatement()
                .executeQuery(j.toString());
        List<DatabaseEntry> entryList = new ArrayList<>();
        while (shortList.next()) {
            entryList.add(new DatabaseEntry(shortList));
        }

        // Sorting the rows by a secondary algorithm
        for (DatabaseEntry e : entryList) {
            e.setSimilarity(computeSecondarySimilarity(userQuery, e));
        }
        Collections.sort(entryList, (DatabaseEntry a, DatabaseEntry b) -> (int) (b.getSimilarity
                () - a.getSimilarity()));

        //Cull the list to keep down outbound traffic to the client
        if (entryList.size() > MAX_USER_RETURN) {
            entryList = entryList.subList(0, MAX_USER_RETURN);
        }
        return entryList;
    }

    /**
     * A secondary similarity scoring algoirhtm that will be run in order to sort database entires
     * after they are pulled based on their ngram similarity
     *
     * @return a similarity score between the userQuery and Database Entry code represented as a double
     */
    private double computeSecondarySimilarity(String userQuery, DatabaseEntry entry) {
        List<Integer> q = Arrays.asList(userQuery.split(" ")).stream().map(Integer::parseInt)
                .collect(Collectors.toList());
        List<Integer> e = DBAscii.toIntegerListFromAscii(entry.getBuggyCode());
        double score = LevScorer.scoreSimilarityLocal(q, e);

        // A filter to try and ascribe less weight to tiny fixes
        List<Integer> fix = DBAscii.toIntegerListFromAscii(entry.getFixedCode());
        System.out.println("Fix Size is "+fix.size()+" Err size is "+e.size()+" and score is: "+score);
        if (fix.size()<=e.size()) {

            double scoreRatio = Math.atan( 10*(1.0*fix.size()-0.3*e.size()) / (1.0*e.size()) - 0.5) /Math.PI
                    + (1 - Math.atan( 10*(1.0-0.3) - 0.5)/ Math.PI);
            score = score * scoreRatio*scoreRatio;
        } else {

            double scoreRatio = Math.atan( 10*(-1.0*fix.size()+2.5*e.size()) / (1.0*e.size())) /Math.PI
                    + (1 - Math.atan( 10*(-1.0+2.5))/ Math.PI);
            score = score * scoreRatio*scoreRatio;
        }
        System.out.println("score is now: " + score);
        return score;
    }


//    public static void main(String[] args) {
//        DatabaseServer db = new DatabaseServer("/FixMyBugDB/TEST_DATABASE");
//        try {
//            db.createIndex(Integer.parseInt("4"), "test_tokens");
//            System.out.println(db.getMostSimilarEntries("100 100 100 100 100 110 100 100 100 60"));
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    //     //String fix1 = db.SelectFrom("fixed_code","somebuggg");
    //     //System.out.println("somefffix - " + fix1);
    //     //String fix2 = db.SelectAll("somebug2");
    //     //System.out.println(fix2);
    //     //db.RemoveByID(4);
    //     //db.RemoveByID(5);
    //     //db.RemoveByID(6);
    //     //addToTable(args[0], 6, 5, "somebuggg", "somefffix", 2);
    //     //addToTable(args[0], 4, 2, "somebug2", "somefix2", 2);
    //     //addToTable(args[0], 5, 4, "somebug3", "somefix3", 2);
//    }

    /**
     * GROSS METHOD ALERT: Due to obnoxiousness involving the spring framework and needing to
     * have hmologous classes, several methods need to be pulled into here, specifically the
     * sanitization program output
     */
    public static DatabaseEntryListWrapper sanitizeForJsonTransmission(DatabaseEntryListWrapper
                                                                               wrapper) {
        for (DatabaseEntry e : wrapper.getEntryList()) {
            e.setBuggyCode(e.buggyCodeAsList().stream().map(Object::toString).collect
                    (Collectors.joining(" ")).toString());
            e.setFixedCode(e.fixedCodeAsList().stream().map(Object::toString).collect
                    (Collectors.joining(" ")).toString());
            e.setBuggyCodeAssignments(e.buggyAssignmentsAsList().stream().map(Object::toString).collect
                    (Collectors.joining(" ")).toString());
            e.setFixedCodeAssignments(e.fixedAssignmentsAsList().stream().map
                    (Object::toString).collect(Collectors.joining(" ")).toString());

        }
        return wrapper;
    }
    //ANOTHER SUPER GROSS TEMPORARY THING......
    //TODO this has to stay in lockstep with the dbfiller stuff
    public static HashMap<Character,Character> ESCAPE_CHARACTERS = new HashMap<>();
    public static HashMap<Character,Character> SRETCARAHC_EPASCE = new HashMap<>();
    public void initalizeEscape() {
        if (ESCAPE_CHARACTERS.isEmpty()) {
            ESCAPE_CHARACTERS.put('\0','0');
            ESCAPE_CHARACTERS.put('\'','\'');
            ESCAPE_CHARACTERS.put('\"','\"');
            ESCAPE_CHARACTERS.put('\b','b');
            ESCAPE_CHARACTERS.put('\n','n');
            ESCAPE_CHARACTERS.put('\r','r');
            ESCAPE_CHARACTERS.put('\t','t');
            ESCAPE_CHARACTERS.put('\\','\\');
            ESCAPE_CHARACTERS.put('%','%');
            ESCAPE_CHARACTERS.put('_','_');
        }
        if (SRETCARAHC_EPASCE.isEmpty()) {
            SRETCARAHC_EPASCE.put('0','\0');
            SRETCARAHC_EPASCE.put('\'','\'');
            SRETCARAHC_EPASCE.put('\"','\"');
            SRETCARAHC_EPASCE.put('b','\b');
            SRETCARAHC_EPASCE.put('n','\n');
            SRETCARAHC_EPASCE.put('r','\r');
            SRETCARAHC_EPASCE.put('t','\t');
            SRETCARAHC_EPASCE.put('\\','\\');
            SRETCARAHC_EPASCE.put('%','%');
            SRETCARAHC_EPASCE.put('_','_');
        }
    }


}
