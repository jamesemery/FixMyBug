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
                    "/examples/HelloWorld.java","file").getTokens();
//            List<Token> c2 = new TokenizerBuilder
//                    ("./Tokenizer/src/main/java/carleton/comps/javaparser" +
//                            "/examples/HelloWorld.java","file").betweenLines
//                    (1,7);
//            System.out.println(c2.size() + " " + new TokenizerBuilder
//                    ("./Tokenizer/src/main/java/carleton/comps/javaparser" +
//                            "/examples/HelloWorld.java","file").betweenLines
//                    (1,7));

            System.out.println(NgramScorer.scoreSimilarity("public static " +
                    "void main(String[] args) {", "public static void main" +
                    "(String[] args) { try {"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
