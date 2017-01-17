package Filler;

import Filler.Tokenizer.TokenizerBuilder;
import Filler.Tokenizer.javaparser.*;
import Filler.Tokenizer.javaparser.JavaLexer;
import org.antlr.v4.runtime.Token;

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

            // Pruning the lists to the correct indexes

            // Conversion and building the thing
            // Tokens

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
    public static boolean isAmbiguousToken(Token t)
    {
        return JavaParser.VOCABULARY.getLiteralName(t.getType())==null;
    }
    public static boolean isAmbiuousToken(int t) {
        return JavaParser.VOCABULARY.getLiteralName(t)==null;
    }
}
