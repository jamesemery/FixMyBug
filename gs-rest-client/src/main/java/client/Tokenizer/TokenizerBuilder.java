package client.Tokenizer;

import client.Tokenizer.javaparser.JavaLexer;
import client.Tokenizer.javaparser.JavaParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Takes in code as a string or as a file, tokenizes and stores the code.
 *
 * @author Alex Griese
 * @author Jamie Emery
 */
public class TokenizerBuilder {

    // Holds the tokenized lines of code.
    //private List<Token> tokenizedCode;
    private List<EdiToken> ediTokenizedCode;

    // Holds the specific names of all null tokens.
    private Queue<String> integerTokens;
    private Queue<String> floatingTokens;
    private Queue<String> booleanTokens;
    private Queue<String> characterTokens;
    private Queue<String> stringTokens;
    private Queue<String> nullTokens;
    private Queue<String> identifierTokens;

    private boolean newLine;

    /*
     * Constructs an instance of the TokenizerBuilder given the code passed in as the code parameter.
     * @Param: Code, holds the code that is to be tokenized.
     * @Param: Type, what type of information the code is stored as.
     */
    public TokenizerBuilder(String code, String type) throws IOException {
        // Initializes tokenizedCode of code.
        ediTokenizedCode = new ArrayList<EdiToken>();

        // Initializes all the queues that hold the values for specific code.
        integerTokens = new LinkedList<String>();
        floatingTokens = new LinkedList<String>();
        booleanTokens = new LinkedList<String>();
        characterTokens = new LinkedList<String>();
        stringTokens = new LinkedList<String>();
        nullTokens = new LinkedList<String>();
        identifierTokens = new LinkedList<String>();

        // This if statement is used to tokenize the code according to its type.
        if (type.equals("File")){

            // Checks to see if the file provided is a valid file.
            Scanner scanner;
            try {
                FileInputStream codeReader = new FileInputStream(new File(code));

                // Goes through each line of code and converts it into tokenized code.
                ediTokenizedCode = generateTokens(new JavaLexer(new ANTLRInputStream(codeReader)));
            } catch (FileNotFoundException fe) {
                throw new FileNotFoundException("That is not a valid file!");
            }

        } else if (type.equals("String")) {

            // Tokenizes the code
            ediTokenizedCode = generateTokens(new JavaLexer(new ANTLRInputStream(code)));
        } else {
            throw new IllegalArgumentException("TokenizerBuilder requires a String or File.");
        }
    }

    /*
     * Generates the tokens based on the code in the lexer.
     * @Param: lexedLine, a line of lexed code.
     * @Return: tokenizedLine, the lexed code is now returned as a tokenized line.
     */
    private List<EdiToken> generateTokens(JavaLexer lexedLine) {

        // Converts the lexed line into a token steam and gets the tokens.
        CommonTokenStream stream = new CommonTokenStream(lexedLine);
        List<Token> tokenizedLine = stream.getTokens();

        // Unclear as to why this line is needed, but if it is not called the token stream seemingly returns no tokens
        // and nothing can be printed out.
        stream.getNumberOfOnChannelTokens();

        List <EdiToken> tokenLine = generateIdentifiers(tokenizedLine, stream);

        //holdNullTokens(tokenizedLine);

        // Removes the EOF token.
        tokenLine.remove(tokenLine.size()-1);

        return tokenLine;
    }

