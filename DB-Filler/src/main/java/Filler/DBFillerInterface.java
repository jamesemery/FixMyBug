package Filler;

import Filler.Tokenizer.DBAscii;
import Filler.Tokenizer.TokenizerBuilder;
import Filler.Tokenizer.javaparser.*;
import org.antlr.v4.runtime.Token;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;

import java.io.IOException;
import java.util.*;

/**
 * A Class that handles the process of filling the database given input of two files
 */
public class DBFillerInterface {
    public static HashMap<Character,Character> ESCAPE_CHARACTERS = new HashMap<>();

    SQLiteDataSource dataSource;
    String tableName;
    public static int currentID = 1;

    public static final String DATABASE_TABLE_NAME = "master_table";


    // constructior (takes database name as the argument)
    public DBFillerInterface(String fileName) {
        if (ESCAPE_CHARACTERS.isEmpty()) {
            ESCAPE_CHARACTERS.put('\0','0');
            ESCAPE_CHARACTERS.put('\'','\'');
            ESCAPE_CHARACTERS.put('\"','\"');
            ESCAPE_CHARACTERS.put('\b','b');
            ESCAPE_CHARACTERS.put('\n','n');
            ESCAPE_CHARACTERS.put('\r','r');
            ESCAPE_CHARACTERS.put('\t','t');
            ESCAPE_CHARACTERS.put('\\','\\');
            ESCAPE_CHARACTERS.put('\t','t');
            ESCAPE_CHARACTERS.put('%','%');
            ESCAPE_CHARACTERS.put('_','_');
        }

        String url = "jdbc:sqlite:" + fileName;
        tableName = DATABASE_TABLE_NAME;
        boolean initialize = false;

        //try to connect to the DB
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

    /**
     * Main externally visible method that digests two files with err code data and uploads the
     * resulting generated database entry into the database
     *
     * @param file1 text of file containing the eroneous code
     * @param file2 text of file containing the fixed code
     * @param errLine the line number of the error message
     * @return
     */
    public boolean uploadToDatabase(String file1, String file2, int errLine) {
        //TODO here is where a check goes for valid line syntax
        if (false) {
            return false;
        }
        Insert(createDatabaseEntry(file1,file2,errLine-1,errLine+1,errLine-1,errLine+1));
        return true;
    }


    /**
     * Insert method that takes in parameters that match the master_table columns
     * This method then connects to the database and adds the data to the master_table
     */
    public final void Insert(DatabaseEntry entry) {
        try {
            entry.escape();
            System.out.println("filling to database: " + entry.toString());
            int rs = dataSource.getConnection().createStatement()
                    .executeUpdate("INSERT INTO \"" + tableName + "\" VALUES ("
                            + currentID++ + ", \"" + entry.getBuggyCode() + "\" , \"" + entry
                            .getBuggyCodeAssignments() + "\", \"" + entry.getFixedCode() + "\", \"" +
                            entry.getFixedCodeAssignments() + "\");");
            System.out.println("INSERT INTO \"" + tableName + "\" VALUES ("
                    + currentID++ + ", \"" + entry.getBuggyCode() + "\" , \"" + entry
                    .getBuggyCodeAssignments() + "\", \"" + entry.getFixedCode() + "\", \"" +
                    entry.getFixedCodeAssignments() + "\");");
        } catch (Exception ex) { //SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Method that takes two files and associated lines and builds a database entry to send
     */
    public DatabaseEntry createDatabaseEntry(String errCode, String fixCode,
                                             int errStartLine, int errEndLine, int fixStartLine,
                                             int fixEndLine) {
        try {
            // Ensure lines are valid?
            // SOME CODE

            // Reading each file into tokens
            List<Token> errFileTokens = (new TokenizerBuilder(errCode, "String")).getTokens();
            List<Token> fixFileTokens = (new TokenizerBuilder(fixCode, "String")).getTokens();

            List<Integer> errFileAssignments = new ArrayList<Integer>(errFileTokens.size());
            List<Integer> fixFileAssignments = new ArrayList<Integer>(fixFileTokens.size());

            //Dictionary that holds the string token assignments
            HashMap<String, Integer> ambigousAssignments = new HashMap<String, Integer>();

            int assignedVariables = 0;

            // Assigning disamibuation to tokens of the err file
            for (Token t: errFileTokens) {
                if (isAmbiguousToken(t)) {
                    if (ambigousAssignments.containsKey(t.getText())) {
                        errFileAssignments.add(ambigousAssignments.get(t.getText()));
                    } else {
                        ambigousAssignments.put(t.getText(), ++assignedVariables);
                        errFileAssignments.add(assignedVariables);
                    }
                }
                else {
                    errFileAssignments.add(0);
                }
            }

            // Assigning disamibuation to tokens of the err file
            for (Token t: fixFileTokens) {
                if (isAmbiguousToken(t)) {
                    if (ambigousAssignments.containsKey(t.getText())) {
                        fixFileAssignments.add(ambigousAssignments.get(t.getText()));
                    } else {
                        ambigousAssignments.put(t.getText(), ++assignedVariables);
                        fixFileAssignments.add(assignedVariables);
                    }
                }
                else {
                    fixFileAssignments.add(0);
                }
            }

            // Prune the lists to only account for the specified lines
            int errTokenStartIndex = 0;
            int errTokenEndIndex = 0;
            int fixTokenStartIndex = 0;
            int fixTokenEndIndex =0;

            boolean inWindow = false;
            for (int i = 0; i < errFileTokens.size(); i++) {
                if (!inWindow && errFileTokens.get(i).getLine()>=errStartLine) {
                    errTokenStartIndex = i;
                    inWindow = true;
                } else if (inWindow && errFileTokens.get(i).getLine()>errEndLine){
                    errTokenEndIndex = i -1;
                    break;
                }
            }

            // Determining which caracters in the tokens are in the target lines
            inWindow = false;
            for (int i = 0; i < fixFileTokens.size(); i++) {
                if (!inWindow && fixFileTokens.get(i).getLine()>=fixStartLine) {
                    fixTokenStartIndex = i;
                    inWindow = true;
                } else if (inWindow && fixFileTokens.get(i).getLine()>fixEndLine){
                    fixTokenEndIndex = i -1;
                    break;
                }
            }

            System.out.println("File A: " + errCode);
            System.out.println("A Tokens: " + errFileTokens);
            System.out.println("A File Assignments: " + errFileAssignments);

            System.out.println("\n\n" + ambigousAssignments.toString() + "\n\n");


            System.out.println("File B: " + fixCode);
            System.out.println("B Tokens: " + fixFileTokens);
            System.out.println("B File Assignments: " + fixFileAssignments);

            // Pruning everything to the correct lines
            System.out.println("File A Grabbed Lines: " + errFileTokens.subList(errTokenStartIndex,
                            errTokenEndIndex));
            System.out.println("File B Grabbed Lines: " + fixFileTokens.subList(fixTokenStartIndex,
                    fixTokenEndIndex));
            String buggy_code = DBAscii.tokensToAsciiFormat(errFileTokens.subList
                    (errTokenStartIndex, errTokenEndIndex));
            String buggy_code_assignnments = DBAscii.toAsciiFormat(errFileAssignments.subList(
                    errTokenStartIndex,errTokenEndIndex));

            String fix_code = DBAscii.tokensToAsciiFormat(fixFileTokens.subList(fixTokenStartIndex,
                    fixTokenEndIndex));
            String fix_code_assignnments = DBAscii.toAsciiFormat(fixFileAssignments.subList(
                    fixTokenStartIndex,fixTokenEndIndex));

            // Conversion and building the thing
            // TODO: NOTE, ID PRODUCED HERE IS BOGUS FOR THE TIME BEING
            DatabaseEntry out = new DatabaseEntry(0,buggy_code,buggy_code_assignnments,
                    fix_code,fix_code_assignnments);

            return out;

        }
        catch (IllegalArgumentException e) {
            System.out.println("Something went wrong with the tokenizer");
        }
        catch (IOException e) {
            System.out.println("Could not read file");
        }
        return null;
    }

    /**
     * Method that tests whether a given token is type ambiguous
     */
    public static boolean isAmbiguousToken(Token t)
    {
        return JavaParser.VOCABULARY.getLiteralName(t.getType())==null;
    }
    public static boolean isAmbiuousToken(int t) {
        return JavaParser.VOCABULARY.getLiteralName(t)==null;
    }
}
