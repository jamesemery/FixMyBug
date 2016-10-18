package com.company;

import com.company.javaparser.JavaLexer;
import com.company.javaparser.JavaParser;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    // represents whether or not the lexer has already filled its input
    boolean lexed;

    //TODO open up the API to accept any sort of character stream as input
    public TokenizerBuilder(String file) throws IOException {
        lexer = new JavaLexer(new ANTLRFileStream(file));
        // array of lines, send javalexer each line, store lexed line in array
    }

    /**
     * Narrows down the tokenizer to the right set of lines
     * @return
     */
    public TokenizerBuilder betweenLines(int start, int stop) {
        // go to indexed lines
        if (lexed); //TODO something
        return this;

    }

    public String getString() {
        if (!lexed) {
            generateTokens();
        }
        StringBuilder builder = new StringBuilder();
        for (Token t : data) {
            builder.append(t.getType() +"(" + JavaParser.VOCABULARY
                    .getLiteralName(t.getType()) + ") ");
        }
        return builder.toString();
    }

    public List<Token> getTokens() {
        if (!lexed) {
            generateTokens();
        }
        return data;
    }

    public void generateTokens() {
        CommonTokenStream stream = new CommonTokenStream(lexer);
        data = stream.getTokens();
        lexed = true;
    }
}
