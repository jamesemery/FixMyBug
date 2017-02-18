

package client.Tokenizer;

import client.DatabaseEntry;
import client.HarmonizationStateObject;
import client.Tokenizer.javaparser.JavaLexer;
import client.Tokenizer.javaparser.JavaParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Checks if the TokenizerBuilder class works.
 * @author Alex Griese
 * @author Jamie Emery
 */

public class Main {

    public static void main(String[] args) {
        //printClassIdentifier();
        try {

            // Testing the file type of the constructor.

            // Creates a new instance of TokenizerBuilder.
            //TokenizerBuilder t = new TokenizerBuilder("" +
            //        "./Tokenizer/src/main/java/carleton/comps/javaparser/HelloWorld.java","File");
            //System.out.println(t.ediTokensToString());
            //ParseTreeWalker.DEFAULT.walk(extractor, tree);
            // Prints the tokenized version of HelloWorld.java.
            //System.out.println(t.getString());

            //List<Token> tokens = t.getTokens();
            //System.out.println(t.tokensToString(tokens));

            // Gets a list of Tokens that come from the HelloWorld.java file between lines 1 and 6.
            //List<Token> l = t.betweenLines(0,4);

            // Prints out the list of Tokens in lines 1 to 6 in the HelloWorld.java file.
            //System.out.println(t.tokensToString(l));


            /*========================================================================================================*/


            // Testing the String type of constructor.


            /*TokenizerBuilder x = new TokenizerBuilder("package carleton.comps.javaparser.examples;\n" +
                    "\n" +
                    "public class HelloWorld {\n" +
                    "   public static void main(String[] args) { \n" +
                    "      System.out.println(\"Hello, World\");\n" +
                    "   }\n" +
                    "}","String");
            */
            // Prints the tokenized version of HelloWorld.java.
            //System.out.println(t.getString());

            /*System.out.println(t.harmonize("PACKAGE Identifier DOT Identifier DOT Identifier DOT Identifier SEMI EOF EOF " +
                    "PUBLIC CLASS Identifier LBRACE EOF PUBLIC STATIC VOID Identifier LPAREN Identifier LBRACK RBRACK " +
                    "Identifier RPAREN LBRACE EOF Identifier DOT Identifier DOT Identifier LPAREN StringLiteral RPAREN " +
                    "SEMI EOF RBRACE EOF RBRACE EOF EOF"));
            */
            //List<Token> tokens = t.getTokens();
            //System.out.println(t.tokensToString(tokens));

            // Gets a list of Tokens that come from the HelloWorld.java file between lines 1 and 6.
            //List<Token> l = t.betweenLines(3,3);

            // Prints out the list of Tokens in lines 1 to 6 in the HelloWorld.java file.
            //System.out.println(t.tokensToString(l));
            System.out.println("STARTING PROCESS");
            String buggedCode = "package carleton.comps.javaparser;\n" +
                    "\n" +
                    "public class HelloWorld {\n" +
                    "   public static void main(String[] args) {\n" +
                    "      System.out.println(\"Hello, World\")\n" +
                    "   }\n" +
                    "}";
            String fixedCode = "package carleton.comps.javaparser;\n" +
                    "\n" +
                    "public class HelloWorld {\n" +
                    "   public static void main(String[] args) {\n" +
                    "      System.out.println(\"Hello, World\");\n" +
                    "      System.out.println(apple, 'a');\n" +
                    "   }\n" +
                    "}";

            TokenizerBuilder t = new TokenizerBuilder(buggedCode, "String");
            System.out.println("MADE TOKENIZERBUILDER");
            System.out.println(t.betweenLines(4, 6));
            List<Integer> userDisambiguation = TokenizerBuilder.generateDisambiguationList(t
                    .getTokens()).subList(11,29);
            System.out.println(t.getTokens());
            HarmonizationStateObject object = new HarmonizationStateObject(new TokenizerBuilder
                    (buggedCode, "String"), buggedCode, 4, 6);
            System.out.println("MADE Harmonization State Object");

            TokenizerBuilder fixedTokens = new TokenizerBuilder(fixedCode, "String");
            List<Integer> disambiguation = TokenizerBuilder.generateDisambiguationList(fixedTokens
                    .betweenLines(4, 7));
            DatabaseEntry entry = new DatabaseEntry(100, String.join(" ",t.betweenLines(4,6)
                    .stream().map(EdiToken::getType).map(Object::toString).collect(Collectors
                            .toList())),
                    String.join(" ",TokenizerBuilder.generateDisambiguationList(t.betweenLines(4,
                            6)).stream().map(Object::toString).collect(Collectors.toList())),
                    String.join(" ", fixedTokens.betweenLines(4, 7).stream().map(EdiToken::getType)
                            .map(Object::toString).collect(Collectors.toList())),
                    String.join(" ", disambiguation.stream().map(Object::toString).collect(Collectors.toList())));
            System.out.println(entry);

            System.out.println(object.harmonize(entry));


            System.out.println("================================================");


            System.out.println("STARTING PROCESS");
            String buggedCode2 = "package carleton.comps.javaparser;\n" +
                    "\n" +
                    "public class HelloWorld {\n" +
                    "   public static void main(String[] args) {\n" +
                    "       System.out.println(\"Hello, World\")\n" +
                    "       System.out.print(\"Hello, World\")\n" +
                    "   }\n" +
                    "}";
            String fixedCode2 = "package carleton.comps.javaparser;\n" +
                    "\n" +
                    "public class HelloWorld {\n" +
                    "   public static void main(String[] args) {\n" +
                    "      System.out.println(\"Hello, World\");\n" +
                    "      System.out.println(\"Hello, World\");\n" +
                    "   }\n" +
                    "}";

            TokenizerBuilder t2 = new TokenizerBuilder(buggedCode2, "String");
            System.out.println("MADE TOKENIZERBUILDER");
            System.out.println(t2.betweenLines(4, 7));
            List<Integer> userDisambiguation2 = TokenizerBuilder.generateDisambiguationList(t2
                    .getTokens()).subList(11,29);
            HarmonizationStateObject object2 = new HarmonizationStateObject(new TokenizerBuilder
                    (buggedCode2, "String"), buggedCode2, 4, 7);
            System.out.println("MADE Harmonization State Object");

            TokenizerBuilder fixedTokens2 = new TokenizerBuilder(fixedCode2, "String");
            List<Integer> disambiguation2 = TokenizerBuilder.generateDisambiguationList(fixedTokens
                    .betweenLines(4, 7));
            DatabaseEntry entry2 = new DatabaseEntry(100, String.join(" ",t2.betweenLines(4,6)
                    .stream().map(EdiToken::getType).map(Object::toString).collect(Collectors
                            .toList())),
                    String.join(" ",TokenizerBuilder.generateDisambiguationList(t2.betweenLines(4,
                            6)).stream().map(Object::toString).collect(Collectors.toList())),
                    String.join(" ", fixedTokens2.betweenLines(4, 7).stream().map(EdiToken::getType)
                            .map(Object::toString).collect(Collectors.toList())),
                    String.join(" ", disambiguation2.stream().map(Object::toString).collect
                            (Collectors.toList())));
            System.out.println(entry);

            System.out.println(object.harmonize(entry));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printClassIdentifier() {
        // Get our lexer
        JavaLexer lexer = new JavaLexer(new ANTLRInputStream("package carleton.comps.javaparser;\n" +
                "\n" +
                "public class HelloWorld {\n" +
                "   public static void main(String[] args) {\n" +
                "      System.out.println(\"Hello, World\");\n" +
                "   }\n" +
                "}"));

        // Get a list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Pass the tokens to the parser
        JavaParser parser = new JavaParser(tokens);

        // Specify our entry point
        JavaParser.CompilationUnitContext compilationUnitContext = parser.compilationUnit();

        // Walk it and attach our listener
        ParseTreeWalker walker = new ParseTreeWalker();
        IdentifierListener listener = new IdentifierListener();
        walker.walk(listener, compilationUnitContext);
    }
}
