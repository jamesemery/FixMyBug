package Filler;

import Filler.Tokenizer.TokenizerBuilder;
import Filler.Tokenizer.javaparser.*;
import org.antlr.v4.runtime.Token;
import

import java.io.IOException;
import java.util.*;

/**
 * A Class that handles the process of filling the database given input of two files
 */
public class DBFillerInterface {


    /**
     * Method that takes two files and associated lines and builds a database entry to send
     */
    public DatabaseEntry createDatabaseEntry(String errCode, String fixCode, String errorMessage,
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

            // Test to make sure that the number is within the adapter range
            if (assigned Variables )
            // If possible, try to write this thing to be smart


        }
        catch (IllegalArgumentException e) {
            System.out.println("Something went wrong with the tokenizer");
        }
        catch (IOException e) {
            System.out.println("Could not read file");
        }
    }

    //NOTES FROM MEETING: Put this code into a speperate file that does its own thing.


    /**
     * Method that tests whether a given token is type ambiguous
     */
    public static boolean isAmbiguousToken(Token t) {
        if (JavaLexer. )
    }
    public static boolean isAmbiuousToken(int t) {

    }
}