    private List<EdiToken> generateIdentifiers(List<Token> tokenizedLine, CommonTokenStream tokens) {
        List<EdiToken> ediTokens = new ArrayList<EdiToken>();

        // Pass the tokens to the parser
        JavaParser parser = new JavaParser(tokens);

        // Specify our entry point
        JavaParser.CompilationUnitContext compilationUnitContext = parser.compilationUnit();

        // Walk it and attach our listener
        ParseTreeWalker walker = new ParseTreeWalker();
        IdentifierListener listener = new IdentifierListener();
        walker.walk(listener, compilationUnitContext);
        int identifier = 0;
        int[] types = new int[7];
        for (Token t : tokenizedLine) {
            EdiToken token = new EdiToken(t);
            if (token.getType() == 100) {
                    token.setType(110);
                    types[0]++;
                } else if (listener.identifierPosition.get(identifier) == "function") {
                    token.setType(111);
                    types[1]++;
                } else if (listener.identifierPosition.get(identifier) == "variable") {
                    token.setType(112);
                    types[2]++;
                } else if (listener.identifierPosition.get(identifier) == "outside") {
                    token.setType(113);
                    types[3]++;
                } else if (listener.identifierPosition.get(identifier) == "variableClass") {
                    token.setType(114);
                    types[4]++;
                } else if (listener.identifierPosition.get(identifier) == "variableFunction") {
                    token.setType(115);
                    types[5]++;
                } else if (listener.identifierPosition.get(identifier) == "functionVariableClass") {
                    token.setType(116);
                    types[6]++;
                }
                identifier++;
            }
            if (token.getType()!= 104 || token.getType()!= 105) {
                ediTokens.add(token);
            }
        }
        ediTokens = identifierCheck(ediTokens);
        return ediTokens;
    }

    private List<EdiToken> identifierCheck(List<EdiToken> ediTokens) {
        List<EdiToken> tokens = ediTokens;
        for (EdiToken t: ediTokens) {
            if (t.getType()>113) {
                for (EdiToken to: tokens) {
                    if (to.getText().equals(t.getText()) && to.getType()<114) {
                        t.setType(to.getType());
                        break;
                    }
                }
            }
        }
        return ediTokens;
    }


    /*
     * Returns the tokenized code as a space separated string of ints. Defaults verbose to false.
     * @Return: string version of space separated string of ints.
     */
    public String getString() {
        return getString(false);
    }

    /*
     * Returns the tokenized code between the specified lines
     */
    public String getString(int start, int stop) {
        return tokensToString(betweenLines(start, stop));
    }

    /*
     * Returns the tokenized code as a string.
     * @Return: the string version of the tokenized code.
     */
    public String getString(Boolean verbose) {
        return tokensToString(ediTokenizedCode, verbose);
    }

    /*
     * Returns a list of all the tokens.
     * @Return: allTokens, list of all tokens in code.
     */
    public List<EdiToken> getTokens() {
        return ediTokenizedCode;
    }


    /*
     * Takes a list of tokens and returns the corresponding string.
     * @Param: tokens, a list of tokens to be converted into a string.
     * @Return: tokens as a string.
     */
    public static String tokensToString(List<EdiToken> tokens) {
        return tokensToString(tokens, false);
    }

    /*
     * Takes a list of tokens and returns the corresponding string.
     * @Param: tokens, a list of tokens to be converted into a string.
     * @Param: verbose,.
     * @Return: tokens as a string.
     */
    public static String tokensToString(List<EdiToken> tokens, boolean verbose) {

        StringBuilder builder = new StringBuilder();

        // Goes through each token, converts it into a string and adds it to builder.
        for (EdiToken t : tokens) {
            if (verbose) {
                builder.append(t.getType() +"(" + JavaParser.VOCABULARY
                        .getSymbolicName(t.getType()) + ") " + "(" + t.getText() + ") \n");
            } else {
                builder.append(t.getType() + " ");
            }
        }

        return builder.toString();
    }

    /*
     * Based on the lines specified by the user via @param start and @param stop, returns the tokenized code.
     * @Param: start, first line of tokenized code to be returned.
     * @Param: stop, last line of tokenized code to be returned.
     * @Return: tokens, all tokens between start line and stop line.
     */
    public List<EdiToken> betweenLines(int start, int stop) {

        if (start < 1 || stop > ediTokenizedCode.get(ediTokenizedCode.size()-1).getLine()) {
            System.out.println(ediTokenizedCode);
            System.out.println("Last line: "+ediTokenizedCode.get(ediTokenizedCode.size()-1)
                    .getLine() +"\nLast Token:"+ediTokenizedCode.get(0));
            throw new IndexOutOfBoundsException("The lines you specified are out of range. " +
                    "Start:"+start+"   Stop:"+stop);
        }


        // Holds all the tokenized code.
        List<EdiToken> tokens = new ArrayList<EdiToken>();

        // Searches through the caracter stream for which tokens correspond to the correct line
        for (int i = 0; i<ediTokenizedCode.size(); i++) {
//            System.out.println("i:"+i);
        	int curLine = ediTokenizedCode.get(i).getLine();
//        	System.out.println("cuyrLine:"+curLine);
            if (curLine >= start && curLine <= stop) {
                tokens.add(ediTokenizedCode.get(i));
//                System.out.println("added Token:"+ediTokenizedCode.get(i));
            }
        }
        return tokens;

    }

    /**
     * Method that generates the name association list for a string of tokens
     * @param tokenizedCode
     * @return
     * TODO: note the fact that this must be kept in step with DBFILLER INTERFACE
     */
    public static List<Integer> generateDisambiguationList(List<EdiToken> tokenizedCode) {

        List<Integer> assignments = new ArrayList<Integer>(tokenizedCode.size());

        //Dictionary that holds the string token assignments
        HashMap<String, Integer> ambigousAssignments = new HashMap<String, Integer>();

        int assignedVariables = 0;

        // Assigning disamibuation to tokens of the err file
        for (EdiToken t: tokenizedCode) {
            if (isAmbiguousToken(t)) {
                if (ambigousAssignments.containsKey(t.getText())) {
                    assignments.add(ambigousAssignments.get(t.getText()));
                } else {
                    ambigousAssignments.put(t.getText(), ++assignedVariables);
                    assignments.add(assignedVariables);
                }
            }
            else {
                assignments.add(0);
            }
        }
        return assignments;
    }

    /**
     * Method that tests whether a given token is type ambiguous
     */
    public static boolean isAmbiguousToken(EdiToken t)
    {
        return JavaParser.VOCABULARY.getLiteralName(t.getType())==null;
    }
    public static boolean isAmbiguousToken(int t) {
        return JavaParser.VOCABULARY.getLiteralName(t)==null;
    }

    /**
     * Method that returns true if the polled token is part of the identifier tree
     */
    public static boolean isIdentifier(int token) {
        if ((token==100)||(token>109)) return true;
        return false;
    }

    /**
     * Method that takes two integers and returns true if they are degenerate
     */
    public static boolean isDegenerate(int token1, int token2) {
        if (token1==token2) return true;
        if (isIdentifier(token1)&&isIdentifier(token2)) {
            // if one is a super token, return true;
            if ((token1==116)||(token2==116)) return true;


            // if one is a class, check for class degeneracy
            if ((token1==100)||(token2==100)) {
                if ((token1==114)||(token2==114)) {
                    return true;
                }
                return false;
            }

            // if one is a fucntion, check for possible degeneracy
            else if ((token1==111)||(token2==111)) {
                if ((token1==115)||(token2==115)) {
                    return true;
                }
                return false;
            }

            // if one is a variable, check for possible degeneracy
            else if ((token1==112)||(token2==112)) {
                if ((token1>113)||(token2==113)) {
                    return true;
                }
                return false;
            }

            // At this point, either the two cant be the same OR both are 114 and 115
            else if (((token1==114)&&(token2==115))||((token1==115)&&(token2==114))) return true;
        }
        return false;
    }
}