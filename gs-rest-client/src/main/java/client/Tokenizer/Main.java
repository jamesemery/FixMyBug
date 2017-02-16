

package client.Tokenizer;

import client.Tokenizer.javaparser.JavaLexer;
import client.Tokenizer.javaparser.JavaParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

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
            TokenizerBuilder t = new TokenizerBuilder("/users/alexgriese/Desktop/HelloWorld.java","File");
            System.out.println(t.ediTokensToString());
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
