package carleton.comps;

import carleton.comps.javaparser.JavaLexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.util.List;

public class NgramTest {

    public static void main(String [] args) {
        try {
            List<Token> c1 = new TokenizerBuilder
                    ("./Tokenizer/src/main/java/carleton/comps/javaparser" +
                    "/examples/HelloWorld.java","File").getTokens();
            List<Token> c2 = new TokenizerBuilder
                    ("./Tokenizer/src/main/java/carleton/comps/javaparser" +
                            "/examples/HelloWorld.java","File").betweenLines
                    (0,7);
            System.out.println(c1.size() + " " + c1);
            System.out.println(c2.size() + " " + c2);

            System.out.println(NgramScorer.scoreSimilarity("public static " +
                    "void main(String[] args) {", "public static void main" +
                    "(String[] args) { try {"));

            System.out.println(LevScorer.scoreSimilarity(c1,c2));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
