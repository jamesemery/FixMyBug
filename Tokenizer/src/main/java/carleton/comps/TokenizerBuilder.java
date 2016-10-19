package carleton.comps;

import carleton.comps.javaparser.JavaLexer;
import carleton.comps.javaparser.JavaParser;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

/**
 * Chose a builder API
 *
 * General Usage Pattern: new Tokenizer(Filenam).betweenLines(40,50)
 * .getTokens()
 */
public class TokenizerBuilder {
    String fileSource;
    JavaLexer lexer;
    List<Token> data;
    List<Token> tokenizedLines;
    List<JavaLexer> lexedLines;

    // represents whether or not the lexer has already filled its input
    boolean lexed;

    //TODO open up the API to accept any sort of character stream as input
    public TokenizerBuilder(String code, String type) throws IOException {
        if (type.equals("file")){
            lexer = new JavaLexer(new ANTLRFileStream(code));
            // array of lines, send javalexer each line, store lexed line in array

            // Goes through each line of code and converts it into lexed code.
            Scanner scanner = new Scanner(new File(code));
            lexedLines = new ArrayList<JavaLexer>();
            while (scanner.hasNextLine()) {
                lexedLines.add(new JavaLexer(new ANTLRInputStream(scanner.nextLine())));
            }
        }
    }

    /**
     * Narrows down the tokenizer to the right set of lines
     * @return
     */
    public List<Token> betweenLines(int start, int stop) {
        // go to indexed lines
        if (lexed); //TODO something
        CommonTokenStream s;
        List<Token> list;
        tokenizedLines = new ArrayList<Token>();
        for (int i = start; i<=stop; i++) {
            s = new CommonTokenStream(lexedLines.get(i));
            s.getNumberOfOnChannelTokens();
            list = s.getTokens();

            for (Token t : list) {
                tokenizedLines.add(t);
            }
        }
        return tokenizedLines;

    }

    public String getString() {
        if (!lexed) {
            generateTokens();
        }
        StringBuilder builder = new StringBuilder();
        for (Token t : data) {
            builder.append(t.getType() +"(" + JavaParser.VOCABULARY
                    .getSymbolicName(t.getType()) + ") " + "(" + t.getText() + ") \n");
        }

        return builder.toString();
    }

    public String tokensToString(List<Token> tokens) {

        StringBuilder builder = new StringBuilder();
        for (Token t : tokens) {
            builder.append(t.getType() +"(" + JavaParser.VOCABULARY
                    .getSymbolicName(t.getType()) + ") " + "(" + t.getText() + ") \n");
        }

        return builder.toString();
    }

    public List<Token> getTokens() {
        if (!lexed) {
            generateTokens();
        }
        return data;
    }

    /*
     * Generates the tokens based on the code in the lexer.
     */

    public void generateTokens() {
        CommonTokenStream stream = new CommonTokenStream(lexer);
        data = stream.getTokens();
        stream.getNumberOfOnChannelTokens();
        lexed = true;
    }
}
