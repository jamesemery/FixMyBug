package carleton.comps;

import carleton.comps.javaparser.JavaLexer;
import carleton.comps.javaparser.JavaParser;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import java.util.List;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            ANTLRFileStream input = new ANTLRFileStream("/Users/alexgriese/College/CS/CompsSeminar/FixMyBug/Tokenizer/src/main/java/carleton/comps/javaparser/examples/HelloWorld.java");
            JavaLexer lexer = new JavaLexer(input);
            CommonTokenStream stream = new CommonTokenStream(lexer);
            //System.out.println(stream.getNumberOfOnChannelTokens());
            for (Token t : stream.getTokens()) {
                //System.out.println(t.getType() +"(" + JavaParser.VOCABULARY
                 //.getSymbolicName(t.getType()) + ") " + t.getText());
            }
            //System.out.println(stream.getTokens());


            TokenizerBuilder t = new TokenizerBuilder("/Users/alexgriese/College/CS/CompsSeminar/FixMyBug/Tokenizer/src/main/java/carleton/comps/javaparser/examples/HelloWorld.java","file");
            //System.out.println(t.getString());
            List<Token> l = t.betweenLines(1,2);
            System.out.println(t.tokensToString(l));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
