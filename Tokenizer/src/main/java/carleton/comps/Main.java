package carleton.comps;

import carleton.comps.javaparser.JavaLexer;
import carleton.comps.javaparser.JavaParser;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            ANTLRFileStream input = new ANTLRFileStream("/Users/jamie/Documents/College/Senior/Comps/FixMyBug/Tokenizer/src/main/java/carleton/comps/Main.java");
            JavaLexer lexer = new JavaLexer(input);
            CommonTokenStream stream = new CommonTokenStream(lexer);
            System.out.println(stream.getNumberOfOnChannelTokens());
            for (Token t : stream.getTokens()) {
                System.out.println(t.getType() +"(" + JavaParser.VOCABULARY
                        .getSymbolicName(t.getType()) + ") " + t.getText());
            }
            System.out.println(stream.getTokens());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
